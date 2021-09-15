package com.ayush.flow.Services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.ayush.flow.activity.Dashboard





class BackgroundService: Service() {

    override fun onCreate() {

        Toast.makeText(applicationContext,"This is a Service running in Background", Toast.LENGTH_SHORT).show();

//        Message()
//        Dashboard()

    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Retrieving Message", Toast.LENGTH_LONG).show()
        Dashboard().retrieveMessage(application).execute()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show()
    }
}