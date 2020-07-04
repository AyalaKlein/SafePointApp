package contexts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.*

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Geofence Triggered")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        //        if (geofencingEvent.hasError()) {
//            String errorMessage = LocationServicesManager.getGeofenceErrorString(
//                    geofencingEvent.getErrorCode());
//            Log.e(TAG, errorMessage);
//            return;
//        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL || geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences =
                geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                geofenceTransition,
                triggeringGeofences
            )
            for (geofence in triggeringGeofences) {
                //TODO request new shelter
            }
            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(
                TAG, String.format(
                    "Geofence transition error: invalid transition type %1\$d",
                    geofenceTransition
                )
            )
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = getTransitionString(geofenceTransition)

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList: MutableList<String?> =
            ArrayList()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        return geofenceTransitionString + ": " + TextUtils.join(", ", triggeringGeofencesIdsList)
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entered"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "Dwelt"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exited"
            else -> "Unknown Transition"
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastRcvr"
    }
}