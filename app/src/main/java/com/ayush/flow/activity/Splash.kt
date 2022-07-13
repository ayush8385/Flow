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
//
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
                startActivity(Intent(this,Slider::class.java))
                finishAffinity()
            },300)
        }
    }

    private fun intializeTheme() {
        if(SharedPreferenceUtils.getBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun callDashboard() {
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