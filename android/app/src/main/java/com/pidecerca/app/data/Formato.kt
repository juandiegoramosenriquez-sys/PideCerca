package com.pidecerca.app.data

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formato {

    private val formatoFecha = DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.forLanguageTag("es-PE"))

    fun soles(valor: Double): String = String.format(Locale.US, "S/ %.2f", valor)

    fun distancia(metros: Double?): String? {
        if (metros == null) return null
        return if (metros < 1000) "${metros.toInt()} m" else String.format(Locale.US, "%.1f km", metros / 1000)
    }

    fun fecha(valor: String?): LocalDateTime? = valor?.let {
        runCatching { LocalDateTime.parse(it.take(19)) }.getOrNull()
    }

    fun fechaCorta(valor: String?): String = fecha(valor)?.format(formatoFecha) ?: ""

    fun haceCuanto(valor: String?): String {
        val inicio = fecha(valor) ?: return ""
        val minutos = Duration.between(inicio, LocalDateTime.now()).toMinutes()
        return when {
            minutos < 1 -> "ahora"
            minutos < 60 -> "hace $minutos min"
            minutos < 1440 -> "hace ${minutos / 60} h"
            minutos < 2880 -> "ayer"
            else -> "hace ${minutos / 1440} días"
        }
    }

    fun minutosRestantes(pedido: Pedido): Long? {
        val inicio = fecha(pedido.fechaAceptado ?: pedido.fecha) ?: return null
        val tiempo = pedido.tiempoPreparacion ?: return null
        return Duration.between(LocalDateTime.now(), inicio.plusMinutes(tiempo.toLong())).toMinutes()
    }

    fun metodoPago(metodo: String?): String = when (metodo) {
        "YAPE" -> "Yape"
        "PLIN" -> "Plin"
        "TARJETA" -> "Tarjeta"
        "EFECTIVO" -> "Efectivo"
        else -> metodo ?: "-"
    }

    fun estadoTexto(estado: String): String = when (estado) {
        "PENDIENTE" -> "Enviado"
        "ACEPTADO" -> "Aceptado"
        "EN_PREPARACION" -> "Preparando"
        "LISTO_PARA_RECOGER" -> "¡Listo para recoger!"
        "RECOGIDO" -> "Recogido"
        "CANCELADO" -> "Cancelado"
        else -> estado
    }

    fun pasoEstado(estado: String): Int = when (estado) {
        "PENDIENTE" -> 0
        "ACEPTADO" -> 1
        "EN_PREPARACION" -> 1
        "LISTO_PARA_RECOGER" -> 2
        "RECOGIDO" -> 3
        else -> -1
    }

    fun esActivo(estado: String): Boolean = estado in setOf("PENDIENTE", "ACEPTADO", "EN_PREPARACION", "LISTO_PARA_RECOGER")
}
