package background.services

import android.content.Context
import android.util.Log
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

class SafePointFCMService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "From: ${remoteMessage.from}")
            LocationService.getLastLocation().addOnCompleteListener {
                val userSettings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
                val userFcmToken = userSettings.getString(getString(R.string.fcm_token), "")
                if (it.result != null) {
                    GlobalScope.launch {
                        val response = httpGetAsync {
                            url("http://10.0.2.2:5000/api/shelters/SearchForShelter?operationGuid=${remoteMessage.data["operationGuid"]}&fcmToken=${userFcmToken}&locX=${it.result!!.longitude}&locY=${it.result!!.latitude}")
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

    override fun onNewToken(newToken: String) {
        val userSettings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
        val oldToken = userSettings.getString(getString(R.string.fcm_token), "")

        GlobalScope.launch {
            httpPostAsync {
                url("http://10.0.2.2:5000/api/shelters/ChangeUserFcmToken")
                body {
                    form {
                        "oldToken" to oldToken
                        "newToken" to newToken
                    }
                }
            }.await()
            with(userSettings.edit()){
                putString(getString(R.string.fcm_token), newToken)
                commit()
            }
        }
    }

    companion object {
        private const val TAG = "FCMMessage"
    }
}