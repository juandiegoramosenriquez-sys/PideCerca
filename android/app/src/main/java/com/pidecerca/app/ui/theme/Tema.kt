package com.pidecerca.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class ColorMarca(val nombre: String, val oscuro: Color, val claro: Color, val texto: Color)

val COLORES_MARCA = linkedMapOf(
    "morado" to ColorMarca("Morado", Color(0xFF7C4DFF), Color(0xFF6236FF), Color.White),
    "verde" to ColorMarca("Verde", Color(0xFF00E676), Color(0xFF00A152), Color(0xFF00210F)),
    "amarillo" to ColorMarca("Amarillo", Color(0xFFFFD600), Color(0xFFE6B800), Color(0xFF2A2300)),
    "rojo" to ColorMarca("Rojo", Color(0xFFFF1744), Color(0xFFE0103A), Color.White),
    "azul" to ColorMarca("Azul", Color(0xFF2979FF), Color(0xFF1E5FE0), Color.White)
)

@Immutable
data class Paleta(
    val oscuro: Boolean,
    val fondo: Color,
    val tarjeta: Color,
    val tarjeta2: Color,
    val linea: Color,
    val texto: Color,
    val texto2: Color,
    val texto3: Color,
    val marca: Color,
    val marcaTexto: Color,
    val nuevo: Color,
    val cocinando: Color,
    val listo: Color,
    val peligro: Color
)

fun crearPaleta(colorMarca: String, oscuro: Boolean): Paleta {
    val marca = COLORES_MARCA[colorMarca] ?: COLORES_MARCA.getValue("morado")
    return if (oscuro) {
        Paleta(
            oscuro = true,
            fondo = Color(0xFF0D1014),
            tarjeta = Color(0xFF161A20),
            tarjeta2 = Color(0xFF1C2129),
            linea = Color(0xFF232931),
            texto = Color(0xFFEEF0F3),
            texto2 = Color(0xFF9AA1AB),
            texto3 = Color(0xFF6B727C),
            marca = marca.oscuro,
            marcaTexto = marca.texto,
            nuevo = Color(0xFFFFD600),
            cocinando = Color(0xFF5C9BFF),
            listo = Color(0xFF00E676),
            peligro = Color(0xFFFF5C7A)
        )
    } else {
        Paleta(
            oscuro = false,
            fondo = Color(0xFFF5F6F8),
            tarjeta = Color.White,
            tarjeta2 = Color(0xFFF0F2F5),
            linea = Color(0xFFE3E5E8),
            texto = Color(0xFF14161A),
            texto2 = Color(0xFF5F6670),
            texto3 = Color(0xFF8A9099),
            marca = marca.claro,
            marcaTexto = marca.texto,
            nuevo = Color(0xFF8A6D00),
            cocinando = Color(0xFF1E5FE0),
            listo = Color(0xFF00873E),
            peligro = Color(0xFFD50032)
        )
    }
}

val LocalPaleta = staticCompositionLocalOf { crearPaleta("morado", true) }

private val tipografia = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun PideCercaTema(paleta: Paleta, contenido: @Composable () -> Unit) {
    val esquema = if (paleta.oscuro) {
        darkColorScheme(
            primary = paleta.marca,
            onPrimary = paleta.marcaTexto,
            background = paleta.fondo,
            onBackground = paleta.texto,
            surface = paleta.tarjeta,
            onSurface = paleta.texto,
            surfaceVariant = paleta.tarjeta2,
            onSurfaceVariant = paleta.texto2,
            outline = paleta.linea,
            error = paleta.peligro
        )
    } else {
        lightColorScheme(
            primary = paleta.marca,
            onPrimary = paleta.marcaTexto,
            background = paleta.fondo,
            onBackground = paleta.texto,
            surface = paleta.tarjeta,
            onSurface = paleta.texto,
            surfaceVariant = paleta.tarjeta2,
            onSurfaceVariant = paleta.texto2,
            outline = paleta.linea,
            error = paleta.peligro
        )
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalPaleta provides paleta) {
        MaterialTheme(colorScheme = esquema, typography = tipografia, content = contenido)
    }
}
