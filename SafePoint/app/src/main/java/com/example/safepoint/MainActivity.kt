package com.example.safepoint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signup.setOnClickListener {
            val settings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
            with(settings.edit()){
                putBoolean("ranOnce", true)
                commit()
            }

            FirebaseMessaging.getInstance().subscribeToTopic("israel-alerts")
                .addOnCompleteListener {
                    val intent = Intent(applicationContext, NavigationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
        }

        guest.setOnClickListener {
            val settings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
            with(settings.edit()){
                putBoolean(getString(R.string.ranOnce), true)
                commit()
            }

            FirebaseMessaging.getInstance().subscribeToTopic("israel-alerts")
                .addOnCompleteListener {
                    val intent = Intent(applicationContext, NavigationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
        }

    }
    override fun onStart(){
        val settings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
        val didRunOnce = settings.getBoolean(getString(R.string.ranOnce), false)
        if(didRunOnce){
            val intent = Intent(applicationContext, NavigationActivity::class.java)
            startActivity(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "safepoint alerts"
            val descriptionText = "safepoint alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("SAFEPOINT_ALERT", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        super.onStart()
    }
}