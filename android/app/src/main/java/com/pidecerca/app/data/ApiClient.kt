package com.pidecerca.app.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pidecerca.app.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {

    @Volatile
    var token: String? = null

    var alExpirarSesion: () -> Unit = {}

    private val cliente = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { cadena ->
            val peticion = cadena.request().newBuilder().apply {
                header("Accept", "application/json")
                token?.let { header("Authorization", "Bearer $it") }
            }.build()
            val respuesta = cadena.proceed(peticion)
            if (respuesta.code == 401 && token != null) {
                alExpirarSesion()
            }
            respuesta
        }
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    fun mensajeDeError(error: Throwable): String = when (error) {
        is HttpException -> {
            val cuerpo = error.response()?.errorBody()?.string()
            val mensaje = runCatching {
                Gson().fromJson(cuerpo, JsonObject::class.java)?.get("mensaje")?.asString
            }.getOrNull()
            mensaje ?: "Ocurrió un error (${error.code()})"
        }
        is IOException -> "No hay conexión con el servidor. Revisa tu internet"
        else -> error.message ?: "Ocurrió un error inesperado"
    }
}
