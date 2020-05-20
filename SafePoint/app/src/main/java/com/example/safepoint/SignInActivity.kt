package com.example.safepoint

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_signin.*

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        login.setOnClickListener{

        }

        register.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}