package com.example.safepoint.dataCache

import android.os.Build.HOST
import com.example.safepoint.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.github.rybalkinsd.kohttp.ext.httpGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.json.JSONArray
import org.json.JSONObject

class SheltersCache {
    companion object {
        private var shelters = JSONArray()

        fun getShelters() : JSONArray {
            return shelters
        }

        fun getShelters(locX: Double, locY: Double, radius: Double, callback: ((JSONObject) -> Unit)?) {
            GlobalScope.launch {
                var result = JSONObject("${BuildConfig.HOST}/api/Shelters/GetNearestShelters?locX=${locX}&locY=${locY}&meterRadius=${radius}".httpGet().body()!!.string())
                shelters = result.getJSONArray("shelters")
                callback?.let { it(result) }
            }
        }
    }
}