package com.pidecerca.app.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pidecerca.app.MainActivity
import com.pidecerca.app.PideCercaApp
import com.pidecerca.app.R

object Avisos {

    fun puedeNotificar(contexto: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun mostrar(contexto: Context, notificacion: Notificacion) {
        if (!puedeNotificar(contexto)) return
        val intent = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            notificacion.pedido?.id?.let { putExtra(MainActivity.EXTRA_PEDIDO, it) }
        }
        val pendiente = PendingIntent.getActivity(
            contexto,
            notificacion.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val aviso = NotificationCompat.Builder(contexto, PideCercaApp.CANAL_PEDIDOS)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(notificacion.titulo ?: "PideCerca")
            .setContentText(notificacion.mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificacion.mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendiente)
            .build()
        NotificationManagerCompat.from(contexto).notify(notificacion.id.toInt(), aviso)
    }
}

object Ubicacion {

    const val LATITUD_POR_DEFECTO = -12.0464
    const val LONGITUD_POR_DEFECTO = -77.0428

    fun tienePermiso(contexto: Context): Boolean =
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun ultima(contexto: Context): Location? {
        if (!tienePermiso(contexto)) return null
        val gestor = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return gestor.getProviders(true)
            .mapNotNull { proveedor -> runCatching { gestor.getLastKnownLocation(proveedor) }.getOrNull() }
            .maxByOrNull { it.time }
    }
}
