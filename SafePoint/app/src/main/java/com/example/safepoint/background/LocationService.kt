package com.example.safepoint.background

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.safepoint.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class LocationService {
    companion object {
        val PERMISSION_ID = 42
        lateinit var mFusedLocationClient: FusedLocationProviderClient
        lateinit var context: Context
        var activity: Activity? = null

        fun init(context: Context, activity: Activity?) {
            this.context = context
            this.activity = activity

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context)
        }

        @SuppressLint("MissingPermission")
        fun getLastLocation(): Task<Location?> {
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    return mFusedLocationClient.lastLocation.continueWith(Continuation {
                        var location: Location? = it.result
                        if (location == null) {
                            requestNewLocationData()
                        }
                        return@Continuation location
                    })
                } else {
                    Toast.makeText(context, "Turn on location", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }
            } else if(this.activity != null) {
                requestPermissions()
            }

            return Tasks.call {
                return@call null
            }
        }

        @SuppressLint("MissingPermission")
        private fun requestNewLocationData() {
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 10000
            mLocationRequest.fastestInterval = 5000
            mLocationRequest.numUpdates = 10

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
            )
        }

        private val mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                var mLastLocation: Location = locationResult.lastLocation
                //findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
                //findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
            }
        }

        private fun isLocationEnabled(): Boolean {
            var locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

        private fun checkPermissions(): Boolean {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
            return false
        }

        private fun requestPermissions() {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_ID
            )
        }
    }
}