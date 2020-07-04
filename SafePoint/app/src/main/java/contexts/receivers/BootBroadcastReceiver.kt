package contexts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import background.workers.AddGeofencesWorker

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) return

        if (intent.action  == "android.intent.action.BOOT_COMPLETED") {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "AddGeofences",
                ExistingWorkPolicy.KEEP,
                AddGeofencesWorker.reqeust
            )
        }
    }

    companion object {
        private const val TAG = "PRAIRIE"
    }
}