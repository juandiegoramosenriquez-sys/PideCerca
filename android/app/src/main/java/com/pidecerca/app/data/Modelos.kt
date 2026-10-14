package com.pidecerca.app.data

data class Usuario(
    val id: Long = 0,
    val nombre: String? = null,
    val apellido: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val rol: String? = null,
    val activo: Boolean? = null
) {
    val nombreCompleto: String
        get() = listOfNotNull(nombre, apellido).joinToString(" ")
}

data class LoginRequest(val correo: String, val password: String)

data class RegistroRequest(
    val nombre: String,
    val apellido: String?,
    val correo: String,
    val password: String,
    val telefono: String?
)

data class PerfilRequest(val nombre: String, val apellido: String?, val telefono: String?)

data class CambiarPasswordRequest(val passwordActual: String, val nuevaPassword: String)

data class LoginResponse(val token: String, val usuario: Usuario)

data class Negocio(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    val tipoComida: String? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val imagen: String? = null,
    val horario: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val tiempoEstimado: Int? = null,
    val abierto: Boolean? = null,
    val activo: Boolean? = null,
    val distanciaMetros: Double? = null
)

data class Categoria(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String? = null
)

data class Producto(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    val precio: Double = 0.0,
    val imagen: String? = null,
    val disponible: Boolean? = null,
    val categoria: Categoria? = null
)

data class DetallePedido(
    val id: Long = 0,
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0,
    val producto: Producto? = null
)

data class Pago(
    val id: Long = 0,
    val monto: Double = 0.0,
    val metodo: String? = null,
    val estado: String? = null
)

data class Pedido(
    val id: Long = 0,
    val fecha: String? = null,
    val total: Double = 0.0,
    val estado: String = "PENDIENTE",
    val tiempoPreparacion: Int? = null,
    val fechaAceptado: String? = null,
    val fechaListo: String? = null,
    val fechaRecogido: String? = null,
    val notas: String? = null,
    val motivoCancelacion: String? = null,
    val negocio: Negocio? = null,
    val detalles: List<DetallePedido> = emptyList(),
    val pago: Pago? = null
)

data class ItemPedidoRequest(val productoId: Long, val cantidad: Int)

data class CrearPedidoRequest(
    val negocioId: Long,
    val notas: String?,
    val metodoPago: String,
    val items: List<ItemPedidoRequest>
)

data class Notificacion(
    val id: Long = 0,
    val titulo: String? = null,
    val mensaje: String? = null,
    val leida: Boolean = false,
    val fecha: String? = null,
    val pedido: Pedido? = null
)

data class ConteoNoLeidas(val noLeidas: Long = 0)

data class ItemCarrito(val producto: Producto, val cantidad: Int)
