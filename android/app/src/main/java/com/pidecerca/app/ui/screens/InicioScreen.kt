package com.pidecerca.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pidecerca.app.data.ApiClient
import com.pidecerca.app.data.Formato
import com.pidecerca.app.data.Negocio
import com.pidecerca.app.data.Ubicacion
import com.pidecerca.app.ui.AppViewModel
import com.pidecerca.app.ui.components.Aviso
import com.pidecerca.app.ui.components.Cargando
import com.pidecerca.app.ui.components.MensajeVacio
import com.pidecerca.app.ui.components.Pastilla
import com.pidecerca.app.ui.components.Tarjeta
import com.pidecerca.app.ui.theme.LocalPaleta
import kotlinx.coroutines.delay

@Composable
fun InicioScreen(vm: AppViewModel, abrirNegocio: (Long) -> Unit) {
    val p = LocalPaleta.current
    val contexto = LocalContext.current
    var negocios by remember { mutableStateOf<List<Negocio>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf<String?>(null) }
    var tienePermiso by remember { mutableStateOf(Ubicacion.tienePermiso(contexto)) }
    var recargar by remember { mutableStateOf(0) }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        tienePermiso = Ubicacion.tienePermiso(contexto)
        recargar++
    }

    LaunchedEffect(Unit) {
        if (!tienePermiso) {
            pedirPermiso.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    val ubicacion = remember(tienePermiso, recargar) { Ubicacion.ultima(contexto) }
    val usandoAproximada = ubicacion == null

    LaunchedEffect(busqueda, recargar, tienePermiso) {
        delay(if (busqueda.isBlank()) 0 else 400)
        cargando = negocios.isEmpty()
        runCatching {
            ApiClient.api.negocios(
                texto = busqueda.ifBlank { null },
                tipo = null,
                latitud = ubicacion?.latitude ?: Ubicacion.LATITUD_POR_DEFECTO,
                longitud = ubicacion?.longitude ?: Ubicacion.LONGITUD_POR_DEFECTO
            )
        }
            .onSuccess {
                negocios = it
                error = null
            }
            .onFailure { error = ApiClient.mensajeDeError(it) }
        cargando = false
    }

    val tipos = negocios.mapNotNull { it.tipoComida }.distinct()
    val visibles = negocios.filter { tipo == null || it.tipoComida == tipo }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Hola, ${vm.usuario?.nombre ?: ""}", color = p.texto2)
                    Text("¿Qué se te antoja hoy?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = p.texto)
                }
                IconButton(onClick = { recargar++ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = p.texto2)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = p.marca, modifier = Modifier.size(16.dp))
                Text(
                    if (usandoAproximada) " Ubicación aproximada · Lima" else " Cerca de ti",
                    color = p.texto2,
                    fontSize = 13.sp
                )
            }
        }
        item {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar hamburguesas, pollo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = p.marca,
                    unfocusedBorderColor = p.linea,
                    focusedContainerColor = p.tarjeta,
                    unfocusedContainerColor = p.tarjeta
                )
            )
        }
        if (tipos.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Chip("Todo", tipo == null) { tipo = null } }
                    items(tipos) { t -> Chip(t, tipo == t) { tipo = if (tipo == t) null else t } }
                }
            }
        }
        error?.let { item { Aviso(it, p.peligro) } }
        if (cargando) {
            item { Box(Modifier.fillMaxWidth().height(200.dp)) { Cargando() } }
        } else if (visibles.isEmpty()) {
            item { MensajeVacio("No encontramos negocios", "Prueba con otra búsqueda o vuelve más tarde.") }
        }
        items(visibles, key = { it.id }) { negocio ->
            TarjetaNegocio(negocio) { abrirNegocio(negocio.id) }
        }
    }
}

@Composable
private fun Chip(texto: String, activo: Boolean, alTocar: () -> Unit) {
    val p = LocalPaleta.current
    Box(
        Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (activo) p.marca else p.tarjeta)
            .clickable(onClick = alTocar)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(texto, color = if (activo) p.marcaTexto else p.texto2, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TarjetaNegocio(negocio: Negocio, alTocar: () -> Unit) {
    val p = LocalPaleta.current
    val abierto = negocio.abierto == true
    Tarjeta(alTocar = alTocar, relleno = 0.dp) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(110.dp).background(p.tarjeta2),
                contentAlignment = Alignment.Center
            ) {
                if (!negocio.imagen.isNullOrBlank()) {
                    AsyncImage(
                        model = negocio.imagen,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = p.texto3, modifier = Modifier.size(32.dp))
                }
                Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                    Pastilla(if (abierto) "Abierto" else "Cerrado", if (abierto) p.listo else p.texto2)
                }
            }
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        negocio.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = p.texto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Formato.distancia(negocio.distanciaMetros)?.let {
                        Text(it, color = p.marca, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    listOfNotNull(negocio.tipoComida, negocio.tiempoEstimado?.let { "$it min" }).joinToString(" · "),
                    color = p.texto2
                )
                negocio.direccion?.let {
                    Text(it, color = p.texto3, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
