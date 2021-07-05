package com.ayush.flow.flow.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ayush.flow.R

class Addprofile : AppCompatActivity() {
    lateinit var next:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addprofile)
        next=findViewById(R.id.nxt_btn)

        next.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

    }
}