package com.example.safepoint

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.rybalkinsd.kohttp.dsl.async.httpPostAsync
import io.github.rybalkinsd.kohttp.ext.url
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signin.password
import kotlinx.android.synthetic.main.activity_signin.signup
import kotlinx.android.synthetic.main.activity_signin.username

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        register.setOnClickListener{
            httpPostAsync {
                url("${BuildConfig.HOST}/api/accounts/register")

                body { json {
                    "username" to username
                    "password" to password
                } }
            }.invokeOnCompletion {
                Intent(applicationContext, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }

        }
    }
}