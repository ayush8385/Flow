package com.ayush.flow.activity

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProviders
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates


class Splash : AppCompatActivity() {
    var firebaseuser: FirebaseUser?=null
    lateinit var sharedPreferences: SharedPreferences
    lateinit var hiddenViewModel: HiddenViewModel
    var isLoggedIn by Delegates.notNull<Boolean>()


    override fun onCreate(savedInstanceState: Bundle?) {

        hiddenViewModel= ViewModelProviders.of(this).get(HiddenViewModel::class.java)
        SharedPreferenceUtils.init(applicationContext)

        intializeTheme()

        super.onCreate(savedInstanceState)

        setFullScreen()

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
                startActivity(Intent(this,Slider::class.java))
                finishAffinity()
            },300)
        }
    }

    private fun setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun intializeTheme() {
        if(SharedPreferenceUtils.getBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun callDashboard() {
        GlobalScope.launch{
            checkStatus()
        }
        val username=SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_NAME,"")
        if(username==""){
            startActivity(Intent(this@Splash,Addprofile::class.java))
            finishAffinity()
        }
        else{
            Handler().postDelayed({
                startActivity(Intent(this,Dashboard::class.java))
                finish()
            },200)
        }
    }

    fun checkStatus(){
        val connectionReference= FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID)
//        val lastConnected= FirebaseDatabase.getInstance().reference.child("lastConnected")
        val infoConnected= FirebaseDatabase.getInstance().getReference(".info/connected")

        infoConnected.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected:Boolean=snapshot.value as Boolean

                if(connected){
                    val con=connectionReference.child("status")
                    con.setValue("online")
                    con.onDisconnect().setValue("")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }




//
//    inner class GetImageFromUrl(val userid: String,val appl:Application) : AsyncTask<String?, Void?, Bitmap>() {
//        var bmp: Bitmap?=null
//
//        override fun onPostExecute(result: Bitmap?) {
//            super.onPostExecute(result)
//            saveToInternalStorage(bmp!!,userid,appl).execute()
//        }
//
//        override fun doInBackground(vararg url: String?): Bitmap {
//            val stringUrl = url[0]
//            val options = BitmapFactory.Options()
//            options.inSampleSize = 1
//            while (options.inSampleSize <= 32) {
//                val inputStream = URL(stringUrl).openStream()
//                try {
//                    bmp= BitmapFactory.decodeStream(inputStream, null, options)
//                    inputStream.close()
//                    break
//                } catch (outOfMemoryError: OutOfMemoryError) {
//                }
//                options.inSampleSize++
//            }
//
//            return bmp!!
//        }
//    }


//    inner class saveToInternalStorage:AsyncTask<Void,Void,Boolean>{
//
//        var path:String?=null
//        var user:String?=null
//        var bitmapImage:Bitmap?=null
//        var app:Application?=null
//
//        constructor(bitmapImage: Bitmap, user: String,appl: Application) : super() {
//            this.user=user
//            this.bitmapImage=bitmapImage
//            app=appl
//        }
//
//        override fun onPostExecute(result: Boolean?) {
//            super.onPostExecute(result)
//            ChatViewModel(app!!).updatetChat(user!!,path!!)
//            ContactViewModel(app!!).updateImage(user!!,path!!)
////            chatAdapter.notifyDataSetChanged()
//        }
//
//        override fun doInBackground(vararg params: Void?): Boolean {
//            val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Contacts Images")
//            if(directory.exists()){
//                path=user+".jpg"
//                var fos: FileOutputStream =
//                    FileOutputStream(File(directory, path))
//                try {
//                    bitmapImage!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                } finally {
//                    try {
//                        fos.close()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//            else{
//                directory.mkdirs()
//                if (directory.isDirectory) {
//                    path=user+".jpg"
//                    val fos =
//                        FileOutputStream(File(directory, path))
//                    try {
//                        bitmapImage!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    } finally {
//                        try {
//                            fos.close()
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
//            return true
//        }
//
//    }

//
//    fun startShine(){
//        val animation: Animation = TranslateAnimation(0F,
//            (logo.width + shine.width).toFloat(), 0F, 0F
//        )
//        animation.duration = 800
//        animation.fillAfter = false
//        animation.interpolator = AccelerateDecelerateInterpolator()
//        shine.startAnimation(animation)
//    }
}