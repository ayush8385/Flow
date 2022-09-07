package com.ayush.flow.utils

import android.app.Application
import android.os.*
import com.ayush.flow.database.*
import com.google.firebase.database.*

class RetrieveMessage(val applicat: Application):AsyncTask<Void,Void,Boolean>() {
    override fun doInBackground(vararg params: Void?): Boolean {
        val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(Constants.MY_USERID)
        ref.addChildEventListener(object : ChildEventListener {
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

                var decryptedMsg = msg
                if(type!="doc"){
                    decryptedMsg = AESEncryption().decrypt(msg)!!
                }

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
                    saveMessageToDatabase(type,name,number,
                        decryptedMsg!!,imagepath,sender,messageKey,time,hide,unread,thumbnail,msg_url,msgStatus)
                } else if (ChatViewModel(applicat).isUserExist(sender)) {
                    //get image and name from room db
                    val chatEntity = ChatViewModel(applicat).getChat(sender)
                    name = chatEntity.name
                    number = chatEntity.number
                    imagepath = chatEntity.image
                    unread = chatEntity.unread + 1
                    hide = chatEntity.hide
                    saveMessageToDatabase(type,name,number,decryptedMsg!!,imagepath,sender,messageKey,time,hide,unread,thumbnail,msg_url,msgStatus)
                } else {
                    val refer =
                        FirebaseDatabase.getInstance().reference.child("Users").child(sender)
                    refer.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            number = snapshot.child("number").value.toString()
                            val image_url = snapshot.child("profile_photo").value.toString()
                            if (image_url != "") {
                                ImageHandling(applicat).GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION, sender + ".jpg","profile").execute(image_url)
                                hide = false
                                unread = 1
                            }
                            saveMessageToDatabase(type,name,number,decryptedMsg!!,imagepath,sender,messageKey,time,hide,unread,thumbnail,msg_url,msgStatus)
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
        MessageViewModel(applicat).insertMessage(MessageEntity(messageKey,
            Constants.MY_USERID+"-"+sender,sender,message,time.toLong(),type,"","",thumbnail,msg_url, msgStatus))
    }

}

