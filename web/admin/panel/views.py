from django.contrib import messages
from django.shortcuts import redirect, render
from django.views.decorators.http import require_POST

from . import api
from .api import ApiError
from .decoradores import requiere_admin

ESTADOS_PEDIDO = [
    ("PENDIENTE", "Nuevos"),
    ("ACEPTADO", "Aceptados"),
    ("EN_PREPARACION", "En preparación"),
    ("LISTO_PARA_RECOGER", "Listos"),
    ("RECOGIDO", "Recogidos"),
    ("CANCELADO", "Cancelados"),
]

ROLES = [("CLIENTE", "Cliente"), ("NEGOCIO", "Negocio"), ("ADMIN", "Administrador")]


def _texto(request, campo):
    return (request.POST.get(campo) or "").strip()


def _numero(valor, tipo=float):
    try:
        return tipo(str(valor).replace(",", ".")) if str(valor).strip() != "" else None
    except ValueError:
        return None


def login_view(request):
    if request.session.get("token"):
        return redirect("resumen")

    correo = ""
    if request.method == "POST":
        correo = _texto(request, "correo")
        password = request.POST.get("password") or ""
        if not correo or not password:
            messages.error(request, "Escribe tu correo y tu contraseña")
        else:
            try:
                respuesta = api.llamar(None, "POST", "/auth/login", datos={"correo": correo, "password": password})
                usuario = respuesta["usuario"]
                if usuario.get("rol") != "ADMIN":
                    messages.error(request, "Solo los administradores pueden entrar a este panel")
                else:
                    request.session.cycle_key()
                    request.session["token"] = respuesta["token"]
                    request.session["usuario"] = {
                        "id": usuario.get("id"),
                        "nombre": usuario.get("nombre"),
                        "apellido": usuario.get("apellido"),
                        "correo": usuario.get("correo"),
                    }
                    return redirect("resumen")
            except ApiError as error:
                messages.error(request, error.mensaje)

    return render(request, "panel/login.html", {"correo": correo})


def logout_view(request):
    request.session.flush()
    return redirect("login")


@requiere_admin
def resumen(request):
    datos = api.get(request, "/admin/resumen")
    pedidos = api.get(request, "/pedidos")[:8]
    por_estado = datos.get("pedidosPorEstado", {})
    total = sum(por_estado.values()) or 0
    barras = [
        {"estado": clave, "texto": texto, "cantidad": por_estado.get(clave, 0)}
        for clave, texto in ESTADOS_PEDIDO
    ]
    return render(
        request,
        "panel/resumen.html",
        {"datos": datos, "pedidos": pedidos, "barras": barras, "total_pedidos": total, "seccion": "resumen"},
    )


@requiere_admin
def usuarios(request):
    lista = api.get(request, "/usuarios")
    q = (request.GET.get("q") or "").strip().lower()
    rol = request.GET.get("rol") or ""
    if q:
        lista = [
            u for u in lista
            if q in (u.get("nombre") or "").lower()
            or q in (u.get("apellido") or "").lower()
            or q in (u.get("correo") or "").lower()
        ]
    if rol:
        lista = [u for u in lista if u.get("rol") == rol]
    return render(
        request,
        "panel/usuarios.html",
        {"usuarios": lista, "q": request.GET.get("q", ""), "rol": rol, "roles": ROLES, "seccion": "usuarios"},
    )


def _datos_usuario(request, nuevo):
    datos = {
        "nombre": _texto(request, "nombre"),
        "apellido": _texto(request, "apellido"),
        "correo": _texto(request, "correo"),
        "telefono": _texto(request, "telefono"),
        "rol": request.POST.get("rol") or "CLIENTE",
        "activo": request.POST.get("activo") == "on",
    }
    password = request.POST.get("password") or ""
    if password or nuevo:
        datos["password"] = password
    return datos


@requiere_admin
def usuario_nuevo(request):
    usuario = {"rol": "CLIENTE", "activo": True}
    if request.method == "POST":
        usuario = _datos_usuario(request, True)
        try:
            api.post(request, "/usuarios", usuario)
            messages.success(request, f"Usuario {usuario['correo']} creado")
            return redirect("usuarios")
        except ApiError as error:
            messages.error(request, error.mensaje)
    return render(
        request,
        "panel/usuario_form.html",
        {"usuario": usuario, "roles": ROLES, "nuevo": True, "seccion": "usuarios"},
    )


@requiere_admin
def usuario_editar(request, id):
    usuario = api.get(request, f"/usuarios/{id}")
    if request.method == "POST":
        datos = _datos_usuario(request, False)
        try:
            api.put(request, f"/usuarios/{id}", datos)
            messages.success(request, "Usuario actualizado")
            return redirect("usuarios")
        except ApiError as error:
            messages.error(request, error.mensaje)
            usuario.update(datos)
    return render(
        request,
        "panel/usuario_form.html",
        {"usuario": usuario, "roles": ROLES, "nuevo": False, "seccion": "usuarios"},
    )


@require_POST
@requiere_admin
def usuario_activo(request, id):
    valor = request.POST.get("valor") == "true"
    api.put(request, f"/usuarios/{id}/activo", params={"valor": str(valor).lower()})
    messages.success(request, "Usuario activado" if valor else "Usuario desactivado")
    return redirect(request.META.get("HTTP_REFERER") or "usuarios")


@require_POST
@requiere_admin
def usuario_eliminar(request, id):
    api.delete(request, f"/usuarios/{id}")
    messages.success(request, "Usuario eliminado")
    return redirect("usuarios")


@requiere_admin
def negocios(request):
    lista = api.get(request, "/negocios/todos")
    q = (request.GET.get("q") or "").strip().lower()
    estado = request.GET.get("estado") or ""
    if q:
        lista = [
            n for n in lista
            if q in (n.get("nombre") or "").lower()
            or q in (n.get("tipoComida") or "").lower()
            or q in (n.get("direccion") or "").lower()
        ]
    if estado == "habilitados":
        lista = [n for n in lista if n.get("activo")]
    elif estado == "deshabilitados":
        lista = [n for n in lista if not n.get("activo")]
    return render(
        request,
        "panel/negocios.html",
        {"negocios": lista, "q": request.GET.get("q", ""), "estado": estado, "seccion": "negocios"},
    )


def _datos_negocio(request):
    return {
        "nombre": _texto(request, "nombre"),
        "tipoComida": _texto(request, "tipoComida"),
        "descripcion": _texto(request, "descripcion"),
        "direccion": _texto(request, "direccion"),
        "telefono": _texto(request, "telefono"),
        "horario": _texto(request, "horario"),
        "imagen": _texto(request, "imagen"),
        "latitud": _numero(request.POST.get("latitud")),
        "longitud": _numero(request.POST.get("longitud")),
        "tiempoEstimado": _numero(request.POST.get("tiempoEstimado"), int),
        "activo": request.POST.get("activo") == "on",
    }


def _candidatos_propietario(request, negocio_actual=None):
    usuarios = api.get(request, "/usuarios")
    negocios = api.get(request, "/negocios/todos")
    ocupados = {
        n["propietario"]["id"]
        for n in negocios
        if n.get("propietario") and (not negocio_actual or n["id"] != negocio_actual)
    }
    return [u for u in usuarios if u["id"] not in ocupados and u.get("rol") != "ADMIN"]


@requiere_admin
def negocio_nuevo(request):
    negocio = {"activo": True, "tiempoEstimado": 15}
    modo = "nuevo"
    propietario = {}
    if request.method == "POST":
        negocio = _datos_negocio(request)
        modo = request.POST.get("modo_propietario") or "nuevo"
        cuerpo = {"negocio": negocio}
        if modo == "existente":
            cuerpo["propietarioId"] = _numero(request.POST.get("propietarioId"), int)
        else:
            propietario = {
                "nombre": _texto(request, "p_nombre"),
                "apellido": _texto(request, "p_apellido"),
                "correo": _texto(request, "p_correo"),
                "telefono": _texto(request, "p_telefono"),
                "password": request.POST.get("p_password") or "",
            }
            cuerpo["nuevoPropietario"] = propietario
        try:
            api.post(request, "/negocios", cuerpo)
            messages.success(request, f"Negocio {negocio['nombre']} registrado")
            return redirect("negocios")
        except ApiError as error:
            messages.error(request, error.mensaje)
    return render(
        request,
        "panel/negocio_form.html",
        {
            "negocio": negocio,
            "nuevo": True,
            "modo": modo,
            "propietario": propietario,
            "candidatos": _candidatos_propietario(request),
            "seccion": "negocios",
        },
    )


@requiere_admin
def negocio_editar(request, id):
    negocio = api.get(request, f"/negocios/{id}")
    if request.method == "POST":
        datos = _datos_negocio(request)
        params = {}
        propietario_id = _numero(request.POST.get("propietarioId"), int)
        if propietario_id:
            params["propietarioId"] = propietario_id
        try:
            api.put(request, f"/negocios/{id}", datos, params=params)
            messages.success(request, "Negocio actualizado")
            return redirect("negocios")
        except ApiError as error:
            messages.error(request, error.mensaje)
            negocio.update(datos)
    return render(
        request,
        "panel/negocio_form.html",
        {
            "negocio": negocio,
            "nuevo": False,
            "candidatos": _candidatos_propietario(request, negocio_actual=id),
            "seccion": "negocios",
        },
    )


@require_POST
@requiere_admin
def negocio_estado(request, id):
    accion = "habilitar" if request.POST.get("valor") == "true" else "deshabilitar"
    negocio = api.post(request, f"/negocios/{id}/{accion}")
    messages.success(request, f"{negocio['nombre']} {'habilitado' if accion == 'habilitar' else 'deshabilitado'}")
    return redirect(request.META.get("HTTP_REFERER") or "negocios")


@require_POST
@requiere_admin
def negocio_eliminar(request, id):
    api.delete(request, f"/negocios/{id}")
    messages.success(request, "Negocio eliminado")
    return redirect("negocios")


@requiere_admin
def categorias(request):
    if request.method == "POST":
        datos = {"nombre": _texto(request, "nombre"), "descripcion": _texto(request, "descripcion")}
        id = request.POST.get("id")
        try:
            if id:
                api.put(request, f"/categorias/{id}", datos)
                messages.success(request, "Categoría actualizada")
            else:
                api.post(request, "/categorias", datos)
                messages.success(request, "Categoría creada")
        except ApiError as error:
            messages.error(request, error.mensaje)
        return redirect("categorias")

    lista = api.get(request, "/categorias", params={"todas": "true"})
    generales = sorted([c for c in lista if not c.get("negocio")], key=lambda c: c["nombre"].lower())
    de_negocios = sorted([c for c in lista if c.get("negocio")], key=lambda c: (c["negocio"]["nombre"], c["nombre"]))
    editar = request.GET.get("editar")
    return render(
        request,
        "panel/categorias.html",
        {
            "generales": generales,
            "de_negocios": de_negocios,
            "editar": int(editar) if editar and editar.isdigit() else None,
            "seccion": "categorias",
        },
    )


@require_POST
@requiere_admin
def categoria_eliminar(request, id):
    api.delete(request, f"/categorias/{id}")
    messages.success(request, "Categoría eliminada")
    return redirect("categorias")


@requiere_admin
def pedidos(request):
    estado = request.GET.get("estado") or ""
    negocio = request.GET.get("negocio") or ""
    lista = api.get(request, "/pedidos", params={"estado": estado} if estado else None)
    negocios = sorted(
        {p["negocio"]["id"]: p["negocio"]["nombre"] for p in lista if p.get("negocio")}.items(),
        key=lambda n: n[1],
    )
    if negocio:
        lista = [p for p in lista if p.get("negocio") and str(p["negocio"]["id"]) == negocio]
    total = sum(p.get("total") or 0 for p in lista if p.get("estado") != "CANCELADO")
    return render(
        request,
        "panel/pedidos.html",
        {
            "pedidos": lista,
            "estado": estado,
            "negocio": negocio,
            "estados": ESTADOS_PEDIDO,
            "negocios": negocios,
            "total": total,
            "seccion": "pedidos",
        },
    )


@requiere_admin
def pedido_detalle(request, id):
    pedido = api.get(request, f"/pedidos/{id}")
    return render(request, "panel/pedido_detalle.html", {"pedido": pedido, "seccion": "pedidos"})


@require_POST
@requiere_admin
def pedido_cancelar(request, id):
    api.post(request, f"/pedidos/{id}/cancelar")
    messages.success(request, f"Pedido #{id} cancelado y reembolsado")
    return redirect("pedido_detalle", id=id)
