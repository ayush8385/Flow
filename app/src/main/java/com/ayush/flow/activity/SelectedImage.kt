package com.ayush.flow.activity


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ayush.flow.R
import com.ayush.flow.Services.Constants
import com.ayush.flow.Services.ImageHandling
import com.ayush.flow.Services.ImageHolder
import com.ayush.flow.Services.SharedPreferenceUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File

class SelectedImage : AppCompatActivity() {
    lateinit var image:TouchImageView
    lateinit var back: ImageView
    lateinit var sendImg: ImageView
    lateinit var userId:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        image=findViewById(R.id.selected_img)
        back=findViewById(R.id.back)
        sendImg=findViewById(R.id.send_img_btn)

        var photoBitmap : Bitmap? =null
        val type=intent.getStringExtra("type")
        userId=intent.getStringExtra("userid")!!

        if(type=="view" || type=="msgImg"){
            if(userId==Constants.MY_USERID){
                Glide.with(this).load(File(File(Environment.getExternalStorageDirectory(),Constants.PROFILE_PHOTO_LOCATION),userId+".jpg")).placeholder(android.R.color.transparent).diskCacheStrategy(
                    DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(image)
            }
            else{
                Glide.with(this).load(File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),userId+".jpg")).placeholder(android.R.color.transparent).diskCacheStrategy(
                    DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(image)
            }
//            image.setImageDrawable(ImageHolder.imageDraw)
//                Log.e("imagepath",ImageHolder.imagePath.toString())
//            Glide.with(this).load(ImageHolder.imagePath).placeholder(R.drawable.user).diskCacheStrategy(
//                DiskCacheStrategy.NONE)
//                .skipMemoryCache(true).into(image)
            sendImg.visibility= View.GONE
        }
        else{
//            if (intent.hasExtra("image")){
//                val byteArray = intent.getByteArrayExtra("image")
//                photoBitmap = MediaStore.Images.Media.getBitmap(contentResolver,)
//                image.setImageBitmap(photoBitmap)
//            }
            image.setImageBitmap(ImageHolder.imageBitmap)
        }

        back.setOnClickListener {
            finish()
        }

        sendImg.setOnClickListener {
            if(type=="profile"){
                ImageHandling.saveToInternalStorage(photoBitmap!!,Constants.PROFILE_PHOTO_LOCATION,SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_USERID,"")+".jpg")
               // var imagepath = Addprofile().saveToInternalStorage(photoBitmap!!).execute().get()

                uploadImage(photoBitmap).execute()
//                getSharedPreferences("Shared Preference", Context.MODE_PRIVATE).edit().putString("profile", imagepath).apply()
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

    override fun onDestroy() {
        ImageHolder.imageDraw=null
        super.onDestroy()
    }
}