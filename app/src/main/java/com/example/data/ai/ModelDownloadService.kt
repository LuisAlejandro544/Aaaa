package com.example.data.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ModelDownloadService : Service() {

    companion object {
        const val CHANNEL_ID = "stem_models_download_channel"
        const val NOTIFICATION_ID = 4001

        fun startDownload(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialNotification = buildNotification("Iniciando descarga de 4 modelos IA TFLite (~74.9 MB)...", 0, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        serviceScope.launch {
            StemModelManager.downloadAllModelsWithNotification(
                context = applicationContext,
                onProgress = { currentModelName, overallPercent, currentMb, totalMb, statusMsg ->
                    val notification = buildNotification(
                        content = statusMsg,
                        progress = overallPercent,
                        isIndeterminate = false
                    )
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                },
                onComplete = { success ->
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    val finalManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (success) {
                        val doneNotification = NotificationCompat.Builder(this@ModelDownloadService, CHANNEL_ID)
                            .setContentTitle("¡Modelos IA TFLite Listos!")
                            .setContentText("Los 4 modelos TFLite FP16 (74.9 MB) están instalados y activos.")
                            .setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setAutoCancel(true)
                            .setContentIntent(getPendingIntent())
                            .build()
                        finalManager.notify(NOTIFICATION_ID + 1, doneNotification)
                    } else {
                        val failNotification = NotificationCompat.Builder(this@ModelDownloadService, CHANNEL_ID)
                            .setContentTitle("Descarga de Modelos Incompleta")
                            .setContentText("Reintenta cuando tengas conexión a internet estable.")
                            .setSmallIcon(android.R.drawable.stat_notify_error)
                            .setAutoCancel(true)
                            .setContentIntent(getPendingIntent())
                            .build()
                        finalManager.notify(NOTIFICATION_ID + 1, failNotification)
                    }
                    stopSelf()
                }
            )
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Descarga de Modelos IA TFLite"
            val descriptionText = "Notificaciones de progreso para la descarga de modelos de separación de audio"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(
        content: String,
        progress: Int,
        isIndeterminate: Boolean
    ) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Descargando Modelos IA TFLite FP16")
        .setContentText(content)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setProgress(100, progress, isIndeterminate)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(getPendingIntent())
        .build()
}
