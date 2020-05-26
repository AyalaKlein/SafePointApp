package com.example.safepoint.background

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.safepoint.MainActivity
import com.example.safepoint.dataCache.SheltersCache


class SyncService : Service() {
    private val NOTIF_ID = 1123123
    private val NOTIF_CHANNEL_ID = "91919191"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // do your jobs here
        startForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "SafePoint"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(NOTIF_CHANNEL_ID, name, importance)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        val handler = Handler()
        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                LocationService.getLastLocation().addOnCompleteListener {
                    val location: Location? = it.result
                    if (location != null) {
                        SheltersCache.getShelters(location.latitude, location.longitude, 10000.0) {
                            val a = it
                        }
                    }

                    handler.postDelayed(this, 10000)
                }
            }
        }

        handler.postDelayed(runnableCode, 10000)

        startForeground(
            NOTIF_ID, NotificationCompat.Builder(
                this,
                NOTIF_CHANNEL_ID
            )
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_input_add)
                .setContentTitle("SafePoint")
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build()
        )
    }
}