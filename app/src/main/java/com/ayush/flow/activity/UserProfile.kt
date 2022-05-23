package com.ayush.flow.activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.ayush.flow.R
import com.ayush.flow.Services.*
import com.ayush.flow.database.ChatViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*


class UserProfile : AppCompatActivity(),MessageListener {
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
    var photo:Bitmap?=null
    lateinit var edt_name: ImageView
    lateinit var edt_about: ImageView
    lateinit var firebaseUser: FirebaseUser
    lateinit var back:ImageView
    lateinit var updateImgLayout:RelativeLayout
    lateinit var backImg:ImageView
    lateinit var updtImg:TouchImageView
    lateinit var updtImgbtn:ImageView
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
        updateImgLayout=findViewById(R.id.update_img_now)
        backImg=findViewById(R.id.back_now)
        updtImg=findViewById(R.id.update_select_img)
        updtImgbtn=findViewById(R.id.updimg_btn)
        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        back=findViewById(R.id.back)


        name.text=SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_NAME,"")
        about.text=SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.ABOUT,"I'm with the Flow")
        number.text=SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.NUMBER,"")

        settings.visibility=View.GONE


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
            if(Permissions().checkCamerapermission(this)){
                openCamera()
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.camera,
                    this.resources.getString(R.string.camera_permission),this,"camera")
            }
            image_card.visibility=View.GONE
        }


        gallery.setOnClickListener {
            if(Permissions().checkWritepermission(this)){
                openGallery()
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.gallery,this.resources.getString(R.string.storage_permission),this,"storage")
            }
            image_card.visibility=View.GONE
        }

        delete.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Are you sure want to Delete")
                .setCancelable(false)
                .setPositiveButton("Yes") {
                        dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    url=""
                    image.setImageResource(R.drawable.user)
                    image_card.visibility=View.GONE
                    FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID).child("profile_photo").setValue(url)
                    val directory: File = File(Environment.getExternalStorageDirectory().toString(), Constants.PROFILE_PHOTO_LOCATION)
                    var file: File = File(directory,Constants.MY_USERID+".jpg")
                    file.delete()
                }
                .setNegativeButton("No") {
                        dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
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

        image.setOnClickListener {
          //  ImageHolder.imageDraw=image.drawable
            val intent = Intent(this,SelectedImage::class.java)
            intent.putExtra("type","view")
            intent.putExtra("userid",Constants.MY_USERID)
            startActivity(intent)
        }

        signout.visibility=View.GONE
        signout.setOnClickListener {
            signoutUser()
        }

        backImg.setOnClickListener {
            updtImg.setImageResource(android.R.color.transparent)
            photo=null
            updateImgLayout.visibility=View.GONE
        }

        updtImgbtn.setOnClickListener {
            if(photo!=null){
                ImageHandling.saveToInternalStorage(photo!!,Constants.PROFILE_PHOTO_LOCATION,Constants.MY_USERID+".jpg").execute()
                uploadImage(photo!!).execute()
                image.setImageBitmap(photo)
            }
            updtImg.setImageResource(android.R.color.transparent)
            updateImgLayout.visibility=View.GONE
        }
    }

    override fun onBackPressed() {
        if(updateImgLayout.visibility==View.VISIBLE){
            updateImgLayout.visibility=View.GONE
            updtImg.setImageResource(android.R.color.transparent)
        }
        else{
            super.onBackPressed()
        }
    }

    private fun signoutUser() {

    }

    override fun onResume() {
        Glide.with(this).load(File(File(Environment.getExternalStorageDirectory(),Constants.PROFILE_PHOTO_LOCATION),SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_USERID,"")+".jpg")).placeholder(R.drawable.user).diskCacheStrategy(
            DiskCacheStrategy.NONE)
            .skipMemoryCache(true).into(image)
        super.onResume()
    }

    fun openGallery() {
        var intent= Intent(Intent.ACTION_GET_CONTENT)
        intent.type="image/*"
        startActivityForResult(intent,112)
    }

    fun openCamera(){
        photofile = getphotofile("profile_photo")
        imagepath=photofile.absolutePath
        imageuri = let { it1 -> FileProvider.getUriForFile(it1, "com.ayush.flow.fileprovider", photofile) }
        val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri)
        startActivityForResult(intent,110)
    }

    fun getphotofile(fileName: String):File{
        val storage= getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }

    inner class getRealPathFromURI(val context: Context,val uri: Uri?):AsyncTask<Void,Void,String>() {

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            photo = ImageCompression(context,"profile",intent,application).execute(result).get()
            updtImg.setImageBitmap(photo)
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

        if(requestCode==110 && resultCode== Activity.RESULT_OK){
            if(imageuri!=null){
                try {
                    updateImgLayout.visibility=View.VISIBLE
                    photo = ImageCompression(this,"profile",intent,application).execute(imagepath).get()
                    updtImg.setImageBitmap(photo)
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==112 && resultCode== Activity.RESULT_OK){
            imageuri=data!!.data
            try {
                if(imageuri!=null){
                    updateImgLayout.visibility=View.VISIBLE
                    getRealPathFromURI(this,imageuri!!).execute()
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
//        if(photo!=null){
//            val intent = Intent(this,SelectedImage::class.java)
////            var fos  =  ByteArrayOutputStream()
////            photo!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
////            val byteArray = fos.toByteArray()
//            ImageHolder.imageBitmap=photo
//            intent.putExtra("type","profile")
//        //    intent.putExtra("image", byteArray)
//            intent.putExtra("userid",firebaseUser.uid)
//            intent.putExtra("name","")
//            intent.putExtra("number","")
//            intent.putExtra("user_image","")
//            startActivity(intent)
//        }
    }


//    inner class setImage(val imageView: CircleImageView):AsyncTask<Void,Void,Boolean>(){
//        var b:Bitmap?=null
//        override fun onPostExecute(result: Boolean?) {
//            super.onPostExecute(result)
//            image.setImageBitmap(b)
//        }
//        override fun doInBackground(vararg params: Void?): Boolean {
//            try {
//                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Flow Profile photos"),sharedPreferences.getString("profile",""))
//                b = BitmapFactory.decodeStream(FileInputStream(f))
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            }
//            return true
//        }
//    }
}