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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pidecerca.app.data.Formato
import com.pidecerca.app.ui.AppViewModel
import com.pidecerca.app.ui.components.Aviso
import com.pidecerca.app.ui.components.BarraSuperior
import com.pidecerca.app.ui.components.BotonPrincipal
import com.pidecerca.app.ui.components.BotonSecundario
import com.pidecerca.app.ui.components.Campo
import com.pidecerca.app.ui.components.MensajeVacio
import com.pidecerca.app.ui.components.SelectorCantidad
import com.pidecerca.app.ui.components.Tarjeta
import com.pidecerca.app.ui.theme.LocalPaleta

@Composable
fun CarritoScreen(vm: AppViewModel, notas: String, alCambiarNotas: (String) -> Unit, volver: () -> Unit, irAPagar: () -> Unit, irAInicio: () -> Unit) {
    val p = LocalPaleta.current
    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        BarraSuperior("Tu carrito", alVolver = volver)
        if (vm.carrito.isEmpty()) {
            MensajeVacio("Tu carrito está vacío", "Elige un negocio cercano y agrega lo que se te antoje.") {
                BotonPrincipal("Ver negocios", irAInicio)
            }
            return@Column
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(vm.negocioCarrito?.nombre ?: "", color = p.texto2, fontWeight = FontWeight.SemiBold)
            vm.carrito.forEach { item ->
                Tarjeta(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.producto.nombre, fontWeight = FontWeight.SemiBold, color = p.texto)
                            Text(Formato.soles(item.producto.precio * item.cantidad), color = p.marca, fontWeight = FontWeight.Bold)
                        }
                        SelectorCantidad(
                            item.cantidad,
                            { vm.cambiarCantidad(item.producto.id, -1) },
                            { vm.cambiarCantidad(item.producto.id, 1) }
                        )
                        IconButton(onClick = { vm.quitar(item.producto.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = p.peligro)
                        }
                    }
                }
            }
            Campo(notas, alCambiarNotas, "Nota para el negocio (opcional)", lineas = 2)
            Tarjeta(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Fila("Productos", "${vm.cantidadCarrito}")
                    Fila("Subtotal", Formato.soles(vm.totalCarrito))
                    Fila("Recojo en el local", "Gratis")
                    Row {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = p.texto, modifier = Modifier.weight(1f))
                        Text(Formato.soles(vm.totalCarrito), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = p.texto)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Column(Modifier.navigationBarsPadding().padding(16.dp)) {
            BotonPrincipal("Continuar al pago · ${Formato.soles(vm.totalCarrito)}", irAPagar)
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    val p = LocalPaleta.current
    Row {
        Text(etiqueta, color = p.texto2, modifier = Modifier.weight(1f))
        Text(valor, color = p.texto)
    }
}

private val METODOS = listOf(
    Triple("YAPE", "Yape", "Paga con tu celular"),
    Triple("PLIN", "Plin", "Paga con tu celular"),
    Triple("TARJETA", "Tarjeta", "Débito o crédito"),
    Triple("EFECTIVO", "Efectivo", "Paga al recoger")
)

@Composable
fun PagoScreen(vm: AppViewModel, notas: String, volver: () -> Unit, alConfirmar: (Long) -> Unit) {
    val p = LocalPaleta.current
    var metodo by remember { mutableStateOf("YAPE") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        BarraSuperior("Pago", alVolver = volver)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("¿Cómo quieres pagar?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = p.texto)
            METODOS.forEach { (clave, nombre, detalle) ->
                val activo = metodo == clave
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (activo) p.marca.copy(alpha = 0.12f) else p.tarjeta)
                        .border(if (activo) 2.dp else 1.dp, if (activo) p.marca else p.linea, RoundedCornerShape(16.dp))
                        .clickable { metodo = clave }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(22.dp).clip(CircleShape).border(2.dp, if (activo) p.marca else p.texto3, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activo) Box(Modifier.size(12.dp).clip(CircleShape).background(p.marca))
                    }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(nombre, fontWeight = FontWeight.Bold, color = p.texto)
                        Text(detalle, color = p.texto2, fontSize = 13.sp)
                    }
                }
            }
            Aviso("Pago de demostración: no se cobra dinero real. El pedido se registra como pagado.", p.cocinando)
            Tarjeta(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(vm.negocioCarrito?.nombre ?: "", fontWeight = FontWeight.Bold, color = p.texto)
                    vm.carrito.forEach {
                        Text("${it.cantidad}x ${it.producto.nombre}", color = p.texto2)
                    }
                    if (notas.isNotBlank()) Text("Nota: $notas", color = p.texto3, fontSize = 13.sp)
                }
            }
            error?.let { Aviso(it, p.peligro) }
        }
        Column(Modifier.navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BotonPrincipal("Pagar ${Formato.soles(vm.totalCarrito)}", cargando = cargando, alTocar = {
                cargando = true
                error = null
                vm.confirmarPedido(metodo, notas) { pedido, mensaje ->
                    cargando = false
                    if (pedido != null) alConfirmar(pedido.id) else error = mensaje
                }
            })
            BotonSecundario("Volver al carrito", volver)
        }
    }
}
