package com.ayush.flow.activityimport android.R.*import android.app.*import android.app.ActivityManager.RunningAppProcessInfoimport android.content.Contextimport android.content.Intentimport android.graphics.Bitmapimport android.os.*import android.util.Logimport android.widget.Toastimport androidx.core.app.NotificationCompatimport androidx.core.app.Personimport androidx.core.graphics.drawable.IconCompatimport com.ayush.flow.Notification.MessagingServiceimport com.ayush.flow.activity.Dashboard.Companion.CHANNEL_IDimport com.ayush.flow.database.ChatViewModelimport com.google.firebase.auth.FirebaseAuthimport com.google.firebase.database.DataSnapshotimport com.google.firebase.database.DatabaseErrorimport com.google.firebase.database.FirebaseDatabaseimport com.google.firebase.database.ValueEventListenerimport com.sinch.android.rtc.*import com.sinch.android.rtc.calling.*import com.sinch.android.rtc.video.VideoControllerimport com.sinch.android.rtc.video.VideoScalingTypeimport java.util.*class SinchService : Service(){    private var audioPlayer: AudioPlayer? = null    private val mSinchServiceInterface: SinchServiceInterface = SinchServiceInterface()    private var mSinchClient: SinchClient? = null    var userName: String? = null        private set    private var mListener: StartFailedListener? = null    override fun onCreate() {        val firebaseUser= FirebaseAuth.getInstance().currentUser        start(firebaseUser!!.uid)        super.onCreate()    }    override fun onDestroy() {        if (mSinchClient != null && mSinchClient!!.isStarted) {            mSinchClient!!.terminate()        }        super.onDestroy()    }    private fun start(userName: String) {        if (mSinchClient == null) {            this.userName = userName            mSinchClient =                Sinch.getSinchClientBuilder().context(getApplicationContext()).userId(userName)                    .applicationKey(APP_KEY)                    .applicationSecret(APP_SECRET)                    .environmentHost(ENVIRONMENT).build()            mSinchClient!!.setSupportCalling(true)            mSinchClient!!.setSupportManagedPush(true)            mSinchClient!!.startListeningOnActiveConnection()            mSinchClient!!.addSinchClientListener(MySinchClientListener())            mSinchClient!!.getCallClient().addCallClientListener(SinchCallClientListener())            mSinchClient!!.videoController.setResizeBehaviour(VideoScalingType.ASPECT_FILL)            mSinchClient!!.setPushNotificationDisplayName(userName)            mSinchClient!!.start()        }    }    private fun createClientIfNecessary() {        if (mSinchClient != null) return        start(FirebaseAuth.getInstance().currentUser!!.uid)    }    private fun stop() {        if (mSinchClient != null) {            mSinchClient!!.terminateGracefully()            mSinchClient = null        }    }    private val isStarted: Boolean        private get() = mSinchClient != null && mSinchClient!!.isStarted    override fun onBind(intent: Intent?): IBinder {        return mSinchServiceInterface    }    inner class SinchServiceInterface : Binder() {        fun callPhoneNumber(phoneNumber: String?): Call {            return mSinchClient!!.callClient.callPhoneNumber(phoneNumber)        }        fun callUserVideo(userId: String?):Call{            return mSinchClient!!.callClient.callUserVideo(userId)        }        fun callUser(userId: String?): Call {            return mSinchClient!!.callClient.callUser(userId)        }        fun callUser(userId: String?, headers: Map<String?, String?>?): Call {            return mSinchClient!!.callClient.callUser(userId, headers)        }        val isStarted: Boolean            get() = this@SinchService.isStarted        fun startClient(userName: String) {            start(userName)        }        fun stopClient() {            stop()        }        fun setStartListener(listener: StartFailedListener?) {            mListener = listener        }        fun getCall(callId: String?): Call {            return mSinchClient!!.callClient.getCall(callId)        }        fun muteCall() {            mSinchClient!!.audioController.mute()        }        fun unmuteCall() {            mSinchClient!!.audioController.unmute()        }        fun onSpeaker() {            mSinchClient!!.audioController.enableSpeaker()        }        fun offSpeaker() {            mSinchClient!!.audioController.disableSpeaker()        }        fun relayRemotePushNotificationPayload(payload: MutableMap<String, String>?): NotificationResult? {            createClientIfNecessary()            return mSinchClient!!.relayRemotePushNotificationPayload(payload)        }        fun getVideoController(): VideoController? {            return if (!isStarted) {                null            } else mSinchClient!!.videoController        }    }    interface StartFailedListener {        fun onStartFailed(error: SinchError?)        fun onStarted()    }    private inner class MySinchClientListener : SinchClientListener {        override fun onClientFailed(client: SinchClient, error: SinchError) {            if (mListener != null) {                mListener!!.onStartFailed(error)            }            mSinchClient!!.terminate()            mSinchClient = null        }        override fun onClientStarted(client: SinchClient) {            Log.d(TAG, "SinchClient started")            if (mListener != null) {                mListener!!.onStarted()            }        }        override fun onClientStopped(client: SinchClient) {            Log.d(TAG, "SinchClient stopped")        }        override fun onLogMessage(level: Int, area: String, message: String) {            when (level) {                Log.DEBUG -> Log.d(area, message)                Log.ERROR -> Log.e(area, message)                Log.INFO -> Log.i(area, message)                Log.VERBOSE -> Log.v(area, message)                Log.WARN -> Log.w(area, message)            }        }        override fun onRegistrationCredentialsRequired(client: SinchClient, clientRegistration: ClientRegistration) {        }    }    inner class SinchCallClientListener : CallClientListener {        override fun onIncomingCall(callClient: CallClient?, call: Call) {            val intent:Intent            val title:String            if(call.details.isVideoOffered){                intent = Intent(this@SinchService, Incoming_vdo::class.java)                title="Incoming Video Call"            }            else{                intent = Intent(this@SinchService, Calling::class.java)                title="Incoming Audio Call"            }            intent.putExtra(CALL_ID, call.callId)            intent.flags =                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK            //startActivity(intent)            audioPlayer = AudioPlayer(applicationContext)            call.addCallListener(SinchCallListener())            val inForeground: Boolean = isAppOnForeground(applicationContext)            if (inForeground) {                Toast.makeText(applicationContext,"App is in Foreground",Toast.LENGTH_LONG).show()                this@SinchService.startActivity(intent)            }            else {                Log.e("hell...........","App is in Background")                if (!audioPlayer!!.isPlayedRingtone()) {                    audioPlayer!!.playRingtone()                }                val callintent = Intent(this@SinchService, Outgoing_vdo::class.java)                callintent.putExtra("name","naame")                callintent.putExtra(SinchService.CALL_ID, call.callId)                (Objects.requireNonNull(getSystemService(NOTIFICATION_SERVICE)) as NotificationManager).notify(1,createIncomingCallNotification(call.remoteUserId,callintent, intent,title,1)                )            }        }        private fun isAppOnForeground(context: Context): Boolean {            val activityManager =                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager            var appProcesses: List<RunningAppProcessInfo>? = null            if (activityManager != null) {                appProcesses = activityManager.runningAppProcesses            }            if (appProcesses == null) {                return false            }            val packageName: String = context.getPackageName()            for (appProcess in appProcesses) {                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {                    return true                }            }            return false        }        private fun getPendingIntent(intent: Intent, action: String): PendingIntent? {            intent.action = action            return PendingIntent.getActivity(                applicationContext,                111,                intent,                PendingIntent.FLAG_UPDATE_CURRENT            )        }        fun createIncomingCallNotification(userId: String,callIntent:Intent, fullScreenIntent: Intent,title:String,n:Int): Notification? {            val pendingIntent = PendingIntent.getActivity(applicationContext, 112, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)            val callinIntent =  PendingIntent.getActivity(applicationContext,113,callIntent,PendingIntent.FLAG_UPDATE_CURRENT)            var messageStyle: NotificationCompat.MessagingStyle =                NotificationCompat.MessagingStyle("")            val builder: NotificationCompat.Builder            if(n==1){                builder =                    NotificationCompat.Builder(applicationContext, CHANNEL_ID)                        .setStyle(messageStyle)                        .setSmallIcon(com.ayush.flow.R.drawable.flow)                        .setPriority(NotificationCompat.PRIORITY_HIGH)                        .setCategory(NotificationCompat.CATEGORY_CALL)                        .setContentIntent(pendingIntent)                        .setColor(com.ayush.flow.R.color.purple_500)                        .setFullScreenIntent(pendingIntent, true)                        .setOngoing(true)                if(title=="Incoming Video Call"){                    builder.addAction(R.drawable.common_google_signin_btn_icon_dark, "Answer", callinIntent)                    builder.addAction(R.drawable.common_google_signin_btn_icon_dark_focused, "Ignore", getPendingIntent(fullScreenIntent, "ignore"))                }                else{                    builder.addAction(R.drawable.common_google_signin_btn_icon_dark, "Answer", getPendingIntent(fullScreenIntent,"answer"))                    builder.addAction(R.drawable.common_google_signin_btn_icon_dark_focused, "Ignore", getPendingIntent(fullScreenIntent, "ignore"))                }            }            else{                 builder =                    NotificationCompat.Builder(applicationContext, CHANNEL_ID)                        .setStyle(messageStyle)                        .setSmallIcon(com.ayush.flow.R.drawable.flow)                        .setPriority(NotificationCompat.PRIORITY_HIGH)                        .setContentIntent(pendingIntent)                        .setColor(com.ayush.flow.R.color.purple_500)                        .setAutoCancel(true)                        .addAction(R.drawable.common_google_signin_btn_icon_dark, "Call Back", getPendingIntent(fullScreenIntent, "call_back"))                        .addAction(R.drawable.common_google_signin_btn_icon_dark_focused, "Ignore", getPendingIntent(fullScreenIntent, "ignore"))            }            var userr:Person? = null            val cons= ChatViewModel(application).getChat(userId)            if(cons == null){                val ref= FirebaseDatabase.getInstance().reference.child("Users")                ref.addValueEventListener(object : ValueEventListener {                    override fun onDataChange(snapshot: DataSnapshot) {                        builder.setContentText(snapshot.child(userId).child("number").value.toString())                    }                    override fun onCancelled(error: DatabaseError) {                        TODO("Not yet implemented")                    }                })            }            else{                var name:String?=null                if(cons.name==""){                    name=cons.number                }                else{                    name=cons.name                }                val imgpath = cons.image                var bitmap: Bitmap?=null                if(imgpath!=""){                    bitmap = MessagingService().loadImage(userId).execute().get()                    userr = Person.Builder().setIcon(IconCompat.createWithBitmap(Dashboard().getCircularBitmap(bitmap!!))).setName(name).build()                }                else{                    userr=Person.Builder().setName(name).build()                }            }            messageStyle.addMessage(title,43876483778,userr)            return builder.build()        }    }    fun cancelNotification() {        val nMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager        nMgr?.cancel(1)    }    inner class SinchCallListener : CallListener {        override fun onCallEnded(call: Call) {            cancelNotification()            if(call.details.endCause==CallEndCause.CANCELED || call.details.endCause==CallEndCause.FAILURE || call.details.endCause==CallEndCause.NO_ANSWER ||call.details.endCause==CallEndCause.TIMEOUT){                val intent:Intent                val title:String                intent = Intent(this@SinchService, Dashboard::class.java)                if(call.details.isVideoOffered){                    title="Missed Video Call"                }                else{                    title="Missed Audio Call"                }                intent.putExtra(CALL_ID, call.callId)                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)                val callintent = Intent(this@SinchService, Outgoing_vdo::class.java)                callintent.putExtra("name","naame")                callintent.putExtra(SinchService.CALL_ID, call.callId)                (Objects.requireNonNull(getSystemService(NOTIFICATION_SERVICE)) as NotificationManager).notify(1,SinchCallClientListener().createIncomingCallNotification(call.remoteUserId,callintent,intent,title,2)                )            }            if (audioPlayer != null && audioPlayer!!.isPlayedRingtone()) {                audioPlayer!!.stopRingtone()            }        }        override fun onCallEstablished(call: Call) {            Log.d(TAG, "Call established")            if (audioPlayer != null && audioPlayer!!.isPlayedRingtone()) {                audioPlayer!!.stopRingtone()            }        }        override fun onCallProgressing(call: Call) {            Log.d(TAG, "Call progressing")        }        override fun onShouldSendPushNotification(call: Call, pushPairs: List<PushPair>) {            // no need to implement for managed push        }        private fun createMissedCallNotification(userId: String): Notification? {           // Dashboard().createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT)            val contentIntent = PendingIntent.getActivity(                applicationContext, 0,                Intent(applicationContext, Calling::class.java), 0            )            val builder: NotificationCompat.Builder = NotificationCompat.Builder(                applicationContext, MessagingService.CHANNEL_ID            )                .setSmallIcon(com.ayush.flow.R.drawable.flow)                .setContentTitle("Missed call from ")                .setContentText(userId)                .setColor(com.ayush.flow.R.color.purple_500)                .setContentIntent(contentIntent)                .setDefaults(Notification.DEFAULT_SOUND)                .setAutoCancel(true)            return builder.build()        }    }    companion object {        private val APP_KEY = "144a64d5-4b85-45a4-9b67-e7e8d5bca164"        private val APP_SECRET = "EHXpXse6cU2qf6NVFGXmpA=="        private val ENVIRONMENT = "clientapi.sinch.com"        const val LOCATION = "LOCATION"        const val CALL_ID = "CALL_ID"        val TAG = SinchService::class.java.simpleName    }}