package com.ayush.flow.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ayush.flow.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class Addprofile : AppCompatActivity() {
    lateinit var next:Button
    lateinit var edit_card:CardView
    lateinit var name:EditText
    lateinit var about:EditText
    lateinit var edit_btn:Button
    lateinit var parent:RelativeLayout
    lateinit var camera:ImageView
    lateinit var gallery:ImageView
    lateinit var delete:ImageView
    lateinit var userimage:CircleImageView
    lateinit var progressBar: ProgressBar
    lateinit var sharedPreferences: SharedPreferences
    var url=""
    var imagepath=""
    var photo:Bitmap?=null
    private lateinit var photofile: File
    private var imageuri: Uri?=null
    lateinit var fileBytes: ByteArray
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addprofile)
        next=findViewById(R.id.nxt_btn)
        edit_card=findViewById(R.id.more_card)
        name=findViewById(R.id.name)
        about=findViewById(R.id.about)
        edit_btn=findViewById(R.id.edt_btn)
        parent=findViewById(R.id.profile_parent)
        camera=findViewById(R.id.cam)
        gallery=findViewById(R.id.gall)
        delete=findViewById(R.id.del)
        userimage=findViewById(R.id.image)
        progressBar=findViewById(R.id.progressbar)

        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)

        edit_btn.setOnClickListener {
            edit_btn.visibility=View.GONE
            edit_card.visibility=View.VISIBLE
        }

        parent.setOnClickListener {
            edit_btn.visibility=View.VISIBLE
            edit_card.visibility=View.GONE
        }

        camera.setOnClickListener {
            if(checkpermission()){
                photofile = getphotofile("profile_photo.jpg")
                imageuri = let { it1 -> FileProvider.getUriForFile(it1, "com.ayush.flow.fileprovider", photofile) }
                val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri)
                startActivityForResult(intent,110)
                edit_btn.visibility=View.VISIBLE
                edit_card.visibility=View.GONE
            }
            else{
                requestStoragePermission()
            }
        }


        gallery.setOnClickListener {
            if(checkpermission()){
                var intent= Intent(Intent.ACTION_GET_CONTENT)
                intent.type="image/*"
                startActivityForResult(intent,112)
                edit_btn.visibility=View.VISIBLE
                edit_card.visibility=View.GONE
            }
            else{
                requestStoragePermission()
            }
        }

        delete.setOnClickListener {
            url=""
            userimage.setImageResource(R.drawable.user)
            edit_btn.visibility=View.VISIBLE
            edit_card.visibility=View.GONE
        }


        next.setOnClickListener {
            val username=name.text.toString()
            if(username==""){
                Toast.makeText(applicationContext, "Please Fill Username", Toast.LENGTH_SHORT).show()
            }
            else{

                if(photo!=null){
                    imagepath = saveToInternalStorage(photo!!)

                    val animation: Animation = AnimationUtils.loadAnimation(applicationContext,R.anim.button_anim)
                    next.startAnimation(animation)
                    next.visibility= View.GONE
                    progressBar.visibility= View.VISIBLE
                    next.clearAnimation()

                    //upload image to firebase in background
                    uploadImage(username, photo!!).execute()

                    sharedPreferences.edit().putString("name", username).apply()
                    sharedPreferences.edit().putString("profile",imagepath).apply()
                }


                startActivity(Intent(applicationContext, Dashboard::class.java))
                finishAffinity()
            }
        }
    }

    private fun getphotofile(fileName: String):File{
        val storage= getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }


    fun checkpermission():Boolean {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            return false
        }
    }

    fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
            1234
        )
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            1234 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Do_SOme_Operation();


                }
                else{
                    showMessageOKCancel("You need to allow access permissions",
                        DialogInterface.OnClickListener { dialog, which ->
                            requestStoragePermission()
                        })
                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }

    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==110 && resultCode==Activity.RESULT_OK){
            if(imageuri!=null){
                try {
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==112 && resultCode==Activity.RESULT_OK){
            val filepath=data!!.data
            try {
                photo=MediaStore.Images.Media.getBitmap(contentResolver,filepath)
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        if(photo!=null){
            userimage.setImageBitmap(photo)
//            val baos= ByteArrayOutputStream()
//            photo.compress(Bitmap.CompressFormat.JPEG,50,baos)
//            fileBytes =baos.toByteArray()
        }
    }

    fun saveToInternalStorage(bitmapImage: Bitmap): String{
        val cw = ContextWrapper(applicationContext)
        var path:String?=null
        val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Flow Profile photos")
        if(directory.exists()){
            path=System.currentTimeMillis().toString()+".jpg"
            var fos: FileOutputStream =
                FileOutputStream(File(directory, path))
            try {
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        else{
            directory.mkdirs()
            if (directory.isDirectory) {
                path=System.currentTimeMillis().toString()+".jpg"
                val fos =
                    FileOutputStream(File(directory, path))
                try {
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        return path!!
    }
}

class uploadImage(val username:String,val bmp:Bitmap):AsyncTask<Void,Void,Boolean>(){
    override fun doInBackground(vararg params: Void?): Boolean {
        val userId= FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("username").setValue(username)

        val baos= ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val fileinBytes: ByteArray =baos.toByteArray()

        val ref= FirebaseDatabase.getInstance().reference
        val profilekey=ref.push().key

        val store: StorageReference = FirebaseStorage.getInstance().reference.child("Profile Images/")
        val path=store.child("$profilekey.jpg")
        val uploadTask: StorageTask<*>
        uploadTask=path.putBytes(fileinBytes)

        uploadTask.addOnSuccessListener(OnSuccessListener { taskSnapshot ->
            val firebaseUri = taskSnapshot.storage.downloadUrl
            firebaseUri.addOnSuccessListener { uri ->
                val url = uri.toString()
                FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("profile_photo").setValue(url)
            }
        })
        return true
    }

}


