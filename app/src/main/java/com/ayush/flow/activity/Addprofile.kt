package com.ayush.flow.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.ayush.flow.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso
import java.io.*
import java.net.URL
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.ayush.flow.Services.Permissions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class Addprofile : AppCompatActivity() {
    lateinit var next:CircularProgressButton
    lateinit var name:EditText
    lateinit var about:EditText
    lateinit var camera: ImageView
    lateinit var gallery: ImageView
    lateinit var delete: ImageView
    lateinit var userimage:ImageView
    lateinit var progressBar: ProgressBar
    lateinit var sharedPreferences: SharedPreferences
    var url=""
    var imagepath=""
    var photo:Bitmap?=null
    private lateinit var photofile: File
    private var imageuri: Uri?=null
    lateinit var fileBytes: ByteArray

    lateinit var mode:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addprofile)

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        next=findViewById(R.id.nxt_btn)
        name=findViewById(R.id.username)
        about=findViewById(R.id.userabt)
        camera=findViewById(R.id.cam)
        gallery=findViewById(R.id.gall)
        delete=findViewById(R.id.del)
        userimage=findViewById(R.id.userimg)
        progressBar=findViewById(R.id.progressbar)



        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)

        setData().execute()



        camera.setOnClickListener {
            if(Permissions().checkCamerapermission(this)){
                openCamera()
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.camera_permission,
                    this.resources.getString(R.string.camera_permission),this,"camera")
            }
        }


        gallery.setOnClickListener {
            if(Permissions().checkWritepermission(this)){
                openGallery()

            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.gallery_permission,this.resources.getString(R.string.storage_permission),this,"storage")
            }
        }

        delete.setOnClickListener {
            url=""
            userimage.setImageResource(R.drawable.user)
            photo=null
        }


        next.setOnClickListener {
            val username=name.text.toString()
            val abt=about.text.toString()
            if(username==""){
                Toast.makeText(applicationContext, "Please Fill Username", Toast.LENGTH_SHORT).show()
            }
            else{
                if(Permissions().checkWritepermission(this)){
                    if(photo!=null){
                        imagepath = saveToInternalStorage(photo!!).execute().get()
                        uploadImage(photo!!).execute()
                    }
                    else{
                        FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("profile_photo").setValue(url)
                    }
                    next.startAnimation()
                    sharedPreferences.edit().putString("name", username).apply()
                    sharedPreferences.edit().putString("profile",imagepath).apply()
                    sharedPreferences.edit().putString("about",abt).apply()

                    val userId= FirebaseAuth.getInstance().currentUser!!.uid
                    FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("username").setValue(username)
                    FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("about").setValue(abt)

                    val intent = Intent(this@Addprofile,Dashboard::class.java)
                    intent.putExtra("private",0)
                    startActivity(intent)
                    finishAffinity()

                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.gallery,this.resources.getString(R.string.storage_permission),this,"storage")
                }
            }
        }


    }

    private fun openGallery() {
        var intent= Intent(Intent.ACTION_GET_CONTENT)
        intent.type="image/*"
        startActivityForResult(intent,112)
    }

    fun openCamera(){
        photofile = getphotofile("profile_photo")
        imageuri = let { it1 -> FileProvider.getUriForFile(it1, "com.ayush.flow.fileprovider", photofile) }
        val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri)
        startActivityForResult(intent,110)
    }

    fun getphotofile(fileName: String):File{
        val storage= getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }

    inner class setData():AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            val ref = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var img=""
                    name.setText(snapshot.child("username").value.toString())
                    about.setText(snapshot.child("about").value.toString())
                    img = snapshot.child("profile_photo").value.toString()

                    if(img!=""){
                        Picasso.get().load(img).into(userimage)
                        photo=GetImageFromUrl().execute(img).get()
                    }
                    progressBar.visibility= View.GONE
                    getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            return true
        }

    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            101 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
                // super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            102->{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                        if(!Environment.isExternalStorageManager()){
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                    }
                }
                //  super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }

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
        }
    }

    inner class saveToInternalStorage(val bitmapImage:Bitmap):AsyncTask<Void,Void,String>(){
        val firebaseUser=FirebaseAuth.getInstance().currentUser!!
        val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Flow Profile photos")
        var path:String=firebaseUser.uid+".jpg"
        var file:File = File(directory,path)
        override fun doInBackground(vararg params: Void?): String {
            if(directory.exists()){
                var fos: FileOutputStream =
                    FileOutputStream(file)
                try {
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos)
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
                    var fos: FileOutputStream =
                        FileOutputStream(file)
                    try {
                        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos)
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
}

class uploadImage(val bmp:Bitmap):AsyncTask<Void,Void,Boolean>(){
    override fun doInBackground(vararg params: Void?): Boolean {
        val userId= FirebaseAuth.getInstance().currentUser!!.uid


        val baos= ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG,50,baos)
        val fileinBytes: ByteArray =baos.toByteArray()

//        val ref= FirebaseDatabase.getInstance().reference
        val profilekey=userId

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
class GetImageFromUrl() : AsyncTask<String?, Void?, Bitmap>() {
    var bmp:Bitmap?=null
    override fun doInBackground(vararg url: String?): Bitmap {
        val stringUrl = url[0]
        val options = BitmapFactory.Options()
        options.inSampleSize = 1
        while (options.inSampleSize <= 32) {
            val inputStream = URL(stringUrl).openStream()
            try {
                bmp= BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()
                break
            } catch (outOfMemoryError: OutOfMemoryError) {
            }
            options.inSampleSize++
        }

        return bmp!!
    }
}

