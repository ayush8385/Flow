package com.ayush.flow.activity

import android.provider.MediaStore

import android.content.ContentResolver

import android.provider.DocumentsContract

import android.os.Build

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast


class FileUtils {

    /*
This method can parse out the real local file path from a file URI.
*/
    fun getUriRealPath(ctx: Context, uri: Uri): String? {
        var ret: String? = ""
        ret = if (isAboveKitKat()) {
            // Android sdk version number bigger than 19.
            getUriRealPathAboveKitkat(ctx, uri)
        } else {
            // Android sdk version number smaller than 19.
            getImageRealPath(ctx.contentResolver, uri, null)
        }
        return ret
    }

    /*
This method will parse out the real local file path from the file content URI.
The method is only applied to the android SDK version number that is bigger than 19.
*/
    private fun getUriRealPathAboveKitkat(ctx: Context?, uri: Uri?): String? {
        var ret: String? = ""
        if (ctx != null && uri != null) {
            if (isContentUri(uri)) {



                if (isGooglePhotoDoc(uri.getAuthority()!!)) {
                    ret = uri.getLastPathSegment()
                }
                else if(isDownloadDoc(uri.authority!!)!!){
                    Log.e("djkbfjkdsnbkfjsd..............","workings....download dov....")

                    val downloadUri: Uri = Uri.parse("content://downloads/public_downloads")

                    val documentId = DocumentsContract.getDocumentId(uri)
                    // Append download document id at URI end.
                    val downloadUriAppendId: Uri =
                        ContentUris.withAppendedId(downloadUri, java.lang.Long.valueOf(documentId))
                    ret = getImageRealPath(ctx.getContentResolver(), downloadUriAppendId, null)
                }
                else {
                   ret= getImageRealPath(ctx.getContentResolver(), uri, null)
                }
            } else if (isFileUri(uri)) {
                ret = uri.getPath()
            } else if (isDocumentUri(ctx, uri)) {

                // Get uri related document id.
                val documentId = DocumentsContract.getDocumentId(uri)

                // Get uri authority.
                val uriAuthority: String = uri.getAuthority()!!
                if (isMediaDoc(uriAuthority)) {
                    val idArr = documentId.split(":").toTypedArray()
                    if (idArr.size == 2) {
                        // First item is document type.
                        val docType = idArr[0]

                        // Second item is document real id.
                        val realDocId = idArr[1]

                        // Get content uri by document type.
                        var mediaContentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        if ("image" == docType) {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == docType) {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == docType) {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }

                        // Get where clause with real document id.
                        val whereClause = MediaStore.Images.Media._ID + " = " + realDocId
                        ret = getImageRealPath(ctx.getContentResolver(), mediaContentUri, whereClause)
                    }
                } else if (isDownloadDoc(uriAuthority)) {
                    // Build download URI.
                    val downloadUri: Uri = Uri.parse("content://downloads/public_downloads")

                    // Append download document id at URI end.
                    val downloadUriAppendId: Uri =
                        ContentUris.withAppendedId(downloadUri, java.lang.Long.valueOf(documentId))
                    ret = getImageRealPath(ctx.getContentResolver(), downloadUriAppendId, null)
                } else if (isExternalStoreDoc(uriAuthority)) {
                    val idArr = documentId.split(":").toTypedArray()
                    if (idArr.size == 2) {
                        val type = idArr[0]
                        val realDocId = idArr[1]
                        if ("primary".equals(type, ignoreCase = true)) {
                            ret = Environment.getExternalStorageDirectory()
                                .toString() + "/" + realDocId
                        }
                    }
                }
            }
        }
        return ret
    }

    /* Check whether the current android os version is bigger than KitKat or not. */
    private fun isAboveKitKat(): Boolean {
        var ret = false
        ret = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        return ret
    }

    /* Check whether this uri represent a document or not. */
    private fun isDocumentUri(ctx: Context?, uri: Uri?): Boolean {
        var ret = false
        if (ctx != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(ctx, uri)
        }
        return ret
    }

    /* Check whether this URI is a content URI or not.
*  content uri like content://media/external/images/media/1302716
*  */
    private fun isContentUri(uri: Uri?): Boolean {
        var ret = false
        if (uri != null) {
            val uriSchema: String = uri.getScheme()!!
            if ("content".equals(uriSchema, ignoreCase = true)) {
                ret = true
            }
        }
        return ret
    }

    /* Check whether this URI is a file URI or not.
*  file URI like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg
* */
    private fun isFileUri(uri: Uri?): Boolean {
        var ret = false
        if (uri != null) {
            val uriSchema: String = uri.getScheme()!!
            if ("file".equals(uriSchema, ignoreCase = true)) {
                ret = true
            }
        }
        return ret
    }


    /* Check whether this document is provided by ExternalStorageProvider. Return true means the file is saved in external storage. */
    private fun isExternalStoreDoc(uriAuthority: String): Boolean {
        var ret = false
        if ("com.android.externalstorage.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Check whether this document is provided by DownloadsProvider. return true means this file is a downloaded file. */
    private fun isDownloadDoc(uriAuthority: String): Boolean {
        var ret = false
        if ("com.android.providers.downloads.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /*
Check if MediaProvider provides this document, if true means this image is created in the android media app.
*/
    private fun isMediaDoc(uriAuthority: String): Boolean {
        var ret = false

        if ("com.android.providers.media.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /*
Check whether google photos provide this document, if true means this image is created in the google photos app.
*/
    private fun isGooglePhotoDoc(uriAuthority: String): Boolean {
        var ret = false
        if ("com.google.android.apps.photos.content" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Return uri represented document file real local path.*/
    private fun getImageRealPath(
        contentResolver: ContentResolver,
        uri: Uri,
        whereClause: String?
    ): String? {
        var ret = ""

        // Query the URI with the condition.
        val cursor: Cursor? = contentResolver.query(uri, null, whereClause, null, null)
        if (cursor != null) {
            val moveToFirst: Boolean = cursor.moveToFirst()
            if (moveToFirst) {

                // Get columns name by URI type.
                var columnName = MediaStore.Images.Media.DATA
                if (uri === MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Images.Media.DATA
                } else if (uri === MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Audio.Media.DATA
                } else if (uri === MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Video.Media.DATA
                }

                // Get column index.
                val imageColumnIndex: Int = cursor.getColumnIndex(columnName)

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex)
            }
        }
        return ret
    }

}