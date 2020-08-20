package background.services

import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.core.app.NotificationCompat
import android.content.Context
import com.example.safepoint.BuildConfig
import com.example.safepoint.NavigationActivity
import com.example.safepoint.R
import com.example.safepoint.background.LocationService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.rybalkinsd.kohttp.dsl.async.httpGetAsync
import io.github.rybalkinsd.kohttp.dsl.async.httpPostAsync
import io.github.rybalkinsd.kohttp.ext.asString
import io.github.rybalkinsd.kohttp.ext.url
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class SafePointFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "From: ${remoteMessage.from}")
            if (remoteMessage.data.containsKey("locX")) {
                //an alert
                val userLocation = LocationService.getLastLocation().result
                val locX = remoteMessage.data["locX"]?.toDoubleOrNull()
                val locY = remoteMessage.data["locY"]?.toDoubleOrNull()
                val distance = remoteMessage.data["meterRadius"]?.toDoubleOrNull()
                if (locX == null || locY == null || userLocation == null) {
                    return;
                }
                val dist = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude,
                    userLocation.longitude,
                    locX,
                    locY,
                    dist
                )
                if (dist[0] < distance ?: 0.0) {
                    //in alert area
                    val builder = NotificationCompat.Builder(this, "")
                    builder.setContentTitle("")
                        //.setContentIntent(notificationPendingIntent)
                        .setContentText("")
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                }

            } else {
                //shelter needs updating
                LocationService.getLastLocation().addOnCompleteListener {
                    val userSettings =
                        getSharedPreferences(
                            getString(R.string.user_settings),
                            Context.MODE_PRIVATE
                        )
                    val userFcmToken = userSettings.getString(getString(R.string.fcm_token), "")
                    if (it.result != null) {
                        GlobalScope.launch {
                            val response = httpGetAsync {
                                url("${BuildConfig.HOST}/api/shelters/SearchForShelter?operationGuid=${remoteMessage.data["operationGuid"]}&fcmToken=${userFcmToken}&locX=${it.result!!.longitude}&locY=${it.result!!.latitude}")
                            }.await()

                            with(userSettings.edit()) {
                                putString(getString(R.string.selected_shelter), response.asString())
                                commit()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewToken(newToken: String) {
        val userSettings =
            getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
        val oldToken = userSettings.getString(getString(R.string.fcm_token), "")

        GlobalScope.launch {
            httpPostAsync {
                url("${BuildConfig.HOST}/api/shelters/ChangeUserFcmToken")
                body {
                    form {
                        "oldToken" to oldToken
                        "newToken" to newToken
                    }
                }
            }.await()
            with(userSettings.edit()) {
                putString(getString(R.string.fcm_token), newToken)
                commit()
            }
        }
    }

    companion object {
        private const val TAG = "FCMMessage"
    }
}