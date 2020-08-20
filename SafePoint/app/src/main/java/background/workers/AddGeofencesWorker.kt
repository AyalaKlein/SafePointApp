package background.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.example.safepoint.R
import com.example.safepoint.background.LocationService
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.double
import kotlinx.serialization.parse
import models.Shelter
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
        val shelter = context.getSharedPreferences(
            context.getString(R.string.user_settings),
            Context.MODE_PRIVATE
        ).getString(context.getString(R.string.selected_shelter), "")

        val lat: Double?
        val lon: Double?
        if (shelter != null && shelter != "") {
            val json = Json(JsonConfiguration.Stable)
            val shelterJson = json.parseJson(shelter)
            lat = shelterJson.jsonObject["locY"]!!.double
            lon = shelterJson.jsonObject["locX"]!!.double
        } else {
            return
        }

        geofencesManager.addOrUpdateGeofence(
            "shelter",
            LatLng(lat, lon),
            100
        )
        geofencesManager.registerGeofences()
    }

    fun permissionsMissing() {}

    companion object {
        const val TAG = "PRACOWNIA_PIWA"
        val reqeust: OneTimeWorkRequest
            get() = OneTimeWorkRequest.Builder(AddGeofencesWorker::class.java).build()
    }
}