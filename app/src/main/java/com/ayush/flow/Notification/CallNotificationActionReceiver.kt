package com.ayush.flow.Notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.ayush.flow.activity.Dashboard


class CallNotificationActionReceiver : BroadcastReceiver() {
    var mContext: Context? = null
    override fun onReceive(context: Context, intent: Intent?) {
        mContext = context
        if (intent != null && intent.extras != null) {
            var action: String? = ""
            action = intent.getStringExtra("ACTION_TYPE")
            if (action != null && !action.equals("", ignoreCase = true)) {
                performClickAction(context, action)
            }

            // Close the notification after the click action is performed.
            val iclose = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(iclose)
            context.stopService(Intent(context, MessagingService::class.java))
        }
    }

    private fun performClickAction(context: Context, action: String) {
        if (action.equals("RECEIVE_CALL", ignoreCase = true)) {
            if (checkAppPermissions()) {
                val intentCallReceive = Intent(mContext, Dashboard::class.java)
                intentCallReceive.putExtra("Call", "incoming")
                intentCallReceive.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                mContext!!.startActivity(intentCallReceive)
            } else {
                val intent = Intent(
                    mContext,
                    Dashboard::class.java
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("CallFrom", "call from push")
                mContext!!.startActivity(intent)
            }
        } else if (action.equals("DIALOG_CALL", ignoreCase = true)) {

            // show ringing activity when phone is locked
            val intent = Intent(
                mContext,
                Dashboard::class.java
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            mContext!!.startActivity(intent)
        } else {
            context.stopService(Intent(context, MessagingService::class.java))
            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(it)
        }
    }

    private fun checkAppPermissions(): Boolean {
        return hasReadPermissions() && hasWritePermissions() && hasCameraPermissions() && hasAudioPermissions()
    }

    private fun hasAudioPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext!!,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext!!,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}