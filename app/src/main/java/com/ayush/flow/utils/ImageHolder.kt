package com.ayush.flow.utils

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

class ImageHolder: Application() {
    companion object{
        var imageDraw:Drawable?=null
        var imageBitmap:Bitmap?=null
        var imagePath:String?=null
    }
}