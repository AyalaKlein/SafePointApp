package background.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SafePointFCMService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "From: ${remoteMessage.from}")
            //TODO update cache
        }
    }

    override fun onNewToken(newToken: String) {
        //TODO update token in api
    }

    companion object {
        private const val TAG = "FCMMessage"
    }
}