package com.example.safepoint.dataCache

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.github.rybalkinsd.kohttp.ext.httpGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.json.JSONArray
import org.json.JSONObject

class SheltersCache {
    companion object {
        private var shelters = JSONArray()
        val scope = CoroutineScope(newFixedThreadPoolContext(1, "synchronizationPool"))

        fun getShelters() : JSONArray {
            return shelters
        }

        fun getShelters(locX: Double, locY: Double, radius: Double, callback: ((JSONObject) -> Unit)?) {
            scope.launch {
                var result = JSONObject("http://10.0.2.2:5000/api/Shelters/GetNearestShelters?locX=${locX}&locY=${locY}&meterRadius=${radius}".httpGet().body()!!.string())
                shelters = result.getJSONArray("shelters")
                callback?.let { it(result) }
            }
        }
    }
}