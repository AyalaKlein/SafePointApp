package background.services

import android.util.Log
import com.example.safepoint.NavigationActivity
import com.example.safepoint.background.LocationService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.rybalkinsd.kohttp.dsl.async.httpGetAsync
import io.github.rybalkinsd.kohttp.ext.url

class SafePointFCMService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "From: ${remoteMessage.from}")
            //TODO update cache
            LocationService.getLastLocation().addOnCompleteListener {
                if (it.result != null) {
                    httpGetAsync {
                        url("http://10.0.2.2:5000/api/shelters/SearchForShelter?operationGuid=${remoteMessage.data["operationGuid"]}&fcmToken=${"asfd"}&locX=${it.result!!.longitude}&locY=${it.result!!.latitude}")
                    }.invokeOnCompletion {

                    }
                }
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