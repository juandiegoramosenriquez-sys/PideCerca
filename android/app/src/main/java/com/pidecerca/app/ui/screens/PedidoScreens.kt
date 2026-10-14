package com.pidecerca.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pidecerca.app.data.ApiClient
import com.pidecerca.app.data.Formato
import com.pidecerca.app.data.Notificacion
import com.pidecerca.app.data.Pedido
import com.pidecerca.app.ui.AppViewModel
import com.pidecerca.app.ui.components.Aviso
import com.pidecerca.app.ui.components.BarraSuperior
import com.pidecerca.app.ui.components.BotonSecundario
import com.pidecerca.app.ui.components.Cargando
import com.pidecerca.app.ui.components.ColorEstado
import com.pidecerca.app.ui.components.IconoCirculo
import com.pidecerca.app.ui.components.MensajeVacio
import com.pidecerca.app.ui.components.Pastilla
import com.pidecerca.app.ui.components.Tarjeta
import com.pidecerca.app.ui.theme.LocalPaleta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PedidoScreen(vm: AppViewModel, pedidoId: Long, volver: () -> Unit) {
    val p = LocalPaleta.current
    var pedido by remember { mutableStateOf<Pedido?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmarCancelacion by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()

    LaunchedEffect(pedidoId, vm.versionPedidos) {
        while (true) {
            runCatching { ApiClient.api.pedido(pedidoId) }
                .onSuccess {
                    pedido = it
                    error = null
                }
                .onFailure { error = ApiClient.mensajeDeError(it) }
            if (pedido != null && !Formato.esActivo(pedido!!.estado)) break
            delay(8_000)
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        BarraSuperior("Tu pedido", alVolver = volver)
        val actual = pedido
        if (actual == null) {
            if (error != null) MensajeVacio("No pudimos cargar el pedido", error!!) else Cargando()
            return@Column
        }
        val color = ColorEstado(actual.estado)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Tarjeta(Modifier.fillMaxWidth(), borde = color) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Número de pedido", color = p.texto2)
                    Text("#${actual.id}", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = p.texto)
                    Pastilla(Formato.estadoTexto(actual.estado), color)
                    Text(
                        textoEstado(actual),
                        color = p.texto2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (actual.estado != "CANCELADO") {
                Pasos(Formato.pasoEstado(actual.estado))
            } else {
                Aviso("Pedido cancelado${actual.motivoCancelacion?.let { ": $it" } ?: ""}. Se devolverá tu dinero.", p.peligro)
            }

            Tarjeta(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(actual.negocio?.nombre ?: "", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = p.texto)
                    actual.negocio?.direccion?.let { Text(it, color = p.texto2) }
                    actual.negocio?.telefono?.let { Text("Tel. $it", color = p.texto3, fontSize = 13.sp) }
                }
            }

            Tarjeta(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    actual.detalles.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(p.tarjeta2),
                                contentAlignment = Alignment.Center
                            ) { Text("${it.cantidad}", fontWeight = FontWeight.Bold, color = p.texto) }
                            Text(it.producto?.nombre ?: "", color = p.texto, modifier = Modifier.weight(1f).padding(start = 10.dp))
                            Text(Formato.soles(it.subtotal), color = p.texto2)
                        }
                    }
                    actual.notas?.let { Text("Nota: $it", color = p.texto3, fontSize = 13.sp) }
                    Row {
                        Text("Total · ${Formato.metodoPago(actual.pago?.metodo)}", fontWeight = FontWeight.Bold, color = p.texto, modifier = Modifier.weight(1f))
                        Text(Formato.soles(actual.total), fontWeight = FontWeight.Bold, color = p.texto)
                    }
                }
            }
            error?.let { Aviso(it, p.peligro) }
            Spacer(Modifier.height(8.dp))
        }
        if (actual.estado == "PENDIENTE") {
            Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                BotonSecundario("Cancelar pedido", { confirmarCancelacion = true }, color = p.peligro)
            }
        }
    }

    if (confirmarCancelacion) {
        AlertDialog(
            onDismissRequest = { confirmarCancelacion = false },
            title = { Text("¿Cancelar el pedido?") },
            text = { Text("Solo puedes cancelar mientras el negocio no lo acepte. Te devolveremos tu dinero.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarCancelacion = false
                    alcance.launch {
                        runCatching { ApiClient.api.cancelarPedido(pedidoId) }
                            .onSuccess { pedido = it }
                            .onFailure { error = ApiClient.mensajeDeError(it) }
                    }
                }) { Text("Sí, cancelar", color = p.peligro) }
            },
            dismissButton = { TextButton(onClick = { confirmarCancelacion = false }) { Text("No", color = p.texto2) } },
            containerColor = p.tarjeta
        )
    }
}

private fun textoEstado(pedido: Pedido): String = when (pedido.estado) {
    "PENDIENTE" -> "Esperando que el negocio acepte tu pedido"
    "ACEPTADO", "EN_PREPARACION" -> {
        val faltan = Formato.minutosRestantes(pedido)
        when {
            faltan == null -> "Tu pedido se está preparando"
            faltan > 0 -> "Estará listo en unos $faltan min"
            else -> "Ya casi está listo"
        }
    }
    "LISTO_PARA_RECOGER" -> "Acércate al negocio y di tu número de pedido"
    "RECOGIDO" -> "¡Buen provecho! Gracias por usar PideCerca"
    else -> ""
}

@Composable
private fun Pasos(paso: Int) {
    val p = LocalPaleta.current
    val etiquetas = listOf("Enviado", "Preparando", "Listo", "Recogido")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        etiquetas.forEachIndexed { i, etiqueta ->
            val hecho = i <= paso
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(3.dp).background(if (i == 0) Color0 else if (hecho) p.marca else p.linea))
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(if (hecho) p.marca else p.tarjeta2),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hecho) Icon(Icons.Default.Check, contentDescription = null, tint = p.marcaTexto, modifier = Modifier.size(16.dp))
                    }
                    Box(Modifier.weight(1f).height(3.dp).background(if (i == etiquetas.lastIndex) Color0 else if (i < paso) p.marca else p.linea))
                }
                Text(etiqueta, fontSize = 12.sp, color = if (hecho) p.texto else p.texto3, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

private val Color0 = androidx.compose.ui.graphics.Color.Transparent

@Composable
fun MisPedidosScreen(vm: AppViewModel, abrirPedido: (Long) -> Unit) {
    val p = LocalPaleta.current
    var pedidos by remember { mutableStateOf<List<Pedido>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vm.versionPedidos) {
        runCatching { ApiClient.api.misPedidos() }
            .onSuccess {
                pedidos = it
                error = null
            }
            .onFailure { error = ApiClient.mensajeDeError(it) }
    }

    val lista = pedidos
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Mis pedidos")
        when {
            lista == null && error == null -> Cargando()
            lista.isNullOrEmpty() -> MensajeVacio("Aún no tienes pedidos", error ?: "Cuando hagas tu primer pedido aparecerá aquí.")
            else -> {
                val activos = lista.filter { Formato.esActivo(it.estado) }
                val anteriores = lista.filterNot { Formato.esActivo(it.estado) }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activos.isNotEmpty()) {
                        item { Titulo("En curso") }
                        items(activos, key = { it.id }) { TarjetaPedido(it) { abrirPedido(it.id) } }
                    }
                    if (anteriores.isNotEmpty()) {
                        item { Titulo("Historial") }
                        items(anteriores, key = { it.id }) { TarjetaPedido(it) { abrirPedido(it.id) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LocalPaleta.current.texto, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun TarjetaPedido(pedido: Pedido, alTocar: () -> Unit) {
    val p = LocalPaleta.current
    val color = ColorEstado(pedido.estado)
    Tarjeta(Modifier.fillMaxWidth(), borde = if (Formato.esActivo(pedido.estado)) color else null, alTocar = alTocar) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${pedido.id}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = p.texto)
                Spacer(Modifier.width(10.dp))
                Text(pedido.negocio?.nombre ?: "", fontWeight = FontWeight.SemiBold, color = p.texto, modifier = Modifier.weight(1f), maxLines = 1)
                Pastilla(Formato.estadoTexto(pedido.estado), color)
            }
            Text(pedido.detalles.joinToString(", ") { "${it.cantidad}x ${it.producto?.nombre}" }, color = p.texto2, maxLines = 2)
            Row {
                Text(Formato.fechaCorta(pedido.fecha), color = p.texto3, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text(Formato.soles(pedido.total), fontWeight = FontWeight.Bold, color = p.texto)
            }
        }
    }
}

@Composable
fun AvisosScreen(vm: AppViewModel, abrirPedido: (Long) -> Unit) {
    val p = LocalPaleta.current
    var lista by remember { mutableStateOf<List<Notificacion>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableStateOf(0) }
    val alcance = rememberCoroutineScope()

    LaunchedEffect(recargar, vm.noLeidas) {
        runCatching { ApiClient.api.notificaciones() }
            .onSuccess { lista = it; error = null }
            .onFailure { error = ApiClient.mensajeDeError(it) }
    }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Avisos") {
            if (lista?.any { !it.leida } == true) {
                TextButton(onClick = {
                    alcance.launch {
                        runCatching { ApiClient.api.leerTodas() }
                        vm.refrescarNoLeidas()
                        recargar++
                    }
                }) { Text("Marcar leídas", color = p.marca) }
            }
        }
        val actual = lista
        when {
            actual == null && error == null -> Cargando()
            actual.isNullOrEmpty() -> MensajeVacio("Sin avisos", error ?: "Aquí te contaremos cuando tu pedido avance.")
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(actual, key = { it.id }) { n ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (n.leida) p.tarjeta else p.marca.copy(alpha = 0.10f))
                            .clickable {
                                alcance.launch {
                                    if (!n.leida) runCatching { ApiClient.api.leer(n.id) }
                                    vm.refrescarNoLeidas()
                                    recargar++
                                }
                                n.pedido?.id?.let(abrirPedido)
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconoCirculo(Icons.Default.Notifications, if (n.leida) p.texto3 else p.marca)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(n.titulo ?: "PideCerca", fontWeight = FontWeight.Bold, color = p.texto)
                            n.mensaje?.let { Text(it, color = p.texto2) }
                            Text(Formato.haceCuanto(n.fecha), color = p.texto3, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
