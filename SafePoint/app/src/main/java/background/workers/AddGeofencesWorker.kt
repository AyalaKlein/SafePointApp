package background.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.example.safepoint.background.LocationService
import com.google.android.gms.maps.model.LatLng
import utils.GeofencesManager
import utils.PermissionsUtils

class AddGeofencesWorker(
    var context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    var geofencesManager: GeofencesManager = GeofencesManager.createGeofenceManager(context, PermissionsUtils(context))!!

    override suspend fun doWork(): Result {
        registerGeofences()
        return Result.success()
    }

    private fun registerGeofences() {
        LocationService.init(context, null)
        //TODO replace with assigned shelter
        val location = LocationService.getLastLocation().result
        if(location != null){
            geofencesManager.addOrUpdateGeofence("shelter", LatLng(location.latitude, location.longitude), 100)
            geofencesManager.registerGeofences()
        }
    }

    fun permissionsMissing() {}

    companion object {
        const val TAG = "PRACOWNIA_PIWA"
        val reqeust: OneTimeWorkRequest
            get() = OneTimeWorkRequest.Builder(AddGeofencesWorker::class.java).build()
    }
}