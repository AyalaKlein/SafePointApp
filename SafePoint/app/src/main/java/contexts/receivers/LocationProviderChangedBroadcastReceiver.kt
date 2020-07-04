package contexts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import background.workers.AddGeofencesWorker

class LocationProviderChangedBroadcastReceiver : BroadcastReceiver() {
    var isGpsEnabled = false
    var isNetworkEnabled = false
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) return
        if (intent.action!! == "android.location.PROVIDERS_CHANGED") {
            Log.i(
                TAG,
                "Location Providers changed"
            )
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (isGpsEnabled || isNetworkEnabled) {
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "AddGeofences",
                    ExistingWorkPolicy.KEEP,
                    AddGeofencesWorker.reqeust
                )
            }
        }
    }

    companion object {
        private const val TAG = "KUHNHENN"
    }
}