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
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import io.github.rybalkinsd.kohttp.ext.httpGet
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject


class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {
    var shelters: JSONArray = JSONArray()
    var choosenShelter: JSONObject = JSONObject()
    private var googleMap: GoogleMap? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.common_google_signin_btn_icon_dark)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //TODO: shelters
        // get current location

        // secLeft need to be the amount of time to find a safe point in the area
        val secLeft = 60

        //
//    val shelter = shelters.getJSONObject(i)!!
//    googleMap.addMarker(MarkerOptions().position(LatLng(shelter["locX"] as Double, shelter["locY"] as Double)))
//}

        for (i in 0 until shelters.length()) {
            val shelter = shelters.getJSONObject(i)!!
            choosenShelter = shelter
        }

        /*TODO: Algo - get the closest shelter
            for each shelter = >
                if shelters is not full ++
                if shelters is easy ++
           shelters.sort by distance then rate
           chosen = shelters[0]
        */


// Navigate

// Obtain the SupportMapFragment and get notified when the map is ready to be used.


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
    this.googleMap = googleMap

    val latLngOrigin = LatLng(10.3181466, 123.9029382) // Ayala
    val latLngDestination = LatLng(10.311795,123.9129666) // Best Shelter
        //LatLng((shelters[0] as JSONObject)["locX"]), (shelters[0] as JSONObject)["locY"])
        //LatLng(10.311795,123.915864) // SM City
    this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Ayala"))
    this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title("Best Shelter"))
    this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))


    val path: MutableList<List<LatLng>> = ArrayList()
    val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=10.3181466,123.9029382&destination=10.311795,123.915864&key=AIzaSyDwiKGPtON3JZTGoW08x9bBDGSqOCfMD2U"
    val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, com.android.volley.Response.Listener<String> {
            response ->
        val jsonResponse = JSONObject(response)
        // Get routes
        val routes = jsonResponse.getJSONArray("routes")
        val legs = routes.getJSONObject(0).getJSONArray("legs")
        val steps = legs.getJSONObject(0).getJSONArray("steps")
        for (i in 0 until steps.length()) {
            val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
            path.add(PolyUtil.decode(points))
        }
        for (i in 0 until path.size) {
            this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
        }
    }, com.android.volley.Response.ErrorListener {
            _ ->
    }){}
    val requestQueue = Volley.newRequestQueue(this)
    requestQueue.add(directionsRequest)

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
