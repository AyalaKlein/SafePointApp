package background.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.core.app.NotificationCompat
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.stringify
import kotlinx.serialization.json.JsonConfiguration
import models.Alert
import org.joda.time.DateTime
import java.util.*


class SafePointFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "From: ${remoteMessage.from}")
            if (remoteMessage.data.containsKey("locX")) {
                //an alert
                LocationService.getLastLocation().addOnCompleteListener{
                    val userLocation = it.result
                    val locX = remoteMessage.data["locX"]?.toDoubleOrNull()
                    val locY = remoteMessage.data["locY"]?.toDoubleOrNull()
                    val distance = remoteMessage.data["meterRadius"]?.toDoubleOrNull()
                    if (locX == null || locY == null || userLocation == null) {
                        return@addOnCompleteListener;
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
                        val settings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
                        with(settings.edit()){
                            val json = Json(JsonConfiguration.Stable)
                            putString(getString(R.string.lastAlert), json.stringify(Alert.serializer(), Alert(DateTime.now(), 360)))
                            commit()
                        }
                        val intent = Intent(this, NavigationActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                        val builder = NotificationCompat.Builder(this, "SAFEPOINT_ALERT").setContentTitle("Alert")
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentText("Get to a Shelter")
                            .setDefaults(NotificationCompat.DEFAULT_ALL).setPriority(
                                NotificationCompat.PRIORITY_HIGH
                            ).setContentIntent(pendingIntent)

                        val notificationManager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(1, builder.build())
                    }
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