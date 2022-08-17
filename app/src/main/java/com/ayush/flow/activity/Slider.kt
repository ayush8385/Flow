package com.ayush.flow.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.ayush.flow.R
import com.ayush.flow.adapter.ViewPagerAdapter
import com.ayush.flow.databinding.ActivitySliderBinding
import com.ayush.flow.model.ScreenItem
import com.google.android.material.tabs.TabLayout

class Slider : AppCompatActivity() {
    lateinit var adapter: ViewPagerAdapter
    var position:Int=0
    val mList: ArrayList<ScreenItem> = ArrayList()
    lateinit var binding:ActivitySliderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_slider)

        mList.add(ScreenItem("Chats","Send one to one messages to your friends in Realtime with one tap",R.mipmap.messages))
        mList.add(ScreenItem("Calls","Make a call to your friend with video/audio calling for free",R.mipmap.calls))
        mList.add(ScreenItem("Private","Hide your chat in Private section inside the application",R.mipmap.secure))
        mList.add(ScreenItem("Stories","Share your moments on the go with stories",R.mipmap.upload))

        binding.viewpager.adapter=ViewPagerAdapter(this,mList)
        binding.tabIndicator.setupWithViewPager(binding.viewpager)

        binding.next.setOnClickListener {
            position=binding.viewpager.currentItem
            if(position<mList.size){
                position++
                binding.viewpager.setCurrentItem(position,true)
            }
            if(position==mList.size-1){
                loadlastScreen()
            }
        }

        binding.button.setOnClickListener {
            openRegistration()
        }

        binding.tabIndicator.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
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

        binding.skip.setOnClickListener {
            loadlastScreen()
        }

    }

    private fun openRegistration() {
        startActivity(Intent(this,Register::class.java))
    }

    private fun loadlastScreen() {
        binding.viewpager.setCurrentItem(mList.size-1,true)
        binding.next.visibility=View.INVISIBLE
        binding.skip.visibility=View.INVISIBLE
        binding.button.visibility=View.VISIBLE
    }
}