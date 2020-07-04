package utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.safepoint.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import contexts.receivers.GeofenceBroadcastReceiver
import java.util.*
import kotlin.collections.HashSet

class GeofencesManager constructor(val context: Context) {
    companion object Factory {
        private const val TAG: String = "GeofenceManager"

        fun createGeofenceManager(context: Context, permissionsUtils: PermissionsUtils): GeofencesManager? {
            if (permissionsUtils.isPermitted(listOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION), 1 )) {//context.applicationContext.resources.getInteger(R.integer.permissions_location_map_location))) {

                return GeofencesManager(context)
            }
            return null
        }
    }

    private var geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private var registeredGeofences: HashSet<String> = hashSetOf()
    private val unRegisteredGeofences: MutableMap<String, Geofence> = mutableMapOf()
    private val mGeofencePendingIntent: PendingIntent? = null

    init {
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.geofences), Context.MODE_PRIVATE)
        val geofences = sharedPreferences.getString(context.getString(R.string.geofence_list), "")
        for (geofence in geofences!!.split(",").toTypedArray()){
            registeredGeofences.add(geofence)
        }
    }

    fun addGeofenceIfNotExist(key: String, location: LatLng?, radius: Int) {
        if (registeredGeofences.contains(key)) {
            return
        }
        if (location != null) {
            val geofence = Geofence.Builder()
                    .setRequestId(key)
                    .setCircularRegion(location.latitude, location.longitude, radius.toFloat())
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL
                            or Geofence.GEOFENCE_TRANSITION_ENTER
                            or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(1000 * 30)
                    .build()
            unRegisteredGeofences[key] = geofence
        }
    }

    fun addOrUpdateGeofence(key: String, location: LatLng?, radius: Int) {
        if (location != null) {
            val geofence = Geofence.Builder()
                    .setRequestId(key)
                    .setCircularRegion(location.latitude, location.longitude, radius.toFloat())
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL
                            or Geofence.GEOFENCE_TRANSITION_ENTER
                            or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(1000 * 30)
                    .build()
            unRegisteredGeofences[key] = geofence
        }
    }// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
// registerGeofences() and removeGeofences().

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private val geofencePendingIntent: PendingIntent
        get() {
            if (mGeofencePendingIntent == null) {
                val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
                // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
                // registerGeofences() and removeGeofences().
                return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            return mGeofencePendingIntent
        }

    // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
    // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
    // is already inside that geofence.
    // Empty mGeofenceList leads to crash

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    fun createGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        // Empty mGeofenceList leads to crash
        builder.addGeofences(ArrayList(unRegisteredGeofences.values))
        return builder.build()
    }

    fun registerGeofences() {
        if (unRegisteredGeofences.size > 0) {
            try {
                val t = geofencingClient.addGeofences(createGeofencingRequest(), geofencePendingIntent)
                t.addOnSuccessListener { aVoid: Void? ->
                    Log.i(TAG, "Adding Geofence Success")
                    updateRegisteredGeofences()
                }
                t.addOnFailureListener { e: Exception? ->
                    Log.e(TAG, "add geofence failed", e)
                }
            } catch (securityException: SecurityException) {
                Log.e(TAG, "Missing permission ACCESS_FINE_LOCATION", securityException)
            }
        }
    }

    private fun updateRegisteredGeofences() {
        registeredGeofences.addAll(unRegisteredGeofences.keys)
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.geofences), Context.MODE_PRIVATE)
        with(sharedPreferences.edit()){
            putString(context.getString(R.string.geofence_list), registeredGeofences.joinToString(separator = ","))
            apply()
        }
    }

    fun removeGeofences(vararg keys: String?) {
        geofencingClient.removeGeofences(Arrays.asList<String>(*keys)).addOnSuccessListener { aVoid: Void? ->
            Log.i(TAG, "Removing Geofence Success")
            for (key in keys) {
                registeredGeofences.remove(key)
            }
       }.addOnFailureListener { e: Exception? ->

            Log.e(TAG, "Removing geofence failed", e)
        }
    }

    fun resetAppGeofences() {
        if (registeredGeofences.size > 0) {
            geofencingClient.removeGeofences(geofencePendingIntent).addOnSuccessListener { aVoid: Void? ->
                registeredGeofences.clear()
               Log.i(TAG, "Removing Geofence Success")
            }.addOnFailureListener { e: Exception? ->
                Log.e(TAG, "Removing geofence failed", e)
            }
        }
    }


    //because dagger is shit, this should be called from activities in order to request user input if needed through the activity
    fun setContext(context: Context){
        geofencingClient = LocationServices.getGeofencingClient(context)
    }

}


fun getGeofenceErrorString(errorCode: Int): String? {
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence service is not available now"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Your app has registered too many geofences"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "You have provided too many PendingIntents to the registerGeofences() call"
        else -> "Unknown error: the Geofence service is not available now"
    }
}

