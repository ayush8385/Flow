package com.ayush.flow.activity


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ayush.flow.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File

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

        if(type=="view"){
            Glide.with(this).load(File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),intent.getStringExtra("userid")!!+".jpg")).placeholder(R.drawable.user).diskCacheStrategy(
                DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(image)
            sendImg.visibility= View.GONE
        }
        else if(type=="msgImg"){
            Glide.with(this).load(File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Chat Images"),intent.getStringExtra("userid")!!+".jpg")).placeholder(R.drawable.user).diskCacheStrategy(
                DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(image)
            sendImg.visibility= View.GONE
        }

        else{
          //  if (intent.hasExtra("image")){
                //convert to bitmap
               // val byteArray = intent.getByteArrayExtra("image")
                photoBitmap = MediaStore.Images.Media.getBitmap(contentResolver,intent.data)
                image.setImageBitmap(photoBitmap)
           // }
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