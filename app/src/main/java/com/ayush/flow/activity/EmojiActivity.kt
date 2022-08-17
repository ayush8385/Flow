package com.ayush.flow.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ayush.flow.R
import com.ayush.flow.databinding.ActivityEmojiBinding
import com.vanniktech.emoji.EmojiPopup

class EmojiActivity:AppCompatActivity() {
    lateinit var binding: ActivityEmojiBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmojiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emojiPopup = EmojiPopup.Builder.fromRootView(binding.root).build(binding.sendText)

        binding.emojibtn.setOnClickListener {
            emojiPopup.toggle()
        }
    }
}