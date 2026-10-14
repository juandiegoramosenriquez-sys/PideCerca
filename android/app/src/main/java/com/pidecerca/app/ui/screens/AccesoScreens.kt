package com.pidecerca.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pidecerca.app.ui.AppViewModel
import com.pidecerca.app.ui.components.Aviso
import com.pidecerca.app.ui.components.BotonPrincipal
import com.pidecerca.app.ui.components.Campo
import com.pidecerca.app.ui.components.Logo
import com.pidecerca.app.ui.theme.LocalPaleta

@Composable
fun LoginScreen(vm: AppViewModel, irARegistro: () -> Unit) {
    val p = LocalPaleta.current
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        Logo(34)
        Text("Pide antes, recoge sin esperar", color = p.texto2)
        Spacer(Modifier.height(20.dp))
        Text("Inicia sesión", style = MaterialTheme.typography.headlineMedium, color = p.texto)
        error?.let { Aviso(it, p.peligro) }
        Campo(correo, { correo = it }, "Correo electrónico", tipo = KeyboardType.Email)
        Campo(password, { password = it }, "Contraseña", tipo = KeyboardType.Password, esPassword = true)
        BotonPrincipal("Iniciar sesión", cargando = cargando, alTocar = {
            if (correo.isBlank() || password.isBlank()) {
                error = "Escribe tu correo y tu contraseña"
                return@BotonPrincipal
            }
            cargando = true
            error = null
            vm.iniciarSesion(correo, password) {
                cargando = false
                error = it
            }
        })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?", color = p.texto2)
            TextButton(onClick = irARegistro) {
                Text("Regístrate", color = p.marca, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RegistroScreen(vm: AppViewModel, volver: () -> Unit) {
    val p = LocalPaleta.current
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Logo(28)
        Text("Crea tu cuenta", style = MaterialTheme.typography.headlineMedium, color = p.texto)
        error?.let { Aviso(it, p.peligro) }
        Campo(nombre, { nombre = it }, "Nombre")
        Campo(apellido, { apellido = it }, "Apellido")
        Campo(correo, { correo = it }, "Correo electrónico", tipo = KeyboardType.Email)
        Campo(telefono, { telefono = it }, "Celular", tipo = KeyboardType.Phone)
        Campo(password, { password = it }, "Contraseña (mínimo 6)", tipo = KeyboardType.Password, esPassword = true)
        Campo(confirmar, { confirmar = it }, "Confirmar contraseña", tipo = KeyboardType.Password, esPassword = true)
        BotonPrincipal("Registrarme", cargando = cargando, alTocar = {
            error = when {
                nombre.isBlank() -> "Escribe tu nombre"
                !correo.contains("@") -> "Escribe un correo válido"
                password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                password != confirmar -> "Las contraseñas no coinciden"
                else -> null
            }
            if (error != null) return@BotonPrincipal
            cargando = true
            vm.registrarse(nombre, apellido, correo, telefono, password) {
                cargando = false
                error = it
            }
        })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?", color = p.texto2)
            TextButton(onClick = volver) {
                Text("Inicia sesión", color = p.marca, fontWeight = FontWeight.Bold)
            }
        }
    }
}
