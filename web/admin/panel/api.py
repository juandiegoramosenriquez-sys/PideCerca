import requests
from django.conf import settings


class ApiError(Exception):
    def __init__(self, mensaje, status=400):
        super().__init__(mensaje)
        self.mensaje = mensaje
        self.status = status


class SesionExpirada(ApiError):
    pass


def llamar(request, metodo, ruta, datos=None, params=None, token=None):
    cabeceras = {"Accept": "application/json"}
    token = token or (request.session.get("token") if request is not None else None)
    if token:
        cabeceras["Authorization"] = f"Bearer {token}"

    try:
        respuesta = requests.request(
            metodo,
            settings.PIDECERCA_API_URL + ruta,
            json=datos,
            params=params,
            headers=cabeceras,
            timeout=15,
        )
    except requests.RequestException:
        raise ApiError(
            "No se pudo conectar con el backend de PideCerca. Verifica que esté encendido",
            503,
        )

    if respuesta.status_code == 401 and token:
        raise SesionExpirada("Tu sesión expiró. Vuelve a iniciar sesión", 401)

    if respuesta.status_code == 204 or not respuesta.content:
        contenido = None
    else:
        try:
            contenido = respuesta.json()
        except ValueError:
            contenido = None

    if not respuesta.ok:
        mensaje = (contenido or {}).get("mensaje") if isinstance(contenido, dict) else None
        raise ApiError(mensaje or "El backend respondió con un error", respuesta.status_code)

    return contenido


def get(request, ruta, params=None):
    return llamar(request, "GET", ruta, params=params)


def post(request, ruta, datos=None, params=None):
    return llamar(request, "POST", ruta, datos=datos, params=params)


def put(request, ruta, datos=None, params=None):
    return llamar(request, "PUT", ruta, datos=datos, params=params)


def delete(request, ruta):
    return llamar(request, "DELETE", ruta)
