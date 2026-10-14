from django.urls import path

from . import views

urlpatterns = [
    path("", views.resumen, name="resumen"),
    path("login/", views.login_view, name="login"),
    path("salir/", views.logout_view, name="logout"),
    path("usuarios/", views.usuarios, name="usuarios"),
    path("usuarios/nuevo/", views.usuario_nuevo, name="usuario_nuevo"),
    path("usuarios/<int:id>/", views.usuario_editar, name="usuario_editar"),
    path("usuarios/<int:id>/activo/", views.usuario_activo, name="usuario_activo"),
    path("usuarios/<int:id>/eliminar/", views.usuario_eliminar, name="usuario_eliminar"),
    path("negocios/", views.negocios, name="negocios"),
    path("negocios/nuevo/", views.negocio_nuevo, name="negocio_nuevo"),
    path("negocios/<int:id>/", views.negocio_editar, name="negocio_editar"),
    path("negocios/<int:id>/estado/", views.negocio_estado, name="negocio_estado"),
    path("negocios/<int:id>/eliminar/", views.negocio_eliminar, name="negocio_eliminar"),
    path("categorias/", views.categorias, name="categorias"),
    path("categorias/<int:id>/eliminar/", views.categoria_eliminar, name="categoria_eliminar"),
    path("pedidos/", views.pedidos, name="pedidos"),
    path("pedidos/<int:id>/", views.pedido_detalle, name="pedido_detalle"),
    path("pedidos/<int:id>/cancelar/", views.pedido_cancelar, name="pedido_cancelar"),
]
