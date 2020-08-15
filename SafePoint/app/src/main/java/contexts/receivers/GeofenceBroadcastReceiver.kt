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

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            //TODO get assigned shelter
        } else {
            Log.e(
                TAG, String.format(
                    "Geofence transition error: invalid transition type %1\$d",
                    geofenceTransition
                )
            )
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastRcvr"
    }
}