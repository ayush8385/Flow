package com.ayush.flow.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.ayush.flow.R

class ReplyReciever : BroadcastReceiver() {
    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent) {
        val userid: String = intent.getStringExtra("userid")!!
        val stringExtra: String = intent.getStringExtra("title")!!
        val resultsFromIntent: Bundle = RemoteInput.getResultsFromIntent(intent)

        val remoteInput: Bundle = resultsFromIntent

        if (remoteInput != null) {
            val charSequence: CharSequence = remoteInput.getCharSequence("key_text_reply", "")
//            val parseInt: Int = Regex("[\\D]").replace(userid, "").toInt()
            val message = Message()
//            message.setNotify(true)
            message.sendMessageToUser(charSequence.toString(), userid,stringExtra,"","").execute()


            val str: String = "com.ayush.flow.WORK_EMAIL"

            val j: Int = Regex("[\\D]").replace(userid, "").toInt()

            if (Dashboard().hashMap.containsKey(j)) {
                val messagingStyle = Dashboard().hashMap[j]
                Dashboard().messageStyle = messagingStyle
            } else {
                Dashboard().messageStyle = NotificationCompat.MessagingStyle("You")
                Dashboard().hashMap[j] = Dashboard().messageStyle
            }

            val notificationBuilder = NotificationCompat.Builder(context!!, "com.ayush.flow")
                .setStyle( Dashboard().messageStyle)
                .setSmallIcon(R.drawable.flow).
                setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setOnlyAlertOnce(true)
                .setGroup(str)
                .setAutoCancel(true)
                .setColor(R.color.white)
                .setPriority(1)

            Dashboard().messageStyle!!.addMessage(charSequence.toString(),"768768".toLong(),"You")


            val notificationManager= NotificationManagerCompat.from(context!!)
            notificationManager.notify(j,notificationBuilder.build())
        }
    }
    companion object{
        const val CHANNEL_ID="com.ayush.flow"
        private const val CHANNEL_NAME="Flow"
    }
}