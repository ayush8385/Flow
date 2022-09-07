package com.ayush.flow.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.app.RemoteInput
import com.ayush.flow.Notification.MessagingService

class ReplyReciever : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        val userid: String = intent.getStringExtra("userid")!!
        val stringExtra: String = intent.getStringExtra("title")!!
        val sender_img:String=intent.getStringExtra("image")!!

        val resultsFromIntent: Bundle = RemoteInput.getResultsFromIntent(intent)!!

        val sharedPreferences:SharedPreferences = context!!.getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)

        val remoteInput: Bundle = resultsFromIntent

        if (remoteInput != null) {
            val charSequence: CharSequence = remoteInput.getCharSequence("key_text_reply", "")
//            val parseInt: Int = Regex("[\\D]").replace(userid, "").toInt()

            MessagingService().sendNotif(userid,stringExtra,"You",sender_img, sharedPreferences.getString("profile","")!!,charSequence.toString(),context)


//            val str: String = "com.ayush.flow.WORK_EMAIL"
//
//            val j: Int = Regex("[\\D]").replace(userid, "").toInt()
//
//            if (Dashboard().hashMap.containsKey(j)) {
//                val messagingStyle = Dashboard().hashMap[j]
//                Dashboard().messageStyle = messagingStyle
//            } else {
//                Dashboard().messageStyle = NotificationCompat.MessagingStyle("You")
//                Dashboard().hashMap[j] = Dashboard().messageStyle
//            }
//
//            val notificationBuilder = NotificationCompat.Builder(context!!, "com.ayush.flow")
//                .setStyle( Dashboard().messageStyle)
//                .setSmallIcon(R.drawable.flow).
//                setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                .setOnlyAlertOnce(true)
//                .setGroup(str)
//                .setAutoCancel(true)
//                .setColor(R.color.white)
//                .setPriority(1)
//
//            Dashboard().messageStyle!!.addMessage(charSequence.toString(),"768768".toLong(),"You")
//
//
//            val notificationManager= NotificationManagerCompat.from(context!!)
//            notificationManager.notify(j,notificationBuilder.build())
        }
    }
}