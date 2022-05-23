package com.ayush.flow.Services

import android.graphics.Bitmap

interface MessageListener {
    fun imageCompressed(bitmap: Bitmap){}
}