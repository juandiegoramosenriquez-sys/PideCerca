from datetime import datetime

from django import template

register = template.Library()

ESTADOS = {
    "PENDIENTE": ("Nuevo", "nuevo"),
    "ACEPTADO": ("Aceptado", "cocinando"),
    "EN_PREPARACION": ("En preparación", "cocinando"),
    "LISTO_PARA_RECOGER": ("Listo para recoger", "listo"),
    "RECOGIDO": ("Recogido", "entregado"),
    "CANCELADO": ("Cancelado", "cancelado"),
}

ROLES = {
    "ADMIN": ("Administrador", "marca"),
    "NEGOCIO": ("Negocio", "cocinando"),
    "CLIENTE": ("Cliente", "entregado"),
}

METODOS = {"YAPE": "Yape", "PLIN": "Plin", "TARJETA": "Tarjeta", "EFECTIVO": "Efectivo"}


@register.filter
def soles(valor):
    try:
        return f"S/ {float(valor or 0):,.2f}"
    except (TypeError, ValueError):
        return "S/ 0.00"


@register.filter
def estado_texto(estado):
    return ESTADOS.get(estado, (estado, "entregado"))[0]


@register.filter
def estado_clase(estado):
    return ESTADOS.get(estado, (estado, "entregado"))[1]


@register.filter
def rol_texto(rol):
    return ROLES.get(rol, (rol, "entregado"))[0]


@register.filter
def rol_clase(rol):
    return ROLES.get(rol, (rol, "entregado"))[1]


@register.filter
def metodo_pago(metodo):
    return METODOS.get(metodo, metodo or "-")


@register.filter
def fecha(valor):
    if not valor:
        return ""
    try:
        momento = datetime.fromisoformat(str(valor))
    except ValueError:
        return valor
    meses = ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "set", "oct", "nov", "dic"]
    return f"{momento.day} {meses[momento.month - 1]} {momento.year} · {momento:%H:%M}"


@register.filter
def iniciales(nombre):
    partes = [p for p in str(nombre or "").split() if p]
    return "".join(p[0].upper() for p in partes[:2])


@register.filter
def obtener(diccionario, clave):
    if isinstance(diccionario, dict):
        return diccionario.get(clave, 0)
    return 0


@register.filter
def porcentaje(valor, total):
    try:
        total = float(total)
        return 0 if total <= 0 else round(float(valor) * 100 / total)
    except (TypeError, ValueError):
        return 0
