package com.ayush.flow.activity


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ayush.flow.R

class SelectedImage : AppCompatActivity() {
    lateinit var image: TouchImageView
    lateinit var back: ImageView
    lateinit var sendImg: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        image=findViewById(R.id.selected_img)
        back=findViewById(R.id.back)
        sendImg=findViewById(R.id.send_img_btn)

        var photoBitmap : Bitmap? =null
        if (intent.hasExtra("image")){
            //convert to bitmap
            val byteArray = intent.getByteArrayExtra("image")
            photoBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        }

        image.setImageBitmap(photoBitmap)

        back.setOnClickListener {
            finish()
        }

        sendImg.setOnClickListener {
            Message().sendImageMessageToUser(
                photoBitmap!!,
                intent.getStringExtra("userid")!!,
                intent.getStringExtra("name")!!,
                intent.getStringExtra("number")!!,
                intent.getStringExtra("user_image")!!,
                application
            ).execute()
            finish()
        }
    }
}