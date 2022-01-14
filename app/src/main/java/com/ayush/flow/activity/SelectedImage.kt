package com.ayush.flow.activity


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.view.View
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
        val type=intent.getStringExtra("type")

        if (intent.hasExtra("image")){
            //convert to bitmap
            val byteArray = intent.getByteArrayExtra("image")
            photoBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
            image.setImageBitmap(photoBitmap)
        }

        if(type=="view"){
            sendImg.visibility= View.GONE
        }

        back.setOnClickListener {
            finish()
        }

        sendImg.setOnClickListener {
            if(type=="profile"){
                var imagepath = Addprofile().saveToInternalStorage(photoBitmap!!).execute().get()

                uploadImage(photoBitmap).execute()
                getSharedPreferences("Shared Preference", Context.MODE_PRIVATE).edit().putString("profile", imagepath).apply()
                finish()
            }
            if(type=="message"){
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
}