package com.ayush.flow.activity

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.ayush.flow.R
import com.ayush.flow.Services.*
import com.ayush.flow.databinding.ActivityAddprofileBinding
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
    var addingUser=false
    var resumeCount=0
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
            if(Permissions().checkCamerapermission(this)){
                openCamera()
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.camera_permission,
                    this.resources.getString(R.string.camera_permission),this,"camera")
            }
        }


        binding.gall.setOnClickListener {
            if(Permissions().checkWritepermission(this)){
                openGallery()
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.gallery_permission,this.resources.getString(R.string.storage_permission),this,"storage")
            }
        }

        binding.del.setOnClickListener {
            url=""
            binding.userimg.setImageResource(R.drawable.user)
            photo=null
        }



        binding.nxtBtn.setOnClickListener {
            if(binding.username.text.toString()==""){
                Toast.makeText(applicationContext, "Please Fill Username", Toast.LENGTH_SHORT).show()
            }
            else{
                addingUser=true
                if(Permissions().checkWritepermission(this)){
                    addUserData()
                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.gallery,this.resources.getString(R.string.storage_permission),this,"storage")
                }
            }
        }


    }

    private fun addUserData() {
        if(photo!=null){
            if(imagepath!=null && imagepath!=""){
                ImageCompression(this,"profile",intent,application).execute(imagepath)
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

    override fun onResume() {
        super.onResume()
        if(addingUser){
            resumeCount+=1
            if(resumeCount==2){
                if(Permissions().checkWritepermission(this)){
                    addUserData()
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
        selectedPath=photofile.absolutePath
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
            val ref = FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID)
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var img=""
                    binding.username.setText(snapshot.child("username").value.toString())
                    binding.userabt.setText(snapshot.child("about").value.toString())
                    img = snapshot.child("profile_photo").value.toString()

                    if(img!=""){
                        Picasso.get().load(img).into(binding.userimg)
                        photo=ImageHandling.GetImageFromUrl().execute(img).get()
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
            101 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
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
                        else{
                            addUserData()
                        }
                    }
                    else{
                        addUserData()
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }

    }
    inner class getRealPathFromURI_API19(val context: Context,val uri: Uri?):AsyncTask<Void,Void,String>() {

        override fun doInBackground(vararg p0: Void?): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri!!)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri!!.lastPathSegment!! else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri!!.scheme, ignoreCase = true)) {
                return uri!!.path!!
            }

            return null
        }


    }


    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==110 && resultCode==Activity.RESULT_OK){
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
        else if(requestCode==112 && resultCode==Activity.RESULT_OK){
            imageuri=data!!.data
            try {
                photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                if(photo!=null){
                    getRealPathFromURI_API19(this,imageuri!!).execute()  //get path of image and compress in onPostexecute and store in photo bitmap compressed image
                }

                if(imageuri!=null){
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                    binding.userimg.setImageBitmap(photo)
                    imagepath = getRealPathFromURI_API19(this,imageuri!!).execute().get()
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


