package com.example.safepoint

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.safepoint.background.LocationService
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.example.safepoint.dataCache.SheltersCache
import com.google.android.gms.common.api.Response
import com.google.android.gms.location.*
import io.github.rybalkinsd.kohttp.ext.asString
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.ext.httpGetAsync
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.json.JSONArray


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

            val intent = Intent(applicationContext, NavigationActivity::class.java)
            startActivity(intent)
            finish()
        }

        guest.setOnClickListener {
            val settings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
            settings.getBoolean(getString(R.string.ranOnce), false)
            with(settings.edit()){
                putBoolean(getString(R.string.ranOnce), true)
                commit()
            }

            val intent = Intent(applicationContext, NavigationActivity::class.java)
            startActivity(intent)
            finish()
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