package com.ayush.flow.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.ayush.flow.R
import com.ayush.flow.Services.BackgroundService
import com.ayush.flow.Services.Permissions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Splash : AppCompatActivity() {
    var firebaseuser: FirebaseUser?=null
    lateinit var sharedPreferences: SharedPreferences

    var runnable: Runnable? = null
    var delay = 800

    lateinit var logo: ImageView
    lateinit var shine: ImageView
    lateinit var mode:ImageView
    lateinit var theme:SwitchCompat
    override fun onCreate(savedInstanceState: Bundle?) {

        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)
        if(sharedPreferences.getBoolean("nightMode",false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            // theme.isChecked=true
        }

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_splash)

        logo=findViewById(R.id.logo)
        shine=findViewById(R.id.shine)



        FirebaseApp.initializeApp(this)
        firebaseuser= FirebaseAuth.getInstance().currentUser



        val isLoggedIn=sharedPreferences.getBoolean("isLoggedIn",false)

        if(isLoggedIn){
            if(Permissions().checkContactpermission(this)){
                Dashboard().loadContacts(application).execute()
            }
        }

     //   startShine()

//        Handler().postDelayed(Runnable {
//            Handler().postDelayed(runnable!!, delay.toLong())
//            startShine()
//        }.also { runnable = it }, delay.toLong())

        Handler().postDelayed({
            if(isLoggedIn){
                Dashboard().retrieveMessage(application).execute()
                Dashboard().checkStatus().execute()
                val username=sharedPreferences.getString("name","")
                startService(Intent(this, BackgroundService::class.java))
                if(username==""){
                    startActivity(Intent(this@Splash,Addprofile::class.java))
                    finishAffinity()
                }
                else{
                    val intent = Intent(this,Dashboard::class.java)
                    intent.putExtra("private",0)
                    startActivity(intent)
                    finish()
                }
            }
            else{
                startActivity(Intent(this,Slider::class.java))
                finish()
            }
        },1200)


    }

    fun startShine(){


        val animation: Animation = TranslateAnimation(0F,
            (logo.width + shine.width).toFloat(), 0F, 0F
        )
        animation.duration = 800
        animation.fillAfter = false
        animation.interpolator = AccelerateDecelerateInterpolator()
        shine.startAnimation(animation)
    }
}