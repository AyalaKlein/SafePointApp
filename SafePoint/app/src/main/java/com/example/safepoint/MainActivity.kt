package com.example.safepoint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.api.Response
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.ext.asyncHttpGet
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.ext.httpGetAsync
import io.github.rybalkinsd.kohttp.util.Json
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Set listener to Home Front Command API

        var json = ""
//        "http://localhost:5000/api/shelters".httpGet().isSuccessful() =

        var res = ""
        val scope = CoroutineScope(newFixedThreadPoolContext(1, "synchronizationPool"))
        scope.launch {
            res = "http://10.0.2.2:5000/api/shelters".httpGet().body()!!.string()
//\            val shelters = JSONArray(res)
            val intent = Intent(applicationContext, NavigationActivity::class.java)

            intent.putExtra("shelters", res)
            startActivity(intent)
            finish()
        }


        profileSettings.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}
