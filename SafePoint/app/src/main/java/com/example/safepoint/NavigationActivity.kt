package com.example.safepoint

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_navigation.*
import org.json.JSONArray


class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {
    var shelters: JSONArray = JSONArray()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        //TODO: shelters
        // get current location
        // secLeft need to be the amount of time to find a safe point in the area
        val secLeft = 20

        //TODO: Algo - get the closest shelter
        // Navigate

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        shelters = JSONArray(intent.getStringExtra("shelters"))

        view_timer.isCountDown = true
        view_timer.base = SystemClock.elapsedRealtime() + (1000 * secLeft)
        view_timer.start()

        view_timer.setOnChronometerTickListener {
            //TODO pop up stay safe if there is no time
            if (view_timer.text.toString() == "00:00")
                view_timer.stop()


        }


        nSafe.setOnClickListener {
            //TODO - save something
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Add a marker in Sydney and move the camera
        for (i in 0 until shelters.length()) {
            val shelter = shelters.getJSONObject(i)!!
            googleMap.addMarker(MarkerOptions().position(LatLng(shelter["lat"] as Double, shelter["lon"] as Double)))
        }
    }


}
