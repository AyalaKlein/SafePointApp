package com.example.safepoint.dataCache

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.github.rybalkinsd.kohttp.ext.httpGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.json.JSONArray

class SheltersCache {
    companion object {
        private var shelters = JSONArray()
        val scope = CoroutineScope(newFixedThreadPoolContext(1, "synchronizationPool"))

        fun getShelters() : JSONArray {
            return shelters
        }

        fun getShelters(locX: Double, locY: Double, radius: Double, callback: ((JSONArray) -> Unit)?) {
            scope.launch {

                shelters = JSONArray("http://10.0.2.2:5000/api/Shelters/GetNearestShelters?locX=${locX}&locY=${locY}&meterRadius=${radius}".httpGet().body()!!.string())
                callback?.let { it(shelters) }
            }
        }
    }
}