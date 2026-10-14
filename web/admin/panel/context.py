def admin_actual(request):
    return {"admin": request.session.get("usuario")}
