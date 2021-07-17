package com.ayush.flow.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.adapter.MessageAdapter
import com.ayush.flow.database.ChatEntity
import com.ayush.flow.database.ChatViewModel
import com.ayush.flow.database.MessageEntity
import com.ayush.flow.database.MessageViewModel
import com.ayush.flow.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


class Message : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    var msgList= arrayListOf<Message>()
    lateinit var more:ImageView
    lateinit var name:TextView
    lateinit var more_card:CardView
    lateinit var parent:RelativeLayout
    lateinit var audiocall:ImageView
    lateinit var back:ImageView
    lateinit var send_txt:EditText
    lateinit var send:ImageView
    lateinit var image:CircleImageView
    lateinit var viewModel: MessageViewModel
    var userid:String=""
    var user_image:String=""
    lateinit var status:TextView
    lateinit var firebaseUser: FirebaseUser
    lateinit var adapter: MessageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        layoutManager=LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).stackFromEnd=true
        recyclerView=findViewById(R.id.message_recycler)
        name=findViewById(R.id.user_name)
        more=findViewById(R.id.more)
        more_card=findViewById(R.id.more_card)
        parent=findViewById(R.id.msg_parent)
        back=findViewById(R.id.back)
        send_txt=findViewById(R.id.send_text)
        send=findViewById(R.id.send_btn)
        image=findViewById(R.id.user_pic)
        status=findViewById(R.id.status)

        audiocall=findViewById(R.id.call)

        firebaseUser= FirebaseAuth.getInstance().currentUser!!

        viewModel=ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(MessageViewModel::class.java)

        back.setOnClickListener {
            onBackPressed()
        }

        audiocall.setOnClickListener {
            startActivity(Intent(this,Outgoing::class.java))
        }

        name.text=intent.getStringExtra("name")
        userid= intent.getStringExtra("userid")!!
        user_image=intent.getStringExtra("image")!!

        setIconImage(image).execute()

        adapter= MessageAdapter(this)
        recyclerView.layoutManager=layoutManager
        recyclerView.adapter=adapter


        viewModel.allMessages(firebaseUser.uid+"-"+userid).observe(this, Observer {list->
            list?.let {
                recyclerView.smoothScrollToPosition(list.size)
                adapter.updateList(list)
            }
        })

        more.setOnClickListener {
            if(more_card.visibility== View.GONE){
                more_card.visibility=View.VISIBLE
                val animFadein: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    R.anim.slide_up
                )
                more_card.startAnimation(animFadein)
            }
            else{
                more_card.visibility=View.GONE
                val animFadein: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    R.anim.slide_bottom
                )
                more_card.startAnimation(animFadein)
            }
        }

        send.setOnClickListener {
            val msg=send_txt.text.toString()
            if(msg!=""){
                send_txt.setText("")
                sendMessageToUser(msg).execute()
            }
        }

        val reference=FirebaseDatabase.getInstance().reference.child("Users").child(userid)
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val stat=snapshot.child("status").value.toString()
                if(stat=="online"){
                    status.text=stat
                    status.visibility=View.VISIBLE
                }
                else{
                    status.visibility=View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        Dashboard().checkStatus(firebaseUser)
       // deleteMessage()

    }


//    private fun deleteMessage() {
//        val id=userid+"-"+firebaseUser.uid
//        val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(id)
//        refer.addValueEventListener(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (snaps in snapshot.children){
//                    val seen=snaps.child("seen").value as Boolean
//                    val mid=snaps.child("mid").value.toString()
//                    if(seen==true){
//                        snaps.child(mid).ref.parent!!.removeValue()
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//    }

    inner class sendMessageToUser(val msg:String):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            val ref=FirebaseDatabase.getInstance().reference
            val messageKey=ref.push().key

            if(!viewModel.isMsgExist(messageKey!!)){
                viewModel.insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+userid,firebaseUser.uid,msg,"message"))
            }

            if(!ChatViewModel(application).isUserExist(userid)){
                ChatViewModel(application).deleteChat(userid)
            }
            ChatViewModel(application).inserChat(ChatEntity(intent.getStringExtra("name")!!,intent.getStringExtra("image")!!,msg,ServerValue.TIMESTAMP.toString(),userid))

            val messageHashmap=HashMap<String,Any>()
            messageHashmap.put("mid", messageKey!!)
            messageHashmap.put("userid",userid)
            messageHashmap.put("sender",firebaseUser.uid)
            messageHashmap.put("message",msg)
            messageHashmap.put("type","message")
            messageHashmap.put("received",false)
            messageHashmap.put("seen",false)

            ref.child("Messages").child(userid).child(messageKey).setValue(messageHashmap)

            return true
        }
    }

    inner class setIconImage(val image:CircleImageView):AsyncTask<Void,Void,Boolean>(){
        var b: Bitmap?=null
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            image.setImageBitmap(b)
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),user_image)
                b = BitmapFactory.decodeStream(FileInputStream(f))

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return true
        }

    }
}