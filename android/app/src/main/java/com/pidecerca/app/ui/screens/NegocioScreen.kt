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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pidecerca.app.data.ApiClient
import com.pidecerca.app.data.Formato
import com.pidecerca.app.data.Negocio
import com.pidecerca.app.data.Producto
import com.pidecerca.app.ui.AppViewModel
import com.pidecerca.app.ui.components.Aviso
import com.pidecerca.app.ui.components.BarraSuperior
import com.pidecerca.app.ui.components.BotonPrincipal
import com.pidecerca.app.ui.components.Cargando
import com.pidecerca.app.ui.components.MensajeVacio
import com.pidecerca.app.ui.components.Pastilla
import com.pidecerca.app.ui.components.SelectorCantidad
import com.pidecerca.app.ui.theme.LocalPaleta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegocioScreen(vm: AppViewModel, negocioId: Long, volver: () -> Unit, abrirCarrito: () -> Unit) {
    val p = LocalPaleta.current
    var negocio by remember { mutableStateOf<Negocio?>(null) }
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var seleccionado by remember { mutableStateOf<Producto?>(null) }
    var pendiente by remember { mutableStateOf<Pair<Producto, Int>?>(null) }

    LaunchedEffect(negocioId) {
        runCatching {
            negocio = ApiClient.api.negocio(negocioId)
            productos = ApiClient.api.productos(negocioId)
        }.onFailure { error = ApiClient.mensajeDeError(it) }
        cargando = false
    }

    fun agregar(producto: Producto, cantidad: Int) {
        val actual = negocio ?: return
        if (vm.esDeOtroNegocio(actual)) {
            pendiente = producto to cantidad
        } else {
            vm.agregar(actual, producto, cantidad)
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        BarraSuperior(negocio?.nombre ?: "Menú", alVolver = volver)
        Box(Modifier.weight(1f)) {
            when {
                cargando -> Cargando()
                negocio == null -> MensajeVacio("No pudimos abrir el negocio", error ?: "Inténtalo de nuevo.")
                else -> {
                    val n = negocio!!
                    val grupos = productos.groupBy { it.categoria?.nombre ?: "Otros" }
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Pastilla(if (n.abierto == true) "Abierto" else "Cerrado", if (n.abierto == true) p.listo else p.texto2)
                                    n.tiempoEstimado?.let { Pastilla("$it min", p.cocinando) }
                                    Formato.distancia(n.distanciaMetros)?.let { Pastilla(it, p.marca) }
                                }
                                n.descripcion?.let { Text(it, color = p.texto2) }
                                Text(listOfNotNull(n.direccion, n.horario).joinToString(" · "), color = p.texto3, fontSize = 13.sp)
                                if (n.abierto != true) {
                                    Aviso("Este negocio está cerrado ahora. Puedes ver el menú, pero no hacer pedidos.", p.nuevo)
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                        grupos.forEach { (categoria, lista) ->
                            item(key = "cat-$categoria") {
                                Text(categoria, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = p.texto, modifier = Modifier.padding(top = 8.dp))
                            }
                            items(lista, key = { it.id }) { producto ->
                                FilaProducto(producto, puedePedir = n.abierto == true, alTocar = { seleccionado = producto }) {
                                    agregar(producto, 1)
                                }
                            }
                        }
                        if (productos.isEmpty()) {
                            item { MensajeVacio("Menú vacío", "Este negocio todavía no tiene productos.") }
                        }
                    }
                }
            }

            if (vm.cantidadCarrito > 0 && vm.negocioCarrito?.id == negocioId) {
                Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp)) {
                    BarraCarrito(vm.cantidadCarrito, vm.totalCarrito, abrirCarrito)
                }
            }
        }
    }

    seleccionado?.let { producto ->
        var cantidad by remember(producto.id) { mutableIntStateOf(1) }
        val puede = negocio?.abierto == true && producto.disponible == true
        ModalBottomSheet(
            onDismissRequest = { seleccionado = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = p.tarjeta
        ) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!producto.imagen.isNullOrBlank()) {
                    AsyncImage(
                        model = producto.imagen,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))
                    )
                }
                Text(producto.nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = p.texto)
                producto.descripcion?.let { Text(it, color = p.texto2) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Formato.soles(producto.precio), color = p.marca, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    if (puede) SelectorCantidad(cantidad, { if (cantidad > 1) cantidad-- }, { if (cantidad < 50) cantidad++ })
                }
                BotonPrincipal(
                    texto = if (puede) "Agregar · ${Formato.soles(producto.precio * cantidad)}" else "No disponible",
                    habilitado = puede,
                    alTocar = {
                        agregar(producto, cantidad)
                        seleccionado = null
                    }
                )
            }
        }
    }

    pendiente?.let { (producto, cantidad) ->
        AlertDialog(
            onDismissRequest = { pendiente = null },
            title = { Text("¿Empezar un carrito nuevo?") },
            text = { Text("Tu carrito tiene productos de ${vm.negocioCarrito?.nombre}. Solo puedes pedir a un negocio a la vez.") },
            confirmButton = {
                TextButton(onClick = {
                    negocio?.let { vm.agregar(it, producto, cantidad) }
                    pendiente = null
                }) { Text("Vaciar y agregar", color = p.marca) }
            },
            dismissButton = { TextButton(onClick = { pendiente = null }) { Text("Cancelar", color = p.texto2) } },
            containerColor = p.tarjeta
        )
    }
}

@Composable
private fun FilaProducto(producto: Producto, puedePedir: Boolean, alTocar: () -> Unit, alAgregar: () -> Unit) {
    val p = LocalPaleta.current
    val disponible = producto.disponible == true
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(p.tarjeta)
            .clickable(onClick = alTocar)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(p.tarjeta2),
            contentAlignment = Alignment.Center
        ) {
            if (!producto.imagen.isNullOrBlank()) {
                AsyncImage(model = producto.imagen, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = p.texto3)
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(producto.nombre, fontWeight = FontWeight.SemiBold, color = if (disponible) p.texto else p.texto3)
            producto.descripcion?.let { Text(it, color = p.texto2, fontSize = 13.sp, maxLines = 2) }
            Text(
                if (disponible) Formato.soles(producto.precio) else "Agotado",
                color = if (disponible) p.marca else p.peligro,
                fontWeight = FontWeight.Bold
            )
        }
        if (disponible && puedePedir) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(p.marca).clickable(onClick = alAgregar),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = p.marcaTexto)
            }
        }
    }
}

@Composable
fun BarraCarrito(cantidad: Int, total: Double, alTocar: () -> Unit) {
    val p = LocalPaleta.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(p.marca)
            .clickable(onClick = alTocar)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = p.marcaTexto)
        Text("  Ver carrito · $cantidad", color = p.marcaTexto, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(Formato.soles(total), color = p.marcaTexto, fontWeight = FontWeight.Bold)
    }
}
