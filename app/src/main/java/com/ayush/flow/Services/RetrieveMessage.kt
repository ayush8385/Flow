package com.ayush.flow.Services

import android.app.Application
import android.os.*
import com.ayush.flow.database.*
import com.google.firebase.database.*

class RetrieveMessage(val applicat: Application):AsyncTask<Void,Void,Boolean>() {
    override fun doInBackground(vararg params: Void?): Boolean {
        val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID)
        ref.addChildEventListener(object : ChildEventListener {
//                        @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
//            override fun onDataChange(snapshot: DataSnapshot) {
//                var c=0
//                for(snapshot in snapshot.children){
//                    val messageKey=snapshot.child("mid").value.toString()
//                    val user=snapshot.child("userid").value.toString()
//                    val sender=snapshot.child("sender").value.toString()
//                    var msg=snapshot.child("message").value.toString()
//                    val time=snapshot.child("time").value.toString()
//                    val type=snapshot.child("type").value.toString()
//                    val msg_url=snapshot.child("url").value.toString()
//                    val received=snapshot.child("received").value as Boolean
//                    val seen=snapshot.child("seen").value as Boolean
//
//                    Log.e("messages",seen.toString())
//                    if(seen){
//                        continue
//                    }
//                    Log.e("messages_count",c.toString())
//                    c++;
//
//                    //time set
//                    var msgExist =false
//                    GlobalScope.launch(Dispatchers.IO) {
//                        msgExist = MessageViewModel(applicat).isMsgExist(messageKey)
//                    }
//                    if(!msgExist){
//                        Log.e("messages",c.toString())
//                        c++;
//
//
//                        var name:String=""
//                        var number:String=""
//                        var imagepath:String=""
//                        var unread:Int = 0
//                        var hide:Boolean=false
//
//                        if(ContactViewModel(applicat).isUserExist(sender)){
//                            val contactEntity= ContactViewModel(applicat).getContact(sender)
//                            name=contactEntity.name
//                            number=contactEntity.number
//                            imagepath=contactEntity.image
//                            if(ChatViewModel(applicat).isUserExist(sender)){
//                                val chatEntity= ChatViewModel(applicat).getChat(sender)
//                                unread=chatEntity.unread+1
//                            }
//                        }
//                        else if(ChatViewModel(applicat).isUserExist(sender)){
//                            //get image and name from room db
//                            val chatEntity= ChatViewModel(applicat).getChat(sender)
//                            name=chatEntity.name
//                            number=chatEntity.number
//                            imagepath=chatEntity.image
//                            unread=chatEntity.unread+1
//                            hide=chatEntity.hide
//                        }
//                        else{
//                            //get number as a name from firebase
//                            //get image and sav it to local storage and internal path from firebase
//                            val refer= FirebaseDatabase.getInstance().reference.child("Users").child(sender)
//                            refer.addValueEventListener(object : ValueEventListener {
//                                override fun onDataChange(snapshot: DataSnapshot) {
//                                    number=snapshot.child("number").value.toString()
//                                    //check message type
////                                if(type=="image"){
////                                    ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,"Photo", time.toLong(),hide,unread,sender))
////                                }
////                                else{
////                                    ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),date.format(tm),false,0,sender))
////                                }
//
//                                    //get image of sender
//                                    val image_url=snapshot.child("profile_photo").value.toString()
//                                    if(image_url!=""){
//                                        ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,sender+".jpg").execute(image_url)
//                                        hide=false
//                                        unread=1
////                                    GetImageFromUrl(sender,application).execute(image_url)
////                                    if(type=="image"){
////                                        ChatViewModel(application).inserChat(ChatEntity(name,number,"","Photo", sdf.format(tm),date.format(tm),false,1,sender))
////                                    }
////                                    if(type=="doc"){
////                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),false,1,sender))
////                                    }
////                                    if(type=="message"){
////                                        ChatViewModel(application).inserChat(ChatEntity(name,number,"",msg, sdf.format(tm),date.format(tm),false,1,sender))
////                                    }
//                                    }
//                                    //     ContactViewModel(application).inserContact(ContactEntity(name,number,"",sender))
//
//                                }
//                                override fun onCancelled(error: DatabaseError) {
//                                    TODO("Not yet implemented")
//                                }
//
//                            })
//                        }
//
//
//                        if(type=="image"){
//                            ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,"Photo",time.toLong(),hide,unread,sender))
//
////                            MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,"",sdf.format(tm),type,false,false,false))
//
//                            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
//                                if (Environment.isExternalStorageManager()) {
////                                ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,messageKey+".jpg").execute(msg_url)
//                                    val msg = MessageEntity(messageKey,Constants.MY_USERID+"-"+sender,sender,msg,time.toLong(),type,"",msg_url,false,false,false)
//                                    saveImagefromUrlMsg(Constants.ALL_PHOTO_LOCATION,messageKey+".jpg",msg).execute(msg_url)
//                                } else {
//                                    //request for the permission
////                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
////                                val uri = Uri.fromParts("package", packageName, null)
////                                intent.data = uri
////                                startActivity(intent)
//                                }
//                            }
//                            else{
//                                val msg = MessageEntity(messageKey,Constants.MY_USERID+"-"+sender,sender,msg,time.toLong(),type,"",msg_url,false,false,false)
//                                saveImagefromUrlMsg(Constants.ALL_PHOTO_LOCATION,messageKey+".jpg",msg).execute(msg_url)
//                            }
//
//                        }
//                        if(type=="doc"){
//                            ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,"Document", time.toLong(),hide,unread,sender))
//                        }
//                        if(type=="message"){
//                            ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,msg, time.toLong(),hide,unread,sender))
//                        }
//
//                        MessageViewModel(applicat).insertMessage(MessageEntity(messageKey,Constants.MY_USERID+"-"+sender,sender,msg,time.toLong(),type,"",msg_url,true,false,false))
//
//
//                        // sendNotification(sender,name,msg,imagepath,application).execute()
//
//                        //Set Received
//                        val refer= FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID)
//                        refer.addListenerForSingleValueEvent(object : ValueEventListener {
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                if(snapshot.child(messageKey).exists()){
//                                    FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID).child(messageKey).child("received").setValue(true)
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                TODO("Not yet implemented")
//                            }
//
//                        })
//                    }
//
//
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageKey=snapshot.child("mid").value.toString()
                val user=snapshot.child("userid").value.toString()
                val sender=snapshot.child("sender").value.toString()
                var msg=snapshot.child("message").value.toString()
                val time=snapshot.child("time").value.toString()
                val type=snapshot.child("type").value.toString()
                val thumbnail=snapshot.child("thumbnail").value.toString()
                val msg_url=snapshot.child("url").value.toString()
                val msgStatus=snapshot.child("msgStatus").value.toString()

                if(msgStatus=="seen" || msgStatus=="Delivered"){
                    return
                }

                var name: String = ""
                var number: String = ""
                var imagepath: String = ""
                var unread: Int = 0
                var hide: Boolean = false

                if (ContactViewModel(applicat).isUserExist(sender)) {
                    val contactEntity = ContactViewModel(applicat).getContact(sender)
                    name = contactEntity.name
                    number = contactEntity.number
                    imagepath = contactEntity.image
                    if (ChatViewModel(applicat).isUserExist(sender)) {
                        val chatEntity = ChatViewModel(applicat).getChat(sender)
                        unread = chatEntity.unread + 1
                    }
                    saveMessageToDatabase(type,name,number,msg,imagepath,sender,messageKey,time,hide,unread,thumbnail,msg_url,msgStatus)
                } else if (ChatViewModel(applicat).isUserExist(sender)) {
                    //get image and name from room db
                    val chatEntity = ChatViewModel(applicat).getChat(sender)
                    name = chatEntity.name
                    number = chatEntity.number
                    imagepath = chatEntity.image
                    unread = chatEntity.unread + 1
                    hide = chatEntity.hide
                    saveMessageToDatabase(type,name,number,msg,imagepath,sender,messageKey,time,hide,unread,thumbnail,msg_url,msgStatus)
                } else {
                    val refer =
                        FirebaseDatabase.getInstance().reference.child("Users").child(sender)
                    refer.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            number = snapshot.child("number").value.toString()
                            val image_url = snapshot.child("profile_photo").value.toString()
                            if (image_url != "") {
                                ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION, sender + ".jpg").execute(image_url)
                                hide = false
                                unread = 1
                            }
                            saveMessageToDatabase(type,name,number,msg,imagepath,sender,messageKey,time,hide,unread,thumbnail,msg_url,msgStatus)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }
                //Set Received
                setMessageReceived(messageKey)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
  //              TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
    //            TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
    //            TODO("Not yet implemented")
            }
        })
        return true
    }

    private fun setMessageReceived(messageKey: String) {
        val refer= FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID)
        refer.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(messageKey).exists()){
                    FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID).child(messageKey).child("msgStatus").setValue("Delivered")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun saveMessageToDatabase(
        type: String,
        name: String,
        number: String,
        msg: String,
        imagepath: String,
        sender: String,
        messageKey: String,
        time: String,
        hide: Boolean,
        unread: Int,
        thumbnail: String,
        msg_url: String,
        msgStatus: String
    ){
        var path = ""
        var message = msg
        var lstmsg = msg
        if(type=="image"){
            message="Photo"
            path = sender+".jpg"
            lstmsg="Photo"
        }
        if(type=="doc"){
            path = sender
            lstmsg="Document"
        }
        ChatViewModel(applicat).inserChat(ChatEntity(name,number,imagepath,lstmsg,sender,messageKey,path, time.toLong(),hide,unread,sender))
        MessageViewModel(applicat).insertMessage(MessageEntity(messageKey,Constants.MY_USERID+"-"+sender,sender,message,time.toLong(),type,"","",thumbnail,msg_url, msgStatus))
    }

}

