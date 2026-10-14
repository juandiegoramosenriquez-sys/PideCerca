package com.pidecerca.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first

private val Context.almacen by preferencesDataStore(name = "pidecerca")

class Preferencias(private val contexto: Context) {

    private val gson = Gson()

    private object Claves {
        val token = stringPreferencesKey("token")
        val usuario = stringPreferencesKey("usuario")
        val color = stringPreferencesKey("color")
        val modo = stringPreferencesKey("modo")
    }

    suspend fun leerSesion(): Pair<String, Usuario>? {
        val datos = contexto.almacen.data.first()
        val token = datos[Claves.token] ?: return null
        val usuario = datos[Claves.usuario]?.let {
            runCatching { gson.fromJson(it, Usuario::class.java) }.getOrNull()
        } ?: return null
        return token to usuario
    }

    suspend fun guardarSesion(token: String, usuario: Usuario) {
        contexto.almacen.edit {
            it[Claves.token] = token
            it[Claves.usuario] = gson.toJson(usuario)
        }
    }

    suspend fun guardarUsuario(usuario: Usuario) {
        contexto.almacen.edit { it[Claves.usuario] = gson.toJson(usuario) }
    }

    suspend fun borrarSesion() {
        contexto.almacen.edit {
            it.remove(Claves.token)
            it.remove(Claves.usuario)
        }
    }

    suspend fun leerTema(): Pair<String, String> {
        val datos = contexto.almacen.data.first()
        return (datos[Claves.color] ?: "morado") to (datos[Claves.modo] ?: "auto")
    }

    suspend fun guardarTema(color: String, modo: String) {
        contexto.almacen.edit {
            it[Claves.color] = color
            it[Claves.modo] = modo
        }
    }
}
