package com.ayush.flow.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.ayush.flow.R
import com.ayush.flow.Services.BackgroundService
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Splash : AppCompatActivity() {
    var firebaseuser: FirebaseUser?=null
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        FirebaseApp.initializeApp(this)
        firebaseuser= FirebaseAuth.getInstance().currentUser
        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)

        val isLoggedIn=sharedPreferences.getBoolean("isLoggedIn",false)

        Handler().postDelayed({
            if(isLoggedIn){
                Dashboard().checkStatus().execute()
                val username=sharedPreferences.getString("name","")
                startService(Intent(this, BackgroundService::class.java))
                if(username==""){
                    startActivity(Intent(this@Splash,Addprofile::class.java))
                    finish()
                }
                else{
                    startActivity(Intent(this@Splash,Dashboard::class.java))
                    finish()
                }
            }
            else{
                startActivity(Intent(this,Register::class.java))
                finish()
            }
        },300)
    }
}