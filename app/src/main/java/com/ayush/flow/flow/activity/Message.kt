package com.ayush.flow.flow.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.flow.adapter.MessageAdapter
import com.ayush.flow.flow.model.Message


class Message : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    var msgList= arrayListOf<Message>()
    lateinit var more:ImageView
    lateinit var name:TextView
    lateinit var more_card:CardView
    lateinit var parent:RelativeLayout
    lateinit var audiocall:ImageView
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

        audiocall=findViewById(R.id.call)


        audiocall.setOnClickListener {
            startActivity(Intent(this,Outgoing::class.java))
        }



        name.text=intent.getStringExtra("name")

        msgList.add(Message(1,"Hello how's goign your app working fine"))
        msgList.add(Message(1,"Hello how's goign your app working gjbjhgjgj fine hghjvhj"))
        msgList.add(Message(2,"Hello h Hello how's goign your app working fine hghjvhj ow's goign your app working fine"))
        msgList.add(Message(1,"Hello how's goign your apine"))
        msgList.add(Message(2,"Hello how's goign your app working fine"))
        msgList.add(Message(1,"Hello ine"))
        msgList.add(Message(2,"Hello how's goign your app fine"))
        msgList.add(Message(2,"Hello he"))
        msgList.add(Message(1,"Hello how's goign yHello how's goign your app working fine hghjvhj"))
        msgList.add(Message(1,"Hello how's goign your app workg fine"))
        msgList.add(Message(1,"Hello horking fine"))
        msgList.add(Message(2,"Hello how's goign your app working fine"))
        msgList.add(Message(1,"Helloi"))
        msgList.add(Message(2,"Hello how's goign your app working fine"))
        msgList.add(Message(2,"Hello how's goign your app working fine"))
        msgList.add(Message(1,"Hello ho hgj"))
        msgList.add(Message(1,"Hel"))

        recyclerView.adapter=MessageAdapter(this,msgList)
        recyclerView.layoutManager=layoutManager


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

    }
}