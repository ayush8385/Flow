package com.ayush.flow.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider.getUriForFile
import org.webrtc.ContextUtils.getApplicationContext
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class ImageHandling(val context: Context) {

    class saveToInternalStorage(val bitmapImage:Bitmap,val location:String,val fileName:String):AsyncTask<Void,Void,Boolean>(){
        val directory: File = File(Environment.getExternalStorageDirectory().toString(), location)
        var file: File = File(directory,fileName)
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
        }
        override fun doInBackground(vararg params: Void?):Boolean {
            if(!directory.exists()){
                directory.mkdirs()
            }
            var fos: FileOutputStream = FileOutputStream(file)
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
            return true
        }
    }

    inner class GetUrlImageAndSave(val location: String,val fileName: String,val from :String) : AsyncTask<String?, Void?, Boolean>() {
        var bmp: Bitmap?=null
        override fun doInBackground(vararg url: String?): Boolean {
            val url: URL = mStringToURL(url[0]!!)!!
            val connection: HttpURLConnection?
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)
                bmp= BitmapFactory.decodeStream(bufferedInputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if(from == "message"){
                saveToInternalStorage(bmp!!,location,fileName+".jpg").execute()
            }
            if(from == "profile"){
                ImageHandling(context).saveImageToFileProviderCache(fileName,bmp!!)
            }
        }

        private fun mStringToURL(string: String): URL? {
            try {
                return URL(string)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return null
        }
    }


    class GetImageFromUrl() : AsyncTask<String?, Void?, Bitmap>() {
        var bmp: Bitmap?=null
        override fun doInBackground(vararg url: String?): Bitmap {
//        val stringUrl = url[0]
//        val options = BitmapFactory.Options()
//        options.inSampleSize = 1
//        while (options.inSampleSize <= 32) {
//            val inputStream = URL(stringUrl).openStream()
//            try {
//                bmp= BitmapFactory.decodeStream(inputStream, null, options)
//                inputStream.close()
//                break
//            } catch (outOfMemoryError: OutOfMemoryError) {
//            }
//            options.inSampleSize++
//        }
            val url: URL = mStringToURL(url[0]!!)!!
            val connection: HttpURLConnection?
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)
                bmp= BitmapFactory.decodeStream(bufferedInputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bmp!!
        }

        private fun mStringToURL(string: String): URL? {
            try {
                return URL(string)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return null
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




    fun saveImageToFileProviderCache(userid:String,photo:Bitmap){
        val imageName = "/${userid}.jpg"
        val imagePath = File(context.filesDir, "profile_images")
        val newFile = File(imagePath, imageName)
        Toast.makeText(context,"save bro", Toast.LENGTH_LONG).show()
        if(!imagePath.exists()){
            imagePath.mkdirs()
        }
        if (newFile.exists()) {
            newFile.delete()
        }
        val stream:FileOutputStream = FileOutputStream(newFile)
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream) // can be png and any quality level
        stream.close()
    }

    fun getUserProfileImageUri(userid: String):Uri {
        val imageName = "/${userid}.jpg"
        val imagePath = File(context.filesDir, "profile_images")
        val newFile = File(imagePath, imageName)
        val contentUri: Uri = getUriForFile(context, "com.ayush.flow.fileprovider", newFile)
        return contentUri
    }


}