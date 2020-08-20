package com.example.safepoint

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.safepoint.background.LocationService
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
import io.github.rybalkinsd.kohttp.ext.httpGetAsync
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.Shelter
import org.json.JSONObject
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.*
import models.Alert
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {
    private var shelters: Array<Shelter> = emptyArray();
    private var assignedShelter: Shelter? = null
    private var googleMap: GoogleMap? = null
    private var myLocation: Location? = null

    private var isEmergency: Boolean = false
        set(value) {
            field = value
            if (value) {
                showEmergencyDisplay()
            } else {
                showRegularDisplay()
            }
        }

    private fun showEmergencyDisplay() {
        nSafe.visibility = View.VISIBLE
        left_btn.visibility = View.VISIBLE
        nav_btn.visibility = View.VISIBLE
        load_btn.visibility = View.VISIBLE
        angry_btn.visibility = View.VISIBLE
        view_timer.visibility = View.VISIBLE
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
            //TODO - do something
        }
        if (this.googleMap != null) {
            showEmergencyMap()
        }
    }

    private fun showEmergencyMap() {

        if (assignedShelter == null) {
            //TODO show there is no current location
            return;
        }
        val latLngDestination = LatLng(assignedShelter!!.locY, assignedShelter!!.locX)
        this.googleMap!!.addMarker(
            MarkerOptions().position(latLngDestination).title("Shelter")
        )
        //TODO get current location over time
        LocationService.getLastLocation().addOnCompleteListener {

            val latLngOrigin = LatLng(it.result!!.latitude, it.result!!.longitude)

            this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Origin"))
            this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))


            val path: MutableList<List<LatLng>> = ArrayList()
            val urlDirections =
                "https://maps.googleapis.com/maps/api/directions/json?origin=${latLngOrigin.latitude},${latLngOrigin.longitude}&destination=${latLngDestination.latitude},${latLngDestination.longitude}}&key=AIzaSyDfroIIZLRZMYZ4sYGDXO3O4Rqe3aCdaFA"
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
                        this.googleMap!!.addPolyline(
                            PolylineOptions().addAll(path[i]).color(Color.RED)
                        )
                    }
                },
                com.android.volley.Response.ErrorListener { _ ->
                }) {}
            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(directionsRequest)
        }
    }

    private fun showRegularMap() {
        if (!this.shelters.isNotEmpty()) {
            return;
        }

        val builder = LatLngBounds.Builder()
        for (shelter in shelters) {
            val latLng = LatLng(shelter.locY, shelter.locX)
            this.googleMap!!.addMarker(MarkerOptions().position(latLng).title(shelter.description))
            builder.include(latLng)
        }
        this.googleMap!!.setLatLngBoundsForCameraTarget(builder.build())
        //TODO fix borders
        //TODO get only surrounding locations
        //TODO show user location
    }

    private fun showRegularDisplay() {
        nSafe.visibility = View.GONE
        left_btn.visibility = View.GONE
        nav_btn.visibility = View.GONE
        load_btn.visibility = View.GONE
        angry_btn.visibility = View.GONE
        view_timer.visibility = View.GONE
        GlobalScope.launch {
            val res = "${BuildConfig.HOST}/api/shelters".httpGetAsync().await()
            res.use {
                val json = Json(JsonConfiguration.Stable)
                this@NavigationActivity.shelters = it.asString()?.let { body ->
                    json.parse(Shelter.serializer().list, body)
                }?.toTypedArray() ?: emptyArray()
            }
            this@NavigationActivity.runOnUiThread { showRegularMap() }
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

    override fun onStart() {
        val settings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
        val lastAlertJson = settings.getString(getString(R.string.lastAlert), "")
        if(lastAlertJson.isNullOrEmpty()){
            super.onStart()
            return
        }
        val json = Json(JsonConfiguration.Stable)
        val alert : Alert = json.parse(Alert.serializer(), lastAlertJson)
        isEmergency = !alert.alertDate.plusSeconds(alert.alertSeconds).isBeforeNow
        super.onStart()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if (!isEmergency) {
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
