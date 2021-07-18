package com.ayush.flow.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.ayush.flow.R
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


class UserProfile : AppCompatActivity() {
    lateinit var image:CircleImageView
    lateinit var name:TextView
    lateinit var about:TextView
    lateinit var number:TextView
    lateinit var settings:RelativeLayout
    lateinit var signout:TextView
    lateinit var camera: ImageView
    lateinit var gallery: ImageView
    lateinit var delete: ImageView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var edt_img:ImageView
    lateinit var image_card:CardView
    lateinit var parent:RelativeLayout
    var url=""
    private lateinit var photofile: File
    private var imageuri: Uri?=null
    lateinit var edt_name:ImageView
    lateinit var edt_about:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        image=findViewById(R.id.user_img)
        name=findViewById(R.id.user_name)
        about=findViewById(R.id.user_about)
        number=findViewById(R.id.user_number)
        settings=findViewById(R.id.settings)
        signout=findViewById(R.id.sign_out)
        camera=findViewById(R.id.cam)
        gallery=findViewById(R.id.gall)
        delete=findViewById(R.id.del)
        edt_img=findViewById(R.id.edt_img)
        edt_name=findViewById(R.id.edt_name)
        edt_about=findViewById(R.id.edt_abt)
        image_card=findViewById(R.id.img_card)
        parent=findViewById(R.id.user_parent)

        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)

        setImage(image).execute()
        name.text=sharedPreferences.getString("name","")
        about.text=sharedPreferences.getString("about","")
        number.text=sharedPreferences.getString("number","")


        edt_img.setOnClickListener {
            image_card.visibility= View.VISIBLE
        }

        parent.setOnClickListener {
            image_card.visibility=View.GONE
        }

        camera.setOnClickListener {
            if(Addprofile().checkpermission(this)){
                photofile = getphotofile("profile_photo.jpg")
                imageuri = let { it1 -> FileProvider.getUriForFile(it1, "com.ayush.flow.fileprovider", photofile) }
                val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri)
                startActivityForResult(intent,110)
                image_card.visibility=View.GONE
            }
            else{
                Addprofile().requestStoragePermission()
            }
        }


        gallery.setOnClickListener {
            if(Addprofile().checkpermission(this)){
                var intent= Intent(Intent.ACTION_GET_CONTENT)
                intent.type="image/*"
                startActivityForResult(intent,112)
                image_card.visibility=View.GONE
            }
            else{
                Addprofile().requestStoragePermission()
            }
        }

        delete.setOnClickListener {
            url=""
            image.setImageResource(R.drawable.user)
            image_card.visibility=View.GONE
        }

        edt_name.setOnClickListener {
            val edittext = EditText(this)
            edittext.setText(name.text)
            edittext.isSelected=true
            val alert=AlertDialog.Builder(this)

            alert.setTitle("Set your username")

            alert.setView(edittext)

            alert.setPositiveButton("Ok",
                DialogInterface.OnClickListener { dialog, whichButton -> //What ever you want to do with the value

                    name.text = edittext.text.toString()
                })

            alert.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    // what ever you want to do with No option.
                })

            alert.show()
        }

    }

    fun getphotofile(fileName: String):File{
        val storage= getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }

    override fun onBackPressed() {



        super.onBackPressed()
    }

    inner class setImage(val imageView: CircleImageView):AsyncTask<Void,Void,Boolean>(){
        var b:Bitmap?=null
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            image.setImageBitmap(b)
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Flow Profile photos"),sharedPreferences.getString("profile",""))
                b = BitmapFactory.decodeStream(FileInputStream(f))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return true
        }
    }
}