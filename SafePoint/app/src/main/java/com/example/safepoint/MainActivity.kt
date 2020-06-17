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
import io.github.rybalkinsd.kohttp.ext.httpGet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.json.JSONArray


class MainActivity : AppCompatActivity() {

    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var locXCurr = ""
    var locYCurr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Set listener to Home Front Command API


        //TODO: Set interval for relevant shelters

        var json = ""
//        "http://localhost:5000/api/shelters".httpGet().isSuccessful() =

        var res = ""
        val scope = CoroutineScope(newFixedThreadPoolContext(1, "synchronizationPool"))
        scope.launch {
            res = "http://10.0.2.2:5000/api/shelters".httpGet().body()!!.string()
//          val shelters = JSONArray(res)
            //(shelters[0] as JSONObject)["id"]
            //val intent = Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse("http://maps.google.com/maps?saddr=$locYCurr,$locXCurr&daddr=$locXCurr,$locXCurr")
//            )
//            startActivity(intent)
            val shelters = JSONArray(res)
            val intent = Intent(applicationContext, NavigationActivity::class.java)

            intent.putExtra("shelters", res)
            startActivity(intent)
            finish()

        LocationService.init(applicationContext, this@MainActivity)
        LocationService.getLastLocation().addOnCompleteListener {
            if (it.result != null) {
                //initShelters(it.result!!)
            }
        }
            //TODO: Set interval for relevant shelters


            profileSettings.setOnClickListener {
                val intent = Intent(applicationContext, ProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

}