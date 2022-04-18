package com.ayush.flow.Services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class ImageHandling {

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

    class GetUrlImageAndSave(val location: String,val fileName: String) : AsyncTask<String?, Void?, Boolean>() {
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
            Log.e("starteddd","compressed")
            saveToInternalStorage(bmp!!,location,fileName).execute()
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


}