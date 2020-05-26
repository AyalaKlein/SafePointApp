package com.example.safepoint

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

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        register.setOnClickListener{

            GlobalScope.launch {

                val response = httpPostAsync {
                    url("http://10.0.2.2:5000/api/accounts/register")

                    body { json {
                        "username" to username
                        "password" to password
                        "email" to email
                    } }
                }.await()
                if(!response.isSuccessful){
                    //todo show error
                    return@launch
                }
                Intent(applicationContext, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}