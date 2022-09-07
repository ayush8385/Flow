package com.ayush.flow.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.aghajari.emojiview.view.AXEmojiEditText
import com.aghajari.emojiview.view.AXEmojiPopupLayout
import com.aghajari.emojiview.view.AXEmojiView
import com.ayush.flow.R


class EmojiActivity : AppCompatActivity() {
    lateinit var emojiBtn :ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emoji)

        val edt = findViewById<AXEmojiEditText>(R.id.edt)
        val emojiView = AXEmojiView(this)
        emojiView.editText = edt
        val layout = findViewById<AXEmojiPopupLayout>(R.id.layout)
        layout.initPopupView(emojiView)

        emojiBtn=findViewById(R.id.emojibtn)
        emojiBtn.setOnClickListener {
            layout.toggle()
        }


    }
}