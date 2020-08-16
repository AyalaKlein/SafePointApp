package com.example.safepoint

import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.example.safepoint.background.LocationService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import io.github.rybalkinsd.kohttp.ext.asString
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.ext.httpGetAsync
import io.github.rybalkinsd.kohttp.util.Json
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import models.Shelter
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.serialization.*
import kotlinx.serialization.json.*


class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {
    private var shelters: Array<Shelter> = emptyArray();
    private var assignedShelter: Shelter? = null
    private var googleMap: GoogleMap? = null
    private var myLocation: Location? = null

    //TODO add logic to set isEmergency to true (for now FCM)
    private var isEmergency: Boolean = false
        set(value) {
            field = value
            if(value){
                showEmergencyDisplay()
            } else {
                showRegularDisplay()
            }
        }

    private fun showEmergencyDisplay() {
        val secLeft = 60
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
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
        }
        if(this.googleMap != null){
            showEmergencyMap()
        }
    }
    private fun showEmergencyMap(){

        if(assignedShelter == null){
            //TODO show there is no current location
            return;
        }
        //TODO get current location
        val latLngOrigin = LatLng(10.3181466, 123.9029382) // Ayala
        val latLngDestination = assignedShelter!!.latLong

        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Origin"))
        this.googleMap!!.addMarker(
            MarkerOptions().position(latLngDestination).title("Shelter")
        )
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))


        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=10.3181466,123.9029382&destination=10.311795,123.915864&key=AIzaSyDwiKGPtON3JZTGoW08x9bBDGSqOCfMD2U"
        val directionsRequest = object : StringRequest(
            Request.Method.GET,
            urlDirections,
            com.android.volley.Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points =
                        steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
            },
            com.android.volley.Response.ErrorListener { _ ->
            }) {}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)

    }
    private fun showRegularMap(){
        if(!this.shelters.isNotEmpty()){
            return;
        }

        val builder = LatLngBounds.Builder()
        for(shelter in shelters){
            this.googleMap!!.addMarker(MarkerOptions().position(shelter.latLong).title(shelter.details))
            builder.include(shelter.latLong)
        }
        this.googleMap!!.setLatLngBoundsForCameraTarget(builder.build())
    }

    private fun showRegularDisplay() {
        nSafe.visibility = View.GONE
        left_btn.visibility = View.GONE
        nav_btn.visibility = View.GONE
        load_btn.visibility = View.GONE
        angry_btn.visibility = View.GONE
        view_timer.visibility = View.GONE
        GlobalScope.launch {
            //TODO use env server
            val res = "${BuildConfig.HOST}/api/shelters".httpGetAsync().await()
//            res.use{
//                val json = Json(JsonConfiguration.Default)
//                this@NavigationActivity.shelters = json.parse(Shelter.ser(), it.asString())
//            }
            showRegularMap()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.common_google_signin_btn_icon_dark)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        isEmergency = false

        LocationService.init(applicationContext, this@NavigationActivity)
        LocationService.getLastLocation().addOnCompleteListener {
            this.myLocation = it.result
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if(!isEmergency){
            showRegularMap()
        } else {
            showEmergencyMap()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item);
    }


}
