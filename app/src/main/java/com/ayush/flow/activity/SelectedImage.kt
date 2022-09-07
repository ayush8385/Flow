package com.ayush.flow.activity


import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.ayush.flow.R
import com.ayush.flow.utils.Constants
import com.ayush.flow.utils.ImageHandling
import com.ayush.flow.utils.ImageHolder
import com.ayush.flow.utils.SharedPreferenceUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.io.FileNotFoundException


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


        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        val requestOptions = RequestOptions()
        requestOptions.placeholder(circularProgressDrawable)
        requestOptions.error(R.drawable.user)
        requestOptions.fitCenter()
        if(type=="view"){
            try {
                val profileUri = ImageHandling(this).getUserProfileImageUri(userId)
                Glide.with(this).load(profileUri).apply(requestOptions) // here you have all options you need
                    .transition(DrawableTransitionOptions.withCrossFade(150)) .diskCacheStrategy(
                    DiskCacheStrategy.NONE).skipMemoryCache(true).into(image)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            sendImg.visibility= View.GONE
        }
        else if(type=="msgImg"){
            try {
                val f = File(File(Environment.getExternalStorageDirectory(), Constants.ALL_PHOTO_LOCATION),
                    "$userId.jpg"
                )
                Glide.with(this).load(f).apply(requestOptions) // here you have all options you need
                    .transition(DrawableTransitionOptions.withCrossFade(100)).diskCacheStrategy(
                        DiskCacheStrategy.NONE).skipMemoryCache(true).into(image)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
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
                ImageHandling(this).saveImageToFileProviderCache(Constants.MY_USERID,photoBitmap!!)
                uploadImage(photoBitmap).execute()
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
            }
            finish()
        }
    }

    override fun onDestroy() {
        ImageHolder.imageDraw=null
        super.onDestroy()
    }
}