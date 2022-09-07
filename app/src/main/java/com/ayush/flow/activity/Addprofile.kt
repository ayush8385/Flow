package com.ayush.flow.activity

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import com.ayush.flow.R
import com.ayush.flow.Services.*
import com.ayush.flow.databinding.ActivityAddprofileBinding
import com.ayush.flow.utils.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso
import java.io.*


class Addprofile : AppCompatActivity(),MessageListener {
    var url=""
    var imagepath=""
    var selectedPath=""
    var photo:Bitmap?=null
    lateinit var hiddenViewModel: HiddenViewModel
    private lateinit var photofile: File
    private var imageuri: Uri?=null

    lateinit var binding:ActivityAddprofileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getFullScreenViewBack()
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_NAME,"")
        hiddenViewModel= ViewModelProviders.of(this).get(HiddenViewModel::class.java)
        binding.progressbar.visibility= View.VISIBLE
        getWindow().clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setData().execute()

        binding.cam.setOnClickListener {
            if(Build.VERSION.SDK_INT<= Constants.MAX_API_FOR_PERMISSION){
                if(Permissions().checkCamerapermission(this)){
                    openCamera()
                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.camera_permission,
                        this.resources.getString(R.string.camera_permission),this, Constants.CAMERA_PERMISSION)
                }
            }
            else{
                openCamera()
            }
        }


        binding.gall.setOnClickListener {
//            if(Build.VERSION.SDK_INT<= Constants.MAX_API_FOR_PERMISSION){
                if(Permissions().checkCamerapermission(this)){
                    openGallery()
                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.gallery_permission,this.resources.getString(R.string.storage_permission),this,
                        Constants.STORAGE_PERMISSION)
                }
//            }
//            else{
//                openGallery()
//            }
        }

        binding.del.setOnClickListener {
            binding.userimg.setImageResource(R.drawable.user)
            selectedPath=""
            url=""
            photo=null
            imageuri=null
        }



        binding.nxtBtn.setOnClickListener {
            if(binding.username.text.toString()==""){
                Toast.makeText(applicationContext, "Please Fill Username", Toast.LENGTH_SHORT).show()
            }
            else{
                addUserData()
            }
        }


    }
    var img_url=""
    private fun addUserData() {
        if(photo!=null){
            if(imagepath!=null && imagepath!=""){
                img_url=""
                ImageCompression(this,"profile",intent,application).execute(imagepath)
            }
            if(img_url!=""){
                ImageHandling(this).saveImageToFileProviderCache(Constants.MY_USERID,photo!!)
            }
        }
        else{
            FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID).child("profile_photo").setValue("")
        }
        binding.nxtBtn.startAnimation()
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_NAME,binding.username.text.toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_PROFILE_URL,imagepath)
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.ABOUT,binding.userabt.text.toString())


        FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID).child("username").setValue(binding.username.text.toString())
        FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID).child("about").setValue(binding.userabt.text.toString())

        val intent = Intent(this@Addprofile,Dashboard::class.java)
        intent.putExtra("private",0)
        startActivity(intent)
        finishAffinity()
    }

    private fun getFullScreenViewBack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val decorView: View = window.decorView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun openGallery() {
        var intent= Intent(Intent.ACTION_GET_CONTENT)
        intent.type="image/*"
        startActivityForResult(intent, Constants.STORAGE_REQUEST_CODE)
    }

    fun openCamera(){
        photofile = getphotofile("profile_photo")
        imagepath=photofile.absolutePath
        imageuri = let { it1 -> FileProvider.getUriForFile(it1, "com.ayush.flow.fileprovider", photofile) }
        try {
            val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri)
            startActivityForResult(intent, Constants.CAMERA_REQUEST_CODE)
        } catch (exception: ActivityNotFoundException) {
            // some error to be shown here
        }
    }

    fun getphotofile(fileName: String):File{
        val storage= getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }

    inner class setData():AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            val ref = FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID)
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.username.setText(snapshot.child("username").value.toString())
                    binding.userabt.setText(snapshot.child("about").value.toString())
                    img_url = snapshot.child("profile_photo").value.toString()

                    if(img_url!=""){
                        Picasso.get().load(img_url).into(binding.userimg)
                        photo= ImageHandling.GetImageFromUrl().execute(img_url).get()
                    }
                    binding.progressbar.visibility= View.GONE
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
            Constants.PERMISSION_CAMERA_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }
            Constants.PERMISSION_STORAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
//                        if(!Environment.isExternalStorageManager()){
//                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                            val uri = Uri.fromParts("package", packageName, null)
//                            intent.data = uri
//                            startActivity(intent)
//                        }
//                        else{
//                            addUserData()
//                        }
//                    }
//                    else{
//                        addUserData()
//                    }
                    openGallery()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== Constants.CAMERA_REQUEST_CODE && resultCode==Activity.RESULT_OK){
            if(imageuri!=null){
                try {
                    if(imageuri!=null){
                        photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                        binding.userimg.setImageBitmap(photo)
                    }
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode== Constants.STORAGE_REQUEST_CODE && resultCode==Activity.RESULT_OK){
            imageuri=data!!.data
            try {
                if(imageuri!=null){
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                    binding.userimg.setImageBitmap(photo)
                    imagepath = ImageHandling(this).getRealPathFromURI_API19(this,imageuri).execute().get()
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }
    }
}

class uploadImage(val bmp:Bitmap):AsyncTask<Void,Void,Boolean>(){
    override fun doInBackground(vararg params: Void?): Boolean {
        val userId= FirebaseAuth.getInstance().currentUser!!.uid


        val baos= ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val fileinBytes: ByteArray =baos.toByteArray()


        val store: StorageReference = FirebaseStorage.getInstance().reference.child("Profile Images/")
        val path=store.child("${Constants.MY_USERID}.jpg")
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


