package com.ayush.flow.Notification

import android.annotation.SuppressLint
import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.*
import android.graphics.*
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.ayush.flow.R
import com.ayush.flow.activity.Calling
import com.ayush.flow.activity.Message
import com.ayush.flow.activity.ReplyReciever
import com.ayush.flow.activity.SinchService
import com.ayush.flow.activity.SinchService.SinchServiceInterface
import com.ayush.flow.database.ChatViewModel
import com.ayush.flow.database.ContactViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sinch.android.rtc.NotificationResult
import com.sinch.android.rtc.SinchHelpers
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


class MessagingService : FirebaseMessagingService(),ServiceConnection {

    private var notificationManager: NotificationManager? = null
    val CHANNEL_NAME = "Flow"
    private var messageStyle: MessagingStyle = MessagingStyle("Me")

    private var mSinchServiceInterface: SinchService.SinchServiceInterface? = null


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data: MutableMap<*, *> = remoteMessage.data
        if (SinchHelpers.isSinchPushPayload(data as MutableMap<String, String>?)) {
            object : ServiceConnection {
                private var payload: Map<*, *>? = null
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    val context = applicationContext
                   val sharedPreferences = context.getSharedPreferences("call shared", MODE_PRIVATE)
                    if (payload != null) {
                        val sinchService = service as SinchServiceInterface
                        if (sinchService != null) {
                            val result: NotificationResult =
                                sinchService.relayRemotePushNotificationPayload(payload as MutableMap<String, String>?)!!
                            // handle result, e.g. show a notification or similar
                            // here is example for notifying user about missed/canceled call:
                            if (result != null && result.isValid && result.isCall) {
                                val callResult = result.callResult

                                if (callResult != null && result.displayName != null) {
                                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                    editor.putString(callResult.remoteUserId, result.displayName)
                                    editor.apply()
                                    Toast.makeText(applicationContext,"Here I am", Toast.LENGTH_SHORT).show()
                                }
                                if (callResult != null && callResult.isCallCanceled) {

                                    var displayName = result.displayName
                                    if (displayName == null) {
                                        displayName = sharedPreferences.getString(
                                            callResult.remoteUserId,
                                            "n/a"
                                        )
                                    }
                                    createMissedCallNotification(if (!displayName.isEmpty()) displayName else callResult.remoteUserId)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        context.deleteSharedPreferences("call shared")
                                    }
                                }
                            }
                        }
                    }
                    payload = null
                }

                override fun onServiceDisconnected(name: ComponentName) {}
                fun relayMessageData(data: MutableMap<*, *>) {
                    payload = data
                    createNotificationChannel(NotificationManager.IMPORTANCE_MAX)
                    applicationContext.bindService(
                        Intent(
                            applicationContext,
                            SinchService::class.java
                        ), this, BIND_AUTO_CREATE
                    )
                }
            }.relayMessageData(data)
        }

        else{
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                     createNotificationChannel(notificationManager!!)

                    sendCall(remoteMessage)
            }
            else{
//                Toast.makeText(applicationContext,"Not sending Version Not Supported",Toast.LENGTH_SHORT).show()
            }
            sendOreoNotif(remoteMessage.data["sender"]!!, remoteMessage.data["message"]!!,remoteMessage.data["type"].toString(),this)
        }

    }

    private fun createMissedCallNotification(userId: String) {
        createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT)
        val contentIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, Calling::class.java), 0
        )
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext, CHANNEL_ID
        )
            .setSmallIcon(R.drawable.flow)
            .setContentTitle("Missed call from ")
            .setContentText(userId)
            .setContentIntent(contentIntent)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setAutoCancel(true)
        val mNotificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(2, builder.build())
    }

    private fun createNotificationChannel(importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Amar LPG"
            val description = "Incoming call notification"
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun sendCall(remoteMessage: RemoteMessage) {
        val map: Map<String, String> = remoteMessage.getData()
        val dataHashMap:MutableMap<String,String> = if (map is HashMap) map else HashMap(map)
        if (SinchHelpers.isSinchPushPayload(map)) {
            ///Check if the application is in foreground if in foreground the SinchService already run ////
            if (foregrounded()) {
                return
            }
            val intent = Intent(this, Calling::class.java)
            val callVo = NotificationCallVo()
            callVo.setData(dataHashMap)
            intent.putExtra("parcel", callVo)
            startActivity(intent)
        }
    }

    fun foregrounded(): Boolean {
        val appProcessInfo = RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(appProcessInfo)
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND
                || appProcessInfo.importance == IMPORTANCE_VISIBLE)
    }




    fun sendOreoNotif(sender:String, msg:String, type: String, context: Context) {
        if(ChatViewModel(application).isUserExist(sender)){
            val con= ChatViewModel(application).getChat(sender)
            var name:String?=null
            if(con.name==""){
                name=con.number
            }
            else{
                name=con.name
            }
            if(type=="1"){


            }
            else{
                sendNotif(sender,name,"",con.image,"", msg,context)
            }

        }
        else if(ContactViewModel(application).isUserExist(sender)){
            val chat= ContactViewModel(application).getContact(sender)
            if(type=="1"){
               // sendCallnf(sender,"Ayush",msg,this)
            }
            else{
                sendNotif(sender,chat.name,"",chat.image,"", msg,context)
            }

        }
        else{
            val ref= FirebaseDatabase.getInstance().reference.child("Users").child(sender)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("number").value.toString()
                    val img_url=snapshot.child("profile_photo").value.toString()
                    //convert img url to bitmap and save path to databse and pass to sendNotif

//                    if(type=="1"){
//                        sendCallnf(sender,"Ayush",msg,this)
//                    }
//                    else{
//                        sendNotif(sender,name,image, msg,context)
//                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

//        if (message.data["title"].equals("You")) {
//            val refer = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
//            refer.addListenerForSingleValueEvent(object :ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val img_url
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//
//            })
//            val i2 = j
//            val str3 = user
//            val intent2: Intent = intent
//            return
//        }
    }

    @SuppressLint("ResourceAsColor", "ResourceType")
    fun sendNotif(sender: String, name: String?,reply_name:String,imgpath: String?,reply_img:String,msg: String?,context: Context){

        val notId: Int = Regex("[\\D]").replace(sender, "").toInt()

        val hashMap: HashMap<Int, MessagingStyle> = messsageHashmap
        if (hashMap.containsKey(notId)) {
            val messagingStyle = hashMap[notId]
            if(messagingStyle!=null){
                messageStyle = messagingStyle
            }
        } else {
            messageStyle = MessagingStyle(sender)
            hashMap[notId] = messageStyle
        }
        val defaultSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)



        val intent = Intent(context, Message::class.java)
        intent.putExtra("userid",sender)
        intent.putExtra("name",name)
        intent.putExtra("number","")
        intent.putExtra("image",imgpath)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context,notId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        var remoteInput:androidx.core.app.RemoteInput=androidx.core.app.RemoteInput.Builder("key_text_reply").run {
            setLabel("Reply...")
            build()
        }

        val replyIntent = Intent(context, ReplyReciever::class.java)
        replyIntent.putExtra("userid",sender)
        replyIntent.putExtra("title", name)
        replyIntent.putExtra("image",imgpath)
        val pendingReplyIntent: PendingIntent = PendingIntent.getBroadcast(context, notId, replyIntent, FLAG_UPDATE_CURRENT)

        val replyAction = NotificationCompat.Action.Builder(R.color.white, "Reply" as CharSequence, pendingReplyIntent).addRemoteInput(remoteInput).build()


        val builder = Person.Builder()
        var bitmap:Bitmap?=null
        var userr:Person? = null
        if(imgpath!=""){
            if(reply_name=="You"){
                bitmap = loadMyImage(reply_img).execute().get()
                userr = builder.setIcon(IconCompat.createWithBitmap(getCircularBitmap(bitmap!!))).setName(reply_name).build()
            }
            else{
                bitmap = loadImage(imgpath!!).execute().get()
                userr = builder.setIcon(IconCompat.createWithBitmap(getCircularBitmap(bitmap!!))).setName(name).build()
            }
        }
        else{
            userr=builder.setName(name).build()
        }

        val notificationBuilder = NotificationCompat.Builder(context, "com.ayush.flow")
            .setContentIntent(pendingIntent)
            .setStyle(messageStyle)
            .setSmallIcon(R.drawable.flow)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setOnlyAlertOnce(true)
            .setGroup(GROUP_KEY_WORK_EMAIL)
            .setColor(R.color.white)
            .setSound(defaultSound)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
          //  .addAction(replyAction)


        val summaryNotification = NotificationCompat.Builder(context, "com.ayush.flow")
            .setSmallIcon(R.drawable.flow)
            .setColor(R.color.purple_500)
            .setStyle( NotificationCompat.InboxStyle().setSummaryText("New Messages"))
            .setGroup(GROUP_KEY_WORK_EMAIL)
            .setGroupSummary(true).build()


        messageStyle.addMessage(msg as CharSequence?, "7678".toLong(), userr)
        c++

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(notId, notificationBuilder.build())
        notificationManager.notify(0, summaryNotification)

        if(reply_name=="You"){
           // Message().sendMessageToUser(msg!!, sender, name!!,"","").execute()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(CHANNEL_ID,CHANNEL_NAME,IMPORTANCE_HIGH).apply {
            description="My Flow description"
            enableVibration(true)
            lightColor = Color.GREEN
            enableLights(true)
            lockscreenVisibility=Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }


    companion object {
        const val CHANNEL_ID = "com.ayush.flow"
        const val GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL"

        var c = 0
        var image: String? = null

        val messsageHashmap = HashMap<Int, MessagingStyle>()
        var title: String? = null
    }

    inner class loadImage(val image:String): AsyncTask<Void, Void, Bitmap>(){

        override fun doInBackground(vararg params: Void?): Bitmap {
            var b:Bitmap?=null
            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),image)
            if(f.exists()){
                b = BitmapFactory.decodeStream(FileInputStream(f))
            }
             return b!!
        }
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val bitmap2: Bitmap
        val r: Float
        if (bitmap.getWidth() > bitmap.getHeight()) {
            bitmap2 = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888)
        } else {
            bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888)
        }
        val output: Bitmap = bitmap2
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
        if(bitmap.getWidth() > bitmap.getHeight()){
            r= (bitmap.getHeight() / 2).toFloat()
        }
        else {
            r= (bitmap.getWidth()/2).toFloat()
        }
        paint.setAntiAlias(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(-12434878)
        canvas.drawCircle(r, r, r, paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }


    inner class loadMyImage(val imgpath: String?):AsyncTask<Void,Void,Bitmap>(){
        var b:Bitmap?=null
        override fun doInBackground(vararg params: Void?): Bitmap {
            try {
                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Flow Profile photos"),imgpath)
                b = BitmapFactory.decodeStream(FileInputStream(f))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return b!!
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (SinchService::class.java.name == name!!.className) {
            mSinchServiceInterface = service as SinchService.SinchServiceInterface
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        if (SinchService::class.java.name == name!!.getClassName()) {
            mSinchServiceInterface = null
        }
    }


//    inner class sendingCall():BaseActivity(){
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            sinchServiceInterface!!.startClient(FirebaseAuth.getInstance().currentUser!!.uid)
//           // sendCallnf(sender,"Ayush",msg,this)
//        }
//    }
}