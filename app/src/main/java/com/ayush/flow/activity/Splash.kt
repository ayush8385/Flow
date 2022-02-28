package com.ayush.flow.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.ayush.flow.R
import com.ayush.flow.Services.BackgroundService
import com.ayush.flow.Services.Permissions
import com.ayush.flow.database.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Runnable
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class Splash : AppCompatActivity() {
    var firebaseuser: FirebaseUser?=null
    lateinit var sharedPreferences: SharedPreferences

    var runnable: Runnable? = null
    var delay = 800

//    lateinit var logo: ImageView
//    lateinit var shine: ImageView
//    lateinit var mode:ImageView
    lateinit var theme:SwitchCompat
    var isLoggedIn by Delegates.notNull<Boolean>()


    override fun onCreate(savedInstanceState: Bundle?) {

        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)
        if(sharedPreferences.getBoolean("nightMode",false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        super.onCreate(savedInstanceState)

//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        FirebaseApp.initializeApp(this)
        firebaseuser= FirebaseAuth.getInstance().currentUser
        isLoggedIn=sharedPreferences.getBoolean("isLoggedIn",false)


        if(isLoggedIn){
            callDashboard()
            retrieveMessage(application).execute()
        }
        else{
//            Handler().postDelayed({
                startActivity(Intent(this,Slider::class.java))
                finishAffinity()
//            },1000)
        }






    }

    private fun callDashboard() {
        GlobalScope.launch{
            checkStatus()
        }
        val username=sharedPreferences.getString("name","")
        if(username==""){
            startActivity(Intent(this@Splash,Addprofile::class.java))
            finishAffinity()
        }
        else{
            Handler().postDelayed({
                val intent = Intent(this,Dashboard::class.java)
                intent.putExtra("private",0)
                startActivity(intent)
                finish()
            },1500)
        }
    }

    fun checkStatus(){
        var firebaseUser=FirebaseAuth.getInstance().currentUser!!
        val connectionReference= FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        val lastConnected= FirebaseDatabase.getInstance().reference.child("lastConnected")
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





    inner class GetImageFromUrl(val userid: String,val appl:Application) : AsyncTask<String?, Void?, Bitmap>() {
        var bmp: Bitmap?=null

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            saveToInternalStorage(bmp!!,userid,appl).execute()
        }

        override fun doInBackground(vararg url: String?): Bitmap {
            val stringUrl = url[0]
            val options = BitmapFactory.Options()
            options.inSampleSize = 1
            while (options.inSampleSize <= 32) {
                val inputStream = URL(stringUrl).openStream()
                try {
                    bmp= BitmapFactory.decodeStream(inputStream, null, options)
                    inputStream.close()
                    break
                } catch (outOfMemoryError: OutOfMemoryError) {
                }
                options.inSampleSize++
            }

            return bmp!!
        }
    }


    inner class saveToInternalStorage:AsyncTask<Void,Void,Boolean>{

        var path:String?=null
        var user:String?=null
        var bitmapImage:Bitmap?=null
        var app:Application?=null

        constructor(bitmapImage: Bitmap, user: String,appl: Application) : super() {
            this.user=user
            this.bitmapImage=bitmapImage
            app=appl
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            ChatViewModel(app!!).updatetChat(user!!,path!!)
            ContactViewModel(app!!).updateImage(user!!,path!!)
//            chatAdapter.notifyDataSetChanged()
        }

        override fun doInBackground(vararg params: Void?): Boolean {
            val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Contacts Images")
            if(directory.exists()){
                path=user+".jpg"
                var fos: FileOutputStream =
                    FileOutputStream(File(directory, path))
                try {
                    bitmapImage!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            else{
                directory.mkdirs()
                if (directory.isDirectory) {
                    path=user+".jpg"
                    val fos =
                        FileOutputStream(File(directory, path))
                    try {
                        bitmapImage!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            fos.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            return true
        }

    }

    inner class retrieveMessage(application: Application):AsyncTask<Void,Void,Boolean>(){

        override fun doInBackground(vararg params: Void?): Boolean {
            val firebaseUser=FirebaseAuth.getInstance().currentUser!!
            val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)

            ref.addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)

                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snapshot in snapshot.children){

                        val messageKey=snapshot.child("mid").value.toString()
                        val user=snapshot.child("userid").value.toString()
                        val sender=snapshot.child("sender").value.toString()
                        var msg=snapshot.child("message").value.toString()
                        val time=snapshot.child("time").value.toString()
                        val type=snapshot.child("type").value.toString()
                        val url=snapshot.child("url").value.toString()
                        val received=snapshot.child("received").value as Boolean
                        val seen=snapshot.child("seen").value as Boolean

                        //time set
                        if(seen || received){
                            continue
                        }

                        var tm: Date = Date(time.toLong())

                        val sdf = SimpleDateFormat("hh:mm a")
                        val date= SimpleDateFormat("dd/MM/yy")

                        var name:String=""
                        var number:String=""
                        var imagepath:String=""


                        if(ContactViewModel(application).isUserExist(sender)){
                            val contactEntity=ContactViewModel(application).getContact(sender)
                            name=contactEntity.name
                            number=contactEntity.number
                            imagepath=contactEntity.image
                            if(type=="image"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),false,0,sender))
                            }
                            if(type=="doc"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),false,0,sender))
                            }
                            if(type=="message"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),date.format(tm),false,0,sender))
                            }
                        }
                        else if(ChatViewModel(application).isUserExist(sender)){
                            //get image and name from room db
                            val chatEntity=ChatViewModel(application).getChat(sender)
                            name=chatEntity.name
                            number=chatEntity.number
                            imagepath=chatEntity.image
                            var unread=chatEntity.unread
                            val hide=chatEntity.hide
                            if(type=="image"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),hide,unread++,sender))
                            }
                            if(type=="doc"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),hide,unread++,sender))
                            }
                            if(type=="message"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg,sdf.format(tm),date.format(tm),hide,unread++,sender))
                            }
                        }
                        else{
                            //get number as a name from firebase
                            //get image and sav it to local storage and internal path from firebase
                            val refer=FirebaseDatabase.getInstance().reference.child("Users").child(sender)
                            refer.addValueEventListener(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    number=snapshot.child("number").value.toString()
                                    //check message type
                                    if(type=="image"){
                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),false,0,sender))
                                    }
                                    else{
                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),date.format(tm),false,0,sender))
                                    }

                                    //get image of sender
                                    val image_url=snapshot.child("profile_photo").value.toString()
                                    if(image_url!=""){
                                        GetImageFromUrl(sender,application).execute(image_url)
                                        if(type=="image"){
                                            ChatViewModel(application).inserChat(ChatEntity(name,number,"","Photo", sdf.format(tm),date.format(tm),false,1,sender))
                                        }
                                        if(type=="doc"){
                                            ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),false,1,sender))
                                        }
                                        if(type=="message"){
                                            ChatViewModel(application).inserChat(ChatEntity(name,number,"",msg, sdf.format(tm),date.format(tm),false,1,sender))
                                        }
                                    }
                                    //     ContactViewModel(application).inserContact(ContactEntity(name,number,"",sender))

                                }
                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }

                        if(type=="image"){

                            MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid + "-" + sender,sender,messageKey+".jpg",sdf.format(tm),date.format(tm),type,url,false,false,false))
//                            MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,"",sdf.format(tm),type,false,false,false))

                            val photo =  GetImageFromUrl().execute(msg).get()

                            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
                                if (Environment.isExternalStorageManager()) {
                                    Toast.makeText(applicationContext,"Hello",Toast.LENGTH_SHORT).show()
                                    val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Images")
                                    if(!directory.exists()){
                                        directory.mkdirs()
                                    }
                                    msg=messageKey+".jpg"
                                    var fos: FileOutputStream =
                                        FileOutputStream(File(directory, msg))
                                    try {
                                              photo.compress(Bitmap.CompressFormat.JPEG, 25, fos)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        try {
                                            fos.close()
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                    }
                                } else {
                                    //request for the permission
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    val uri = Uri.fromParts("package", packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                }
                            }
                            else{
                                val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Images")
                                if(!directory.exists()){
                                    directory.mkdirs()
                                }
                                msg=messageKey+".jpg"
                                var fos: FileOutputStream =
                                    FileOutputStream(File(directory,msg))
                                try {
                                    photo.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    try {
                                        fos.close()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }
                                MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid + "-" + sender,sender,msg,sdf.format(tm),date.format(tm),type,url,false,false,false))
                            }
                        }

                        MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,msg,sdf.format(tm),date.format(tm),type,url,false,false,false))


                        // sendNotification(sender,name,msg,imagepath,application).execute()
                        val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)
                        refer.addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.child(messageKey).exists()){
                                    FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid).child(messageKey).child("received").setValue(true)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            return true
        }

    }


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