package com.example.safepoint

import android.content.Context
import android.content.Intent
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
            settings.getBoolean(getString(R.string.ranOnce), false)
            with(settings.edit()){
                putBoolean(getString(R.string.ranOnce), true)
                commit()
            }

            FirebaseMessaging.getInstance().subscribeToTopic("israel-alerts")
                .addOnCompleteListener { task ->
                    val intent = Intent(applicationContext, NavigationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
        }

    }
    override fun onStart(){

        // TODO: Set listener to Home Front Command API
        // TODO: move forward based on button click or if the user already saw this display

        val settings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
        val didRunOnce = settings.getBoolean(getString(R.string.ranOnce), false)
        if(didRunOnce){
            val intent = Intent(applicationContext, NavigationActivity::class.java)
            startActivity(intent)
        }
        super.onStart()
    }
}