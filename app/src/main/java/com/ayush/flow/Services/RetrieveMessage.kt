package com.ayush.flow.Services

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import com.ayush.flow.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class RetrieveMessage(val applicat: Application):AsyncTask<Void,Void,Boolean>() {
    override fun doInBackground(vararg params: Void?): Boolean {
        val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID)
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
                    val msg_url=snapshot.child("url").value.toString()
                    val received=snapshot.child("received").value as Boolean
                    val seen=snapshot.child("seen").value as Boolean

                    //time set
                    if(seen || received){
                        continue
                    }

                    var name:String=""
                    var number:String=""
                    var imagepath:String=""
                    var unread:Int =0
                    var hide:Boolean=false

//                    var tm: Date = Date(time.toLong())
//
//                    val sdf = SimpleDateFormat("hh:mm a")
//                    val date= SimpleDateFormat("dd/MM/yy")
//                    sdf.format(tm),date.format(tm)

                    if(ContactViewModel(applicat).isUserExist(sender)){
                        val contactEntity= ContactViewModel(applicat).getContact(sender)
                        name=contactEntity.name
                        number=contactEntity.number
                        imagepath=contactEntity.image
                    }
                    else if(ChatViewModel(applicat).isUserExist(sender)){
                        //get image and name from room db
                        val chatEntity= ChatViewModel(applicat).getChat(sender)
                        name=chatEntity.name
                        number=chatEntity.number
                        imagepath=chatEntity.image
                        unread=chatEntity.unread+1
                        hide=chatEntity.hide
                    }
                    else{
                        //get number as a name from firebase
                        //get image and sav it to local storage and internal path from firebase
                        val refer= FirebaseDatabase.getInstance().reference.child("Users").child(sender)
                        refer.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                number=snapshot.child("number").value.toString()
                                //check message type
//                                if(type=="image"){
//                                    ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,"Photo", time.toLong(),hide,unread,sender))
//                                }
//                                else{
//                                    ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),date.format(tm),false,0,sender))
//                                }

                                //get image of sender
                                val image_url=snapshot.child("profile_photo").value.toString()
                                if(image_url!=""){
                                    ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,sender+".jpg").execute(image_url)
                                    hide=false
                                    unread=1
//                                    GetImageFromUrl(sender,application).execute(image_url)
//                                    if(type=="image"){
//                                        ChatViewModel(application).inserChat(ChatEntity(name,number,"","Photo", sdf.format(tm),date.format(tm),false,1,sender))
//                                    }
//                                    if(type=="doc"){
//                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),false,1,sender))
//                                    }
//                                    if(type=="message"){
//                                        ChatViewModel(application).inserChat(ChatEntity(name,number,"",msg, sdf.format(tm),date.format(tm),false,1,sender))
//                                    }
                                }
                                //     ContactViewModel(application).inserContact(ContactEntity(name,number,"",sender))

                            }
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }


                    if(type=="image"){
                        ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,"Photo",time.toLong(),hide,unread,sender))

//                            MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,"",sdf.format(tm),type,false,false,false))

                        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
                            if (Environment.isExternalStorageManager()) {
//                                ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,messageKey+".jpg").execute(msg_url)
                                val msg = MessageEntity(messageKey,Constants.MY_USERID+"-"+sender,sender,msg,time.toLong(),type,msg_url,false,false,false)
                                saveImagefromUrlMsg(Constants.ALL_PHOTO_LOCATION,messageKey+".jpg",msg).execute(msg_url)
                            } else {
                                //request for the permission
//                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                                val uri = Uri.fromParts("package", packageName, null)
//                                intent.data = uri
//                                startActivity(intent)
                            }
                        }
                        else{
                            val msg = MessageEntity(messageKey,Constants.MY_USERID+"-"+sender,sender,msg,time.toLong(),type,msg_url,false,false,false)
                            saveImagefromUrlMsg(Constants.ALL_PHOTO_LOCATION,messageKey+".jpg",msg).execute(msg_url)
                        }

                    }
                    if(type=="doc"){
                        ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,"Document", time.toLong(),hide,unread,sender))
                    }
                    if(type=="message"){
                        ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,msg, time.toLong(),hide,unread,sender))
                    }

                    MessageViewModel(applicat).insertMessage(MessageEntity(messageKey,Constants.MY_USERID+"-"+sender,sender,msg,time.toLong(),type,msg_url,false,false,false))


                    // sendNotification(sender,name,msg,imagepath,application).execute()

                    //Set Received
                    val refer= FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID)
                    refer.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.child(messageKey).exists()){
                                FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID).child(messageKey).child("received").setValue(true)
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
    inner class saveImgMsgToInternalStorage(val bitmapImage:Bitmap,val location:String,val fileName:String,val msg: MessageEntity):AsyncTask<Void,Void,Boolean>(){
        val directory: File = File(Environment.getExternalStorageDirectory().toString(), location)
        var file: File = File(directory,fileName)
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            MessageViewModel(this@RetrieveMessage.applicat).insertMessage(msg)
        }
        override fun doInBackground(vararg params: Void?):Boolean {
            if(!directory.exists()){
                directory.mkdirs()
            }
            var fos: FileOutputStream = FileOutputStream(file)
            try {
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return true
        }
    }

    inner class saveImagefromUrlMsg(val location: String,val fileName: String,val msg:MessageEntity) : AsyncTask<String?, Void?, Boolean>() {
        var bmp: Bitmap?=null
        override fun doInBackground(vararg url: String?): Boolean {
            val url: URL = mStringToURL(url[0]!!)!!
            val connection: HttpURLConnection?
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)
                bmp= BitmapFactory.decodeStream(bufferedInputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            Log.e("starteddd","compressed")
            saveImgMsgToInternalStorage(bmp!!,location,fileName,msg).execute()
        }

        private fun mStringToURL(string: String): URL? {
            try {
                return URL(string)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return null
        }
    }
}

