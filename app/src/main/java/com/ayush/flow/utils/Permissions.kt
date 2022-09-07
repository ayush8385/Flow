package com.ayush.flow.utils

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
            if(type== Constants.CAMERA_PERMISSION){
                requestCameraPermission(context)
            }
            if(type== Constants.STORAGE_PERMISSION){
                requestStoragePermission(context)
            }
            if(type== Constants.CONTACT_PERMISSION){
                requestContactPermission(context)
            }
            if(type== Constants.MIC_PERMISSION){
                requestMicPermission(context)
            }
            if(type== Constants.MIC_CAM_PERMISSION){
                requestCamAndMicPermission(context)
            }
            bottomSheetDialog.dismiss()
        }

        deny!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    fun checkCamAndMicPermission(context: Context):Boolean{
        return ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED
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
            Constants.PERMISSION_CAMERA_REQUEST_CODE
        )
    }

    fun requestStoragePermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE),
            Constants.PERMISSION_STORAGE_REQUEST_CODE
        )
    }

    fun requestContactPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.READ_CONTACTS),
            Constants.PERMISSION_CONTACT_REQUEST_CODE
        )
    }

    fun requestMicPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            Constants.PERMISSION_MIC_REQUEST_CODE
        )
    }

    fun requestCamAndMicPermission(context: Context){
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA),
            Constants.PERMISSION_MIC_CAM_REQUEST_CODE
        )
    }
}