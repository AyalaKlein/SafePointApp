package background.workers

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import utils.GeofencesManager
import utils.PermissionsUtils

class AddGeofencesWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    var geofencesManager: GeofencesManager = GeofencesManager.createGeofenceManager(context, PermissionsUtils(context))!!

    override fun doWork(): Result {
        registerGeofences()
        return Result.success()
    }

    private fun registerGeofences() {
        geofencesManager.registerGeofences()
    }

    fun permissionsMissing() {}

    companion object {
        const val TAG = "PRACOWNIA_PIWA"
        val reqeust: OneTimeWorkRequest
            get() = OneTimeWorkRequest.Builder(AddGeofencesWorker::class.java).build()
    }
}