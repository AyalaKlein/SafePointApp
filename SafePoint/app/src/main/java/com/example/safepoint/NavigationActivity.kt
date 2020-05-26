package com.example.safepoint

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.MenuItem
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.ext.httpGetAsync
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.json.JSONArray


class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {
    var shelters: JSONArray = JSONArray()
    var accessToken : String? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.common_google_signin_btn_icon_dark)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        accessToken = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
            .getString(getString(R.string.access_token), null)
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
            googleMap.addMarker(MarkerOptions().position(LatLng(shelter["locX"] as Double, shelter["locY"] as Double)))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if(accessToken == null){
                    startActivity(Intent(this, SignInActivity::class.java))
                } else {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item);
    }


}
