package com.example.safepoint

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.rybalkinsd.kohttp.dsl.async.httpPostAsync
import io.github.rybalkinsd.kohttp.ext.url
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signin.password
import kotlinx.android.synthetic.main.activity_signin.register
import kotlinx.android.synthetic.main.activity_signin.username
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        login.setOnClickListener{
           GlobalScope.launch  {
               val response = httpPostAsync {
                   url("http://10.0.2.2:5000/connect/token")

                   body {
                       form {
                           "grant_type" to "password"
                           "username" to username.text
                           "password" to password.text
                           "client_id" to "test_auth"
                           "client_secret" to ""
                           "scope" to "offline_access api"
                       }
                   }
               }.await()
               if(!response.isSuccessful){
                   //Todo show error
                   return@launch
               }
               val userInfo = JSONObject(response.body()?.string() ?: return@launch)

               val userSettings = getSharedPreferences(getString(R.string.user_settings), Context.MODE_PRIVATE)
                with(userSettings.edit()){
                    putString(getString(R.string.refresh_token),
                        userInfo[getString(R.string.refresh_token)] as String?
                    )
                    putString(getString(R.string.access_token),
                        userInfo[getString(R.string.access_token)] as String?
                    )
                    putInt(getString(R.string.expires_in),
                        userInfo[getString(R.string.expires_in)] as Int
                    )
                    apply()
                }
                Intent(applicationContext, NavigationActivity::class.java)
                startActivity(intent)
                finish()
           }
        }

        register.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}