package com.pidecerca.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class PideCercaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val canal = NotificationChannel(
            CANAL_PEDIDOS,
            getString(R.string.canal_pedidos),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Te avisa cuando tu pedido cambia de estado"
            enableVibration(true)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    companion object {
        const val CANAL_PEDIDOS = "pedidos"
    }
}
