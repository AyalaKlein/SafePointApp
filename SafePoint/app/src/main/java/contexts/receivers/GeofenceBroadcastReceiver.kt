package contexts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.example.safepoint.BuildConfig
import com.example.safepoint.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import io.github.rybalkinsd.kohttp.dsl.async.httpGetAsync
import io.github.rybalkinsd.kohttp.ext.asString
import io.github.rybalkinsd.kohttp.ext.url
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Geofence Triggered")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val currLocation = geofencingEvent.triggeringLocation
            val userSettings =
                context.getSharedPreferences(
                    context.getString(R.string.user_settings),
                    Context.MODE_PRIVATE
                )
            val userFcmToken = userSettings.getString(context.getString(R.string.fcm_token), "")
            GlobalScope.launch {
                val response = httpGetAsync {
                    url("${BuildConfig.HOST}/api/shelters/SearchForShelter?operationGuid=${UUID.randomUUID()}&fcmToken=${userFcmToken}&locX=${currLocation.longitude}&locY=${currLocation.latitude}")
                }.await()

                with(userSettings.edit()) {
                    putString(context.getString(com.example.safepoint.R.string.selected_shelter), response.asString())
                    commit()
                }
            }
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