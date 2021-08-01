package com.ayush.flow.Services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ayush.flow.activity.Dashboard
import com.ayush.flow.activity.Message


class BackgroundService: Service() {

    override fun onCreate() {

//        Toast.makeText(applicationContext,"This is a Service running in Background", Toast.LENGTH_SHORT).show();
        Dashboard().retrieveMessage(application).execute()
        Dashboard().loadContacts(application).execute()
        Message()
        Dashboard()

    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}