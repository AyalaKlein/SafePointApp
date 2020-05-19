package com.example.safepoint

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.common_google_signin_btn_icon_dark)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        // secLeft need to be the amount of time to find a safe point in the area
        val secLeft = 20

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        shelters = JSONArray(intent.getStringExtra("shelters"))

        view_timer.isCountDown = true
        view_timer.base = SystemClock.elapsedRealtime() + (1000 * secLeft)
        view_timer.start()

        view_timer.setOnChronometerTickListener {
            if (view_timer.getText().toString() == "00:00")
                view_timer.stop()
        }


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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
