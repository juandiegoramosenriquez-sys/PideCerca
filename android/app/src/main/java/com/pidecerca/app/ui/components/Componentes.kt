package com.pidecerca.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pidecerca.app.ui.theme.LocalPaleta

@Composable
fun Logo(tamano: Int = 22) {
    val p = LocalPaleta.current
    Row {
        Text("Pide", fontSize = tamano.sp, fontWeight = FontWeight.ExtraBold, color = p.texto)
        Text("Cerca", fontSize = tamano.sp, fontWeight = FontWeight.ExtraBold, color = p.marca)
    }
}

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    borde: Color? = null,
    alTocar: (() -> Unit)? = null,
    relleno: Dp = 14.dp,
    contenido: @Composable () -> Unit
) {
    val p = LocalPaleta.current
    val forma = RoundedCornerShape(18.dp)
    Box(
        modifier
            .clip(forma)
            .background(p.tarjeta)
            .border(if (borde != null) 2.dp else 1.dp, borde ?: p.linea, forma)
            .then(if (alTocar != null) Modifier.clickable(onClick = alTocar) else Modifier)
            .padding(relleno)
    ) { contenido() }
}

@Composable
fun BotonPrincipal(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    habilitado: Boolean = true,
    color: Color? = null,
    colorTexto: Color? = null
) {
    val p = LocalPaleta.current
    Button(
        onClick = alTocar,
        enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color ?: p.marca,
            contentColor = colorTexto ?: p.marcaTexto,
            disabledContainerColor = (color ?: p.marca).copy(alpha = 0.5f),
            disabledContentColor = (colorTexto ?: p.marcaTexto).copy(alpha = 0.8f)
        ),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(22.dp), color = colorTexto ?: p.marcaTexto, strokeWidth = 2.5.dp)
        } else {
            Text(texto, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BotonSecundario(texto: String, alTocar: () -> Unit, modifier: Modifier = Modifier, color: Color? = null) {
    val p = LocalPaleta.current
    OutlinedButton(
        onClick = alTocar,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color ?: p.linea),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color ?: p.texto)
    ) { Text(texto, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun Campo(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    tipo: KeyboardType = KeyboardType.Text,
    esPassword: Boolean = false,
    lineas: Int = 1
) {
    val p = LocalPaleta.current
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        singleLine = lineas == 1,
        minLines = lineas,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = tipo),
        visualTransformation = if (esPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = p.marca,
            unfocusedBorderColor = p.linea,
            focusedLabelColor = p.marca,
            unfocusedLabelColor = p.texto2,
            focusedContainerColor = p.tarjeta,
            unfocusedContainerColor = p.tarjeta,
            cursorColor = p.marca
        )
    )
}

@Composable
fun Pastilla(texto: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(99.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ColorEstado(estado: String): Color {
    val p = LocalPaleta.current
    return when (estado) {
        "PENDIENTE" -> p.nuevo
        "ACEPTADO", "EN_PREPARACION" -> p.cocinando
        "LISTO_PARA_RECOGER" -> p.listo
        "CANCELADO" -> p.peligro
        else -> p.texto2
    }
}

@Composable
fun Cargando(modifier: Modifier = Modifier) {
    val p = LocalPaleta.current
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = p.marca)
    }
}

@Composable
fun MensajeVacio(titulo: String, texto: String, modifier: Modifier = Modifier, accion: (@Composable () -> Unit)? = null) {
    val p = LocalPaleta.current
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(titulo, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, color = p.texto, textAlign = TextAlign.Center)
        Text(texto, color = p.texto2, textAlign = TextAlign.Center)
        if (accion != null) {
            Spacer(Modifier.height(8.dp))
            accion()
        }
    }
}

@Composable
fun Aviso(texto: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(12.dp)
    ) {
        Text(texto, color = LocalPaleta.current.texto, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BarraSuperior(titulo: String, alVolver: (() -> Unit)? = null, acciones: @Composable () -> Unit = {}) {
    val p = LocalPaleta.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (alVolver != null) {
            IconButton(onClick = alVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = p.texto)
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            titulo,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = p.texto,
            modifier = Modifier.weight(1f)
        )
        acciones()
    }
}

@Composable
fun Avatar(texto: String, tamano: Dp = 36.dp) {
    val p = LocalPaleta.current
    Box(
        Modifier.size(tamano).clip(CircleShape).background(p.marca.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = p.marca, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun IconoCirculo(icono: ImageVector, color: Color, tamano: Dp = 44.dp) {
    Box(
        Modifier.size(tamano).clip(CircleShape).background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color)
    }
}

@Composable
fun SelectorCantidad(cantidad: Int, alRestar: () -> Unit, alSumar: () -> Unit) {
    val p = LocalPaleta.current
    Row(
        Modifier.clip(RoundedCornerShape(12.dp)).background(p.tarjeta2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clickable(onClick = alRestar), contentAlignment = Alignment.Center) {
            Text("−", fontSize = 20.sp, color = p.texto, fontWeight = FontWeight.Bold)
        }
        Text("$cantidad", fontWeight = FontWeight.Bold, color = p.texto, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
        Box(Modifier.size(38.dp).clickable(onClick = alSumar), contentAlignment = Alignment.Center) {
            Text("+", fontSize = 20.sp, color = p.marca, fontWeight = FontWeight.Bold)
        }
    }
}
