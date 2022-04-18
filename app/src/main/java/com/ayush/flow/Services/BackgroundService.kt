package com.ayush.flow.Services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.ayush.flow.activity.Dashboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class BackgroundService: Service() {

    override fun onCreate() {

        Toast.makeText(applicationContext,"This is a Service running in Background", Toast.LENGTH_SHORT).show();

    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Retrieving Message", Toast.LENGTH_LONG).show()
        GlobalScope.launch {
            RetrieveMessage(application).execute()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show()
    }
}