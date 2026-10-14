package com.pidecerca.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pidecerca.app.data.ApiClient
import com.pidecerca.app.data.Avisos
import com.pidecerca.app.data.CrearPedidoRequest
import com.pidecerca.app.data.ItemCarrito
import com.pidecerca.app.data.ItemPedidoRequest
import com.pidecerca.app.data.LoginRequest
import com.pidecerca.app.data.Negocio
import com.pidecerca.app.data.Pedido
import com.pidecerca.app.data.Preferencias
import com.pidecerca.app.data.Producto
import com.pidecerca.app.data.RegistroRequest
import com.pidecerca.app.data.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val preferencias = Preferencias(aplicacion)
    private var vigilancia: Job? = null
    private val avisados = mutableSetOf<Long>()
    private var primeraRevision = true

    var iniciando by mutableStateOf(true)
        private set
    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var colorMarca by mutableStateOf("morado")
        private set
    var modo by mutableStateOf("auto")
        private set
    var negocioCarrito by mutableStateOf<Negocio?>(null)
        private set
    var carrito by mutableStateOf<List<ItemCarrito>>(emptyList())
        private set
    var noLeidas by mutableStateOf(0L)
        private set
    var versionPedidos by mutableStateOf(0)
        private set

    val cantidadCarrito: Int get() = carrito.sumOf { it.cantidad }
    val totalCarrito: Double get() = carrito.sumOf { it.producto.precio * it.cantidad }

    init {
        ApiClient.alExpirarSesion = { viewModelScope.launch { cerrarSesion() } }
        viewModelScope.launch {
            val (color, modoGuardado) = preferencias.leerTema()
            colorMarca = color
            modo = modoGuardado
            val sesion = preferencias.leerSesion()
            if (sesion != null) {
                ApiClient.token = sesion.first
                usuario = sesion.second
                runCatching { ApiClient.api.yo() }
                    .onSuccess {
                        usuario = it
                        preferencias.guardarUsuario(it)
                    }
                iniciarVigilancia()
            }
            iniciando = false
        }
    }

    fun iniciarSesion(correo: String, password: String, alTerminar: (String?) -> Unit) {
        viewModelScope.launch {
            runCatching { ApiClient.api.login(LoginRequest(correo.trim(), password)) }
                .onSuccess { respuesta ->
                    if (respuesta.usuario.rol != "CLIENTE") {
                        alTerminar(
                            if (respuesta.usuario.rol == "NEGOCIO") "Esta app es para clientes. Los negocios usan la web de PideCerca"
                            else "Esta app es para clientes. El administrador usa el panel web"
                        )
                    } else {
                        guardarSesion(respuesta.token, respuesta.usuario)
                        alTerminar(null)
                    }
                }
                .onFailure { alTerminar(ApiClient.mensajeDeError(it)) }
        }
    }

    fun registrarse(
        nombre: String,
        apellido: String,
        correo: String,
        telefono: String,
        password: String,
        alTerminar: (String?) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.registro(
                    RegistroRequest(
                        nombre = nombre.trim(),
                        apellido = apellido.trim().ifBlank { null },
                        correo = correo.trim(),
                        password = password,
                        telefono = telefono.trim().ifBlank { null }
                    )
                )
            }
                .onSuccess {
                    guardarSesion(it.token, it.usuario)
                    alTerminar(null)
                }
                .onFailure { alTerminar(ApiClient.mensajeDeError(it)) }
        }
    }

    private suspend fun guardarSesion(token: String, nuevoUsuario: Usuario) {
        ApiClient.token = token
        preferencias.guardarSesion(token, nuevoUsuario)
        usuario = nuevoUsuario
        primeraRevision = true
        avisados.clear()
        iniciarVigilancia()
    }

    fun actualizarUsuario(nuevo: Usuario) {
        usuario = nuevo
        viewModelScope.launch { preferencias.guardarUsuario(nuevo) }
    }

    fun cerrarSesion() {
        vigilancia?.cancel()
        ApiClient.token = null
        usuario = null
        noLeidas = 0
        vaciarCarrito()
        viewModelScope.launch { preferencias.borrarSesion() }
    }

    fun cambiarTema(color: String = colorMarca, nuevoModo: String = modo) {
        colorMarca = color
        modo = nuevoModo
        viewModelScope.launch { preferencias.guardarTema(color, nuevoModo) }
    }

    fun esDeOtroNegocio(negocio: Negocio): Boolean =
        negocioCarrito != null && negocioCarrito?.id != negocio.id && carrito.isNotEmpty()

    fun agregar(negocio: Negocio, producto: Producto, cantidad: Int = 1) {
        if (esDeOtroNegocio(negocio)) vaciarCarrito()
        negocioCarrito = negocio
        val existente = carrito.find { it.producto.id == producto.id }
        carrito = if (existente != null) {
            carrito.map {
                if (it.producto.id == producto.id) it.copy(cantidad = (it.cantidad + cantidad).coerceAtMost(50)) else it
            }
        } else {
            carrito + ItemCarrito(producto, cantidad)
        }
    }

    fun cambiarCantidad(productoId: Long, cambio: Int) {
        carrito = carrito.mapNotNull {
            if (it.producto.id != productoId) it
            else {
                val nueva = it.cantidad + cambio
                if (nueva <= 0) null else it.copy(cantidad = nueva.coerceAtMost(50))
            }
        }
        if (carrito.isEmpty()) negocioCarrito = null
    }

    fun quitar(productoId: Long) {
        carrito = carrito.filterNot { it.producto.id == productoId }
        if (carrito.isEmpty()) negocioCarrito = null
    }

    fun vaciarCarrito() {
        carrito = emptyList()
        negocioCarrito = null
    }

    fun confirmarPedido(metodoPago: String, notas: String, alTerminar: (Pedido?, String?) -> Unit) {
        val negocio = negocioCarrito ?: return alTerminar(null, "Tu carrito está vacío")
        viewModelScope.launch {
            runCatching {
                ApiClient.api.crearPedido(
                    CrearPedidoRequest(
                        negocioId = negocio.id,
                        notas = notas.trim().ifBlank { null },
                        metodoPago = metodoPago,
                        items = carrito.map { ItemPedidoRequest(it.producto.id, it.cantidad) }
                    )
                )
            }
                .onSuccess {
                    vaciarCarrito()
                    versionPedidos++
                    alTerminar(it, null)
                }
                .onFailure { alTerminar(null, ApiClient.mensajeDeError(it)) }
        }
    }

    fun refrescarNoLeidas() {
        viewModelScope.launch { revisarNotificaciones() }
    }

    private fun iniciarVigilancia() {
        vigilancia?.cancel()
        vigilancia = viewModelScope.launch {
            while (isActive && usuario != null) {
                revisarNotificaciones()
                delay(10_000)
            }
        }
    }

    private suspend fun revisarNotificaciones() {
        if (usuario == null) return
        val conteo = runCatching { ApiClient.api.noLeidas() }.getOrNull() ?: return
        val anterior = noLeidas
        noLeidas = conteo.noLeidas
        if (conteo.noLeidas == 0L) {
            primeraRevision = false
            return
        }
        if (!primeraRevision && conteo.noLeidas <= anterior) return
        val lista = runCatching { ApiClient.api.notificaciones() }.getOrNull() ?: return
        val pendientes = lista.filter { !it.leida && it.id !in avisados }
        if (!primeraRevision) {
            pendientes.take(3).forEach { Avisos.mostrar(getApplication(), it) }
            if (pendientes.isNotEmpty()) versionPedidos++
        }
        avisados.addAll(pendientes.map { it.id })
        primeraRevision = false
    }
}
