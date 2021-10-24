package com.ayush.flow.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.ayush.flow.R
import com.ayush.flow.adapter.ViewPagerAdapter
import com.ayush.flow.model.ScreenItem
import com.google.android.material.tabs.TabLayout

class Slider : AppCompatActivity() {
    lateinit var viewPager: ViewPager
    lateinit var adapter: ViewPagerAdapter
    lateinit var tabIndicator:TabLayout
    lateinit var next:ImageButton
    lateinit var getStarted:Button
    var position:Int=0
    lateinit var skip:TextView
    val mList: ArrayList<ScreenItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_slider)



        tabIndicator=findViewById(R.id.tab_indicator)
        next=findViewById(R.id.next)
        skip=findViewById(R.id.skip)
        getStarted=findViewById(R.id.button)



        mList.add(ScreenItem("Chats","Send one to one messages to your friends in Realtime with one tap",R.mipmap.messages))
        mList.add(ScreenItem("Calls","Make a call to your friend with video/audio calling for free",R.mipmap.calls))
        mList.add(ScreenItem("Private","Hide your chat in Private section inside the application",R.mipmap.secure))
        mList.add(ScreenItem("Stories","Share your moments on the go with stories",R.mipmap.upload))

        viewPager=findViewById(R.id.viewpager)
        adapter=ViewPagerAdapter(this,mList)

        viewPager.adapter=adapter

        tabIndicator.setupWithViewPager(viewPager)

        next.setOnClickListener {

            position=viewPager.currentItem

            if(position<mList.size){
                position++
                viewPager.setCurrentItem(position,true)
            }

            if(position==mList.size-1){
                loadlastScreen()
            }

        }

        getStarted.setOnClickListener {
            openRegistration()
        }

        tabIndicator.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab!!.position==mList.size-1){
                    loadlastScreen()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        skip.setOnClickListener {
            loadlastScreen()
        }

    }

    private fun openRegistration() {
        startActivity(Intent(this,Register::class.java))
    }

    private fun loadlastScreen() {
        next.visibility=View.INVISIBLE
        skip.visibility=View.INVISIBLE
        viewPager.setCurrentItem(mList.size-1,true)

        getStarted.visibility=View.VISIBLE

    }
}