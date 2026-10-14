package com.pidecerca.app.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/registro")
    suspend fun registro(@Body request: RegistroRequest): LoginResponse

    @GET("auth/me")
    suspend fun yo(): Usuario

    @PUT("usuarios/me")
    suspend fun actualizarPerfil(@Body request: PerfilRequest): Usuario

    @PUT("usuarios/me/password")
    suspend fun cambiarPassword(@Body request: CambiarPasswordRequest)

    @GET("negocios")
    suspend fun negocios(
        @Query("q") texto: String?,
        @Query("tipo") tipo: String?,
        @Query("lat") latitud: Double?,
        @Query("lng") longitud: Double?
    ): List<Negocio>

    @GET("negocios/{id}")
    suspend fun negocio(@Path("id") id: Long): Negocio

    @GET("productos/negocio/{id}")
    suspend fun productos(@Path("id") negocioId: Long, @Query("soloDisponibles") soloDisponibles: Boolean = false): List<Producto>

    @POST("pedidos")
    suspend fun crearPedido(@Body request: CrearPedidoRequest): Pedido

    @GET("pedidos/mios")
    suspend fun misPedidos(): List<Pedido>

    @GET("pedidos/{id}")
    suspend fun pedido(@Path("id") id: Long): Pedido

    @POST("pedidos/{id}/cancelar")
    suspend fun cancelarPedido(@Path("id") id: Long): Pedido

    @GET("notificaciones")
    suspend fun notificaciones(): List<Notificacion>

    @GET("notificaciones/no-leidas")
    suspend fun noLeidas(): ConteoNoLeidas

    @PUT("notificaciones/{id}/leer")
    suspend fun leer(@Path("id") id: Long): Notificacion

    @PUT("notificaciones/leer-todas")
    suspend fun leerTodas()
}
