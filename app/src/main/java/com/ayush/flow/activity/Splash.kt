package com.ayush.flow.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.*
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProviders
import com.ayush.flow.R
import com.ayush.flow.Services.Constants
import com.ayush.flow.Services.RetrieveMessage
import com.ayush.flow.Services.SharedPreferenceUtils
import com.ayush.flow.database.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import java.util.*
import kotlin.properties.Delegates


class Splash : AppCompatActivity() {
    var firebaseuser: FirebaseUser?=null
    lateinit var hiddenViewModel: HiddenViewModel
    var isLoggedIn by Delegates.notNull<Boolean>()
    
    override fun onCreate(savedInstanceState: Bundle?) {

        hiddenViewModel= ViewModelProviders.of(this).get(HiddenViewModel::class.java)
        SharedPreferenceUtils.init(applicationContext)
        intializeTheme()
        super.onCreate(savedInstanceState)
        
        FirebaseApp.initializeApp(this)
        firebaseuser= FirebaseAuth.getInstance().currentUser
        isLoggedIn=SharedPreferenceUtils.getBooleanPreference(SharedPreferenceUtils.IS_LOGGED,false)

        SharedPreferenceUtils.setIntPreference(SharedPreferenceUtils.IS_PRIVATE,0)

        if(isLoggedIn){
            callDashboard()
            RetrieveMessage(application).execute()
        }
        else{
            Handler().postDelayed({
                val intent = Intent(this,Slider::class.java)
                openActivity(intent)
            },300)
        }
    }

    private fun openActivity(intent: Intent) {
        startActivity(intent)
        finishAffinity()
    }

    private fun intializeTheme() {
        if(SharedPreferenceUtils.getBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun callDashboard() {
        val username=SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_NAME,"")
        if(username==""){
            openActivity(Intent(this@Splash,Addprofile::class.java))
        }
        else{
            Handler().postDelayed({
                openActivity(Intent(this,Dashboard::class.java))
            },200)
        }
    }
}