package com.ayush.flow.Services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ayush.flow.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class Permissions {

    fun openPermissionBottomSheet(requestImage: Int,requestText:String, context: Context, type: String) {
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(R.layout.permission_modal_bottomsheet)

        val image = bottomSheetDialog.findViewById<ImageView>(R.id.image_access)
        val summary = bottomSheetDialog.findViewById<TextView>(R.id.access_text)
        val allow=bottomSheetDialog.findViewById<Button>(R.id.allow)
        val deny=bottomSheetDialog.findViewById<Button>(R.id.deny)

        image!!.setImageResource(requestImage)
        summary!!.text=requestText

        allow!!.setOnClickListener {
            if(type=="camera"){
                requestCameraPermission(context)
            }
            if(type=="storage"){
                requestStoragePermission(context)
            }
            if(type=="contact"){
                requestContactPermission(context)
            }
            if(type=="mic"){
                requestMicPermission(context)
            }
            bottomSheetDialog.dismiss()
        }

        deny!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }



    fun checkCamerapermission(context: Context):Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun checkWritepermission(context: Context):Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun checkContactpermission(context: Context):Boolean{
        return ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun checkMicpermission(context: Context):Boolean{
        return ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.CAMERA),
            101
        )
    }

    fun requestStoragePermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE),
            102
        )
    }

    fun requestContactPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.READ_CONTACTS),
            103
        )
    }

    fun requestMicPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            104
        )
    }
}