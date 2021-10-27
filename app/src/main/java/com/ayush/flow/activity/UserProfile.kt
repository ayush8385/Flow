package com.ayush.flow.activity

import android.app.Activity
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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.ayush.flow.R
import com.ayush.flow.database.ChatViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException


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
    lateinit var edt_img: ImageView
    lateinit var image_card:CardView
    lateinit var parent:RelativeLayout
    var url=""
    private lateinit var photofile: File
    private var imageuri: Uri?=null
    var imagepath=""
    lateinit var photo:Bitmap
    lateinit var edt_name: ImageView
    lateinit var edt_about: ImageView
    lateinit var firebaseUser: FirebaseUser
    lateinit var back:ImageView
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
        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        back=findViewById(R.id.back)

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

        back.setOnClickListener {
            onBackPressed()
        }

        camera.setOnClickListener {
//            if(Addprofile().checkpermission(this)){
//                photofile = getphotofile("profile_photo.jpg")
//                imageuri = let { it1 -> FileProvider.getUriForFile(it1, "com.ayush.flow.fileprovider", photofile) }
//                val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri)
//                startActivityForResult(intent,110)
//                image_card.visibility=View.GONE
//            }
//            else{
//                Addprofile().requestStoragePermission()
//            }
        }


        gallery.setOnClickListener {
//            if(Addprofile().checkpermission(this)){
//                var intent= Intent(Intent.ACTION_GET_CONTENT)
//                intent.type="image/*"
//                startActivityForResult(intent,112)
//                image_card.visibility=View.GONE
//            }
//            else{
//                Addprofile().requestStoragePermission()
//            }
        }

        delete.setOnClickListener {
            url=""
            image.setImageResource(R.drawable.user)
            image_card.visibility=View.GONE
        }

        edt_name.setOnClickListener {

            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(R.layout.edit_modal_bottomsheet)

            val title=bottomSheetDialog.findViewById<TextView>(R.id.textView)
            val btn1=bottomSheetDialog.findViewById<Button>(R.id.btn_save)
            val edt=bottomSheetDialog.findViewById<EditText>(R.id.set_name)
            val btn2=bottomSheetDialog.findViewById<Button>(R.id.btn_cancel)

            title!!.text="Set your username"

            edt!!.setText(name.text)
            edt!!.isCursorVisible=false
            edt!!.isSelected=true

            btn1!!.setOnClickListener {
                name.text = edt.text.toString()
                sharedPreferences.edit().putString("name", name.text.toString()).apply()
                FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid).child("username").setValue(name.text.toString())
                bottomSheetDialog.dismiss()
            }
            btn2!!.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        edt_about.setOnClickListener {

            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(R.layout.edit_modal_bottomsheet)

            val title=bottomSheetDialog.findViewById<TextView>(R.id.textView)
            val btn1=bottomSheetDialog.findViewById<Button>(R.id.btn_save)
            val edt=bottomSheetDialog.findViewById<EditText>(R.id.set_name)
            val btn2=bottomSheetDialog.findViewById<Button>(R.id.btn_cancel)

            title!!.text="Set your status"

            edt!!.setText(about.text)
            edt!!.isCursorVisible=false
            edt!!.isSelected=true

            btn1!!.setOnClickListener {
                about.text = edt.text.toString()
                sharedPreferences.edit().putString("about",about.text.toString()).apply()
                FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid).child("about").setValue(about.text.toString())
                bottomSheetDialog.dismiss()
            }
            btn2!!.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

    }

    fun getphotofile(fileName: String):File{
        val storage= getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==110 && resultCode== Activity.RESULT_OK){
            if(imageuri!=null){
                try {
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==112 && resultCode== Activity.RESULT_OK){
            val filepath=data!!.data
            try {
                photo=MediaStore.Images.Media.getBitmap(contentResolver,filepath)
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        if(photo!=null){
            image.setImageBitmap(photo)
         //   Addprofile().saveToInternalStorage(photo).execute().get()
            saveandUpload(photo).execute()
    //        Addprofile().uploadImage(name.text.toString(), photo, about.text.toString()).execute()
//            val baos= ByteArrayOutputStream()
//            photo.compress(Bitmap.CompressFormat.JPEG,50,baos)
//            fileBytes =baos.toByteArray()
        }
    }

    inner class saveandUpload(val phot:Bitmap):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            //upload image to firebase in background

            sharedPreferences.edit().putString("name", name.text.toString()).apply()
            sharedPreferences.edit().putString("profile", imagepath).apply()
            sharedPreferences.edit().putString("about",about.text.toString()).apply()

            return true
        }

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