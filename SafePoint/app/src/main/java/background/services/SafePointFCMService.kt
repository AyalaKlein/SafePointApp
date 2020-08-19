package background.services

import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.safepoint.R
import com.example.safepoint.background.LocationService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SafePointFCMService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "From: ${remoteMessage.from}")
            if (remoteMessage.data.containsKey("locX"))
            {
                //an alert
                val userLocation = LocationService.getLastLocation().result
                val locX = remoteMessage.data["locX"]?.toDoubleOrNull()
                val locY = remoteMessage.data["locY"]?.toDoubleOrNull()
                val distance = remoteMessage.data["meterRadius"]?.toDoubleOrNull()
                if (locX == null || locY == null || userLocation == null) {
                    return;
                }
                val dist = FloatArray(1)
                Location.distanceBetween(userLocation.latitude, userLocation.longitude, locX, locY, dist)
                if (dist[0] < distance?:0.0){
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
                //TODO update cache
            }
        }
    }

    override fun onNewToken(newToken: String) {
        //TODO update token in api
    }

    companion object {
        private const val TAG = "FCMMessage"
    }
}