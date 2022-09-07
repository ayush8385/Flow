package com.ayush.flow.activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.ayush.flow.R
import com.ayush.flow.Services.*
import com.ayush.flow.databinding.ActivityUserProfileBinding
import com.ayush.flow.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class UserProfile : AppCompatActivity(),MessageListener {
    lateinit var sharedPreferences: SharedPreferences
    var delete=false
    private lateinit var photofile: File
    private var imageuri: Uri?=null
    var imagepath=""
    var photo:Bitmap?=null
    lateinit var firebaseUser: FirebaseUser
    lateinit var binding:ActivityUserProfileBinding
    var isSave:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        binding.userName.setText(SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_NAME,""))
        binding.userAbout.setText(SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.ABOUT,"I'm with the Flow"))
        binding.userNumber.setText(SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.NUMBER,""))

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.edtDetail.setOnClickListener {
            if(isSave){
                isSave=false
                binding.imgCard.visibility=View.GONE
                binding.edtDetail.setImageResource(R.drawable.edit)
                binding.userName.isEnabled=false
                binding.userName.setBackgroundResource(0)
                binding.userAbout.isEnabled=false
                binding.userAbout.setBackgroundResource(0)
                updateData()
            }
            else{
                isSave=true
                binding.imgCard.visibility=View.VISIBLE
                binding.edtDetail.setImageResource(R.drawable.save)
                binding.userName.isEnabled=true
                binding.userName.setBackgroundResource(R.drawable.editext_bg)
                binding.userAbout.isEnabled=true
                binding.userAbout.setBackgroundResource(R.drawable.editext_bg)
            }
        }

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
            if(Permissions().checkWritepermission(this)){
                openGallery()
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.gallery_permission,this.resources.getString(R.string.storage_permission),this,
                    Constants.STORAGE_PERMISSION)
            }
        }

        binding.del.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Are you sure want to Delete")
                .setCancelable(false)
                .setPositiveButton("Yes") {
                        dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    delete = true
                    binding.userImg.setImageResource(R.drawable.user)
                }
                .setNegativeButton("No") {
                        dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.backNow.setOnClickListener {
            binding.updateSelectImg.setImageResource(android.R.color.transparent)
            photo=null
            binding.updateImgNow.visibility=View.GONE
        }

        binding.updimgBtn.setOnClickListener {
            if(photo!=null){
                binding.userImg.setImageBitmap(photo)
            }
            binding.updateSelectImg.setImageResource(android.R.color.transparent)
            binding.updateImgNow.visibility=View.GONE
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
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
                        if(!Environment.isExternalStorageManager()){
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        else{
                            openGallery()
                        }
                    }
                    else{
                        openGallery()
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }
    }

    private fun updateData() {

        if(delete){
            FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID).child("profile_photo").setValue("")
            val imageUri = ImageHandling(this).getUserProfileImageUri(Constants.MY_USERID)
            if(imageUri!=null){
                contentResolver.delete(imageUri,null,null)
            }
        }

        if(photo!=null){
            if(imagepath!=""){
                ImageCompression(this,"profile",intent,application).execute(imagepath)
            }
        }

        //name
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_NAME,binding.userName.text.toString())
        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid).child("username").setValue(binding.userName.text.toString())

        //about
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.ABOUT,binding.userAbout.text.toString())
        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid).child("about").setValue(binding.userAbout.text.toString())
    }

    override fun onBackPressed() {
        if(binding.updateImgNow.visibility==View.VISIBLE){
            binding.updateImgNow.visibility=View.GONE
            binding.updateSelectImg.setImageResource(android.R.color.transparent)
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onResume() {
//        Glide.with(this).load(File(File(Environment.getExternalStorageDirectory(),Constants.PROFILE_PHOTO_LOCATION),SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_USERID,"")+".jpg")).placeholder(R.drawable.user).diskCacheStrategy(
//            DiskCacheStrategy.NONE)
//            .skipMemoryCache(true).into(binding.userImg)
        try {
            val profileUri = ImageHandling(this).getUserProfileImageUri(Constants.MY_USERID)
            Glide.with(this).load(profileUri).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.user).into(binding.userImg)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        super.onResume()
    }

    fun openGallery() {
        var intent= Intent(Intent.ACTION_GET_CONTENT)
        intent.type="image/*"
        startActivityForResult(intent,Constants.STORAGE_REQUEST_CODE)
    }

    fun openCamera(){
        photofile = getphotofile("profile_photo")
        imagepath=photofile.absolutePath
        imageuri = let { it1 -> FileProvider.getUriForFile(it1, "com.ayush.flow.fileprovider", photofile) }
        val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri)
        startActivityForResult(intent,Constants.CAMERA_REQUEST_CODE)
    }

    fun getphotofile(fileName: String):File{
        val storage= getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }

    inner class getRealPathFromURI(val context: Context,val uri: Uri?):AsyncTask<Void,Void,String>() {

        override fun doInBackground(vararg p0: Void?): String {
            var filePath = ""
            val wholeID = DocumentsContract.getDocumentId(uri)

            // Split at colon, use second item in the array
            val id = wholeID.split(":".toRegex()).toTypedArray()[1]
            val column = arrayOf(MediaStore.Images.Media.DATA)

            // where id is equal to
            val sel = MediaStore.Images.Media._ID + "=?"
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, arrayOf(id), null
            )
            val columnIndex = cursor!!.getColumnIndex(column[0])
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
            return filePath
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==Constants.CAMERA_REQUEST_CODE && resultCode== Activity.RESULT_OK){
            try {
                if(imageuri!=null){
                    binding.updateImgNow.visibility=View.VISIBLE
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                    binding.updateSelectImg.setImageBitmap(photo)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        else if(requestCode==Constants.STORAGE_REQUEST_CODE && resultCode== Activity.RESULT_OK){
            imageuri=data!!.data
            try {
                if(imageuri!=null){
                    binding.updateImgNow.visibility=View.VISIBLE
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                    binding.updateSelectImg.setImageBitmap(photo)
                    imagepath = ImageHandling(this).getRealPathFromURI_API19(this,imageuri!!).execute().get()
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
    }

}