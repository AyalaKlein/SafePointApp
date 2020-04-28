package com.example.safepoint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_navigation.*
import org.json.JSONArray

class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {
    var shelters: JSONArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        shelters = JSONArray(intent.getStringExtra("shelters"))

        // TODO : set interval on nTimer

        nSafe.setOnClickListener {
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
