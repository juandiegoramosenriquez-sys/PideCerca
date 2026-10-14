from functools import wraps

from django.contrib import messages
from django.shortcuts import redirect, render

from .api import ApiError, SesionExpirada


def requiere_admin(vista):
    @wraps(vista)
    def envoltura(request, *args, **kwargs):
        if not request.session.get("token"):
            return redirect("login")
        try:
            return vista(request, *args, **kwargs)
        except SesionExpirada as error:
            request.session.flush()
            messages.error(request, error.mensaje)
            return redirect("login")
        except ApiError as error:
            destino = request.META.get("HTTP_REFERER")
            if request.method == "POST" and destino:
                messages.error(request, error.mensaje)
                return redirect(destino)
            return render(request, "panel/error.html", {"mensaje": error.mensaje}, status=error.status)

    return envoltura
