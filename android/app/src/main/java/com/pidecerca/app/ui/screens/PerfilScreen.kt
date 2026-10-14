package com.pidecerca.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pidecerca.app.data.ApiClient
import com.pidecerca.app.data.CambiarPasswordRequest
import com.pidecerca.app.data.PerfilRequest
import com.pidecerca.app.ui.AppViewModel
import com.pidecerca.app.ui.components.Avatar
import com.pidecerca.app.ui.components.Aviso
import com.pidecerca.app.ui.components.BarraSuperior
import com.pidecerca.app.ui.components.BotonSecundario
import com.pidecerca.app.ui.components.Campo
import com.pidecerca.app.ui.components.Tarjeta
import com.pidecerca.app.ui.theme.COLORES_MARCA
import com.pidecerca.app.ui.theme.LocalPaleta
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(vm: AppViewModel) {
    val p = LocalPaleta.current
    val usuario = vm.usuario
    val alcance = rememberCoroutineScope()
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var apellido by remember { mutableStateOf(usuario?.apellido ?: "") }
    var telefono by remember { mutableStateOf(usuario?.telefono ?: "") }
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BarraSuperior("Mi perfil")
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(
                    listOfNotNull(usuario?.nombre?.firstOrNull(), usuario?.apellido?.firstOrNull()).joinToString("").uppercase(),
                    56.dp
                )
                Column(Modifier.padding(start = 14.dp)) {
                    Text(usuario?.nombreCompleto ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = p.texto)
                    Text(usuario?.correo ?: "", color = p.texto2)
                }
            }

            mensaje?.let { (texto, ok) -> Aviso(texto, if (ok) p.listo else p.peligro) }

            Seccion("Apariencia")
            Tarjeta(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Color principal", color = p.texto2)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        COLORES_MARCA.forEach { (clave, color) ->
                            val activo = vm.colorMarca == clave
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color.oscuro)
                                    .border(if (activo) 3.dp else 0.dp, p.texto, CircleShape)
                                    .clickable { vm.cambiarTema(color = clave) }
                            )
                        }
                    }
                    Text("Modo de pantalla", color = p.texto2)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("auto" to "Automático", "oscuro" to "Oscuro", "claro" to "Claro").forEach { (clave, texto) ->
                            val activo = vm.modo == clave
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (activo) p.marca else p.tarjeta2)
                                    .clickable { vm.cambiarTema(nuevoModo = clave) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(texto, color = if (activo) p.marcaTexto else p.texto2, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Seccion("Mis datos")
            Campo(nombre, { nombre = it }, "Nombre")
            Campo(apellido, { apellido = it }, "Apellido")
            Campo(telefono, { telefono = it }, "Celular", tipo = KeyboardType.Phone)
            BotonSecundario("Guardar mis datos", {
                alcance.launch {
                    runCatching {
                        ApiClient.api.actualizarPerfil(PerfilRequest(nombre.trim(), apellido.trim().ifBlank { null }, telefono.trim().ifBlank { null }))
                    }
                        .onSuccess {
                            vm.actualizarUsuario(it)
                            mensaje = "Tus datos se guardaron" to true
                        }
                        .onFailure { mensaje = ApiClient.mensajeDeError(it) to false }
                }
            })

            Seccion("Cambiar contraseña")
            Campo(actual, { actual = it }, "Contraseña actual", tipo = KeyboardType.Password, esPassword = true)
            Campo(nueva, { nueva = it }, "Nueva contraseña", tipo = KeyboardType.Password, esPassword = true)
            BotonSecundario("Cambiar contraseña", {
                alcance.launch {
                    runCatching { ApiClient.api.cambiarPassword(CambiarPasswordRequest(actual, nueva)) }
                        .onSuccess {
                            actual = ""
                            nueva = ""
                            mensaje = "Contraseña actualizada" to true
                        }
                        .onFailure { mensaje = ApiClient.mensajeDeError(it) to false }
                }
            })

            Spacer(Modifier.height(8.dp))
            BotonSecundario("Cerrar sesión", { vm.cerrarSesion() }, color = p.peligro)
        }
    }
}

@Composable
private fun Seccion(texto: String) {
    Text(texto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LocalPaleta.current.texto, modifier = Modifier.padding(top = 8.dp))
}
