package com.ayush.flow.activity

import android.app.Application
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.googleprovider.AXGoogleEmojiProvider


class FlowApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        AXEmojiManager.install(this,AXGoogleEmojiProvider(this)) // new ProviderClassName

    }
}