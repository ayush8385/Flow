package com.ayush.flow.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
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
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.ayush.flow.R
import com.ayush.flow.Services.*
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
    var selectedPath=""
    var photo:Bitmap?=null
    lateinit var hiddenViewModel: HiddenViewModel
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
        hiddenViewModel= ViewModelProviders.of(this).get(HiddenViewModel::class.java)
        progressBar.visibility= View.GONE
        getWindow().clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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

        userimage.setOnClickListener {
            ImageHolder.imageDraw=userimage.drawable
            val intent = Intent(this,SelectedImage::class.java)
            intent.putExtra("type","view")
            startActivity(intent)
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
                        ImageHandling.saveToInternalStorage(photo!!,Constants.PROFILE_PHOTO_LOCATION,Constants.MY_USERID+".jpg").execute()
                        uploadImage(photo!!).execute()
                    }
                    else{
                        FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID).child("profile_photo").setValue("")
                    }
                    next.startAnimation()
                    SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_NAME,username)
                    SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_PROFILE_URL,imagepath)
                    SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.ABOUT,abt)


                    FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID).child("username").setValue(username)
                    FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID).child("about").setValue(abt)

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

//    private fun mSaveMediaToStorage(bitmap: Bitmap?) {
//        val filename = "${System.currentTimeMillis()}.jpg"
//        var fos: OutputStream? = null
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Flow Profile photos")
//            this.contentResolver?.also { resolver ->
//                val contentValues = ContentValues().apply {
//                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
//                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
//                    put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
//                }
//                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//                fos = imageUri?.let { resolver.openOutputStream(it) }
//            }
//        } else {
//            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//            val image = File(imagesDir, filename)
//            fos = FileOutputStream(image)
//        }
//        fos?.use {
//            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
//            Toast.makeText(this , "Saved to Gallery" , Toast.LENGTH_SHORT).show()
//        }
//    }

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
            val ref = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var img=""
                    name.setText(snapshot.child("username").value.toString())
                    about.setText(snapshot.child("about").value.toString())
                    img = snapshot.child("profile_photo").value.toString()

                    if(img!=""){
                        Picasso.get().load(img).into(userimage)
                        photo=ImageHandling.GetImageFromUrl().execute(img).get()

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


//    inner class getRealPathFromURI(val inContext: Context,val inImage: Bitmap?):AsyncTask<Void,Void,String>(){
//        override fun onPostExecute(result: String?) {
//            super.onPostExecute(result)
//            val bmp = ImageCompression(inContext,"").execute(result).get()
//            userimage.setImageBitmap(bmp)
//        }
//        override fun doInBackground(vararg p0: Void?): String? {
//            val bytes = ByteArrayOutputStream()
//            inImage!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//            val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Te", null)
//            var contentURI:Uri?=null
//            contentURI = Uri.parse(path)
//
//
//            val cursor: Cursor? = inContext.contentResolver.query(contentURI!!, null, null, null, null)
//            if (cursor == null) {
//                return contentURI.getPath()!!
//            } else {
//                cursor.moveToFirst()
//                val idx: Int = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
//                return cursor.getString(idx)
//            }
//        }
//
//    }
//    fun getRealPathFromURI(inContext: Context, inImage: Bitmap): String {
//
//        val bytes = ByteArrayOutputStream()
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
//        val contentURI = Uri.parse(path)
//
//
//        val cursor: Cursor? = this.contentResolver.query(contentURI, null, null, null, null)
//         if (cursor == null) {
//             return contentURI.getPath()!!
//        } else {
//            cursor.moveToFirst()
//            val idx: Int = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
//             return cursor.getString(idx)
//        }
//    }



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
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }

    }
    inner class getRealPathFromURI_API19(val context: Context,val uri: Uri?):AsyncTask<Void,Void,String>() {

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            photo = ImageCompression(context).execute(result).get()
            userimage.setImageBitmap(photo)
        }
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

        if(requestCode==110 && resultCode==Activity.RESULT_OK){
            if(imageuri!=null){
                try {
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                    if(photo!=null){
                        photo = ImageCompression(this).execute(selectedPath).get()
                        userimage.setImageBitmap(photo)
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
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
    }

//    inner class saveToInternalStorage(val bitmapImage:Bitmap):AsyncTask<Void,Void,Boolean>(){
//        val firebaseUser=FirebaseAuth.getInstance().currentUser!!
//        val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Flow Profile photos")
//        var path:String=firebaseUser.uid+".jpg"
//        var file:File = File(directory,path)
//        override fun doInBackground(vararg params: Void?):Boolean {
//            if(!directory.exists()){
//                directory.mkdirs()
//            }
//            var fos: FileOutputStream = FileOutputStream(file)
//            try {
//                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                try {
//                    fos.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//            return true
//        }
//    }
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

//private fun mStringToURL(string: String): URL? {
//    try {
//        return URL(string)
//    } catch (e: MalformedURLException) {
//        e.printStackTrace()
//    }
//    return null
//}


