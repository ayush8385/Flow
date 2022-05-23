package com.ayush.flow.activity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.ayush.flow.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.calling.CallListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import android.view.animation.CycleInterpolator

import android.view.animation.Animation
import android.view.animation.AnimationUtils

import android.view.animation.RotateAnimation
import android.widget.EditText
import android.view.View.OnTouchListener
import com.ayush.flow.Services.Constants
import com.ayush.flow.database.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.security.Permissions


class Calling : BaseActivity(){
    private var mCallId: String? = null
    lateinit var mCallerimg:CircleImageView
    private var mAudioPlayer: AudioPlayer? = null
    lateinit var remoteUser:TextView
    var mAction=""
    var sensorManager: SensorManager? = null
    var proximitySensor: Sensor? = null
    lateinit var backimg: ImageView
    lateinit var pickintent:Intent
    lateinit var image:String

    lateinit var text1:TextView
    lateinit var text2:TextView
    lateinit var callMsg:EditText
    protected var proximityWakelock: PowerManager.WakeLock? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ayush.flow.R.layout.activity_calling)


        remoteUser = findViewById(com.ayush.flow.R.id.user_name)

        text1=findViewById(R.id.text1)
        text2=findViewById(R.id.text2)
        callMsg=findViewById(R.id.call_msg)

        val answer = findViewById(com.ayush.flow.R.id.pick_btn) as CircleImageView
        answer.setOnClickListener(mClickListener)

        val animShake = AnimationUtils.loadAnimation(this, R.anim.shake)
        answer.startAnimation(animShake)

        animShake.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                Handler().postDelayed({
                    answer.startAnimation(animShake)
                }, 1000)

            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        val decline = findViewById(com.ayush.flow.R.id.end_btn) as CircleImageView
        decline.setOnClickListener(mClickListener)

        mCallerimg=findViewById(R.id.user_img)
        backimg=findViewById(R.id.back_img)

        pickintent = Intent(this, Outgoing::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.TYPE_CHANGED
        )

        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer!!.playRingtone()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)


        //sensor works
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY);


        if (proximitySensor == null) {
            Toast.makeText(this, "No proximity sensor found in device.", Toast.LENGTH_SHORT).show();

        } else {
            // registering our sensor with sensor manager.
            sensorManager!!.registerListener(proximitySensorEventListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        }


    }

    var proximitySensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // method to check accuracy changed in sensor.
        }

        override fun onSensorChanged(event: SensorEvent) {
            // check if the sensor type is proximity sensor.
            val params: WindowManager.LayoutParams = getWindow().getAttributes()
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {


                if (event.values[0].toInt() == 0) {
                    params.flags=WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    params.screenBrightness = 0F
                    window.attributes = params
                    Log.e("onSensorChanged", "NEAR")
                } else {

                    params.flags=WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    params.screenBrightness = -1F
                    window.attributes = params
                    Log.e("onSensorChanged", "FAR")
                }
            }
            else{
                Handler().postDelayed({

                    Toast.makeText(applicationContext,"Hello I'm worjing ",Toast.LENGTH_LONG).show()
                },3000)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intent=intent
        if(intent!=null){
            if(intent.getSerializableExtra(SinchService.CALL_ID)!=null){
                mCallId = getIntent().getStringExtra(SinchService.CALL_ID)
            }
            val notificationManager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)
            if(intent.action!=null){
                mAction= intent.action!!
            }
        }
    }




    override fun onServiceConnected() {
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)

        CallViewModel(application).inserCall(CallEntity(remoteUser.text.toString(),"audio","incoming",System.currentTimeMillis(),0,call.remoteUserId))
        CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(remoteUser.text.toString(),"audio","incoming",System.currentTimeMillis(),0,call.remoteUserId,mCallId!!))


        var username=""
        var usernumber=""
        if (call != null) {

            call.addCallListener(SinchCallListener())

            var cons=ContactViewModel(application).getContact(call.remoteUserId)
            if(cons == null){
                var chat = ChatViewModel(application).getChat(call.remoteUserId)
                if(chat==null){

                    val ref=FirebaseDatabase.getInstance().reference.child("Users")
                    ref.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            remoteUser.setText(snapshot.child(call.remoteUserId).child("number").value.toString())

                            val url=snapshot.child(call.remoteUserId).child("profile_photo").value.toString()
                            usernumber=snapshot.child(call.remoteUserId).child("number").value.toString()

                            if(url!=""){
                                Picasso.get().load(url).into(mCallerimg)
                                Picasso.get().load(url).into(backimg)

                            }
                            image=""
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }
                else{

                    remoteUser.setText(chat.number)
                    pickintent.putExtra(SinchService.CALL_ID, mCallId)
                    pickintent.putExtra("name",remoteUser.text)

                    username=chat.name
                    usernumber=chat.number

                    val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),chat.id+".jpg")

                    Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
                        DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(backimg)
                    Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
                        DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(mCallerimg)

                    pickintent.putExtra("userid",call.remoteUserId)

                }
            }
            else{

                remoteUser.setText(cons.name)

                pickintent.putExtra(SinchService.CALL_ID, mCallId)
                pickintent.putExtra("name",remoteUser.text)

                username=cons.name
                usernumber=cons.number

                val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),cons.id+".jpg")

                Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
                    DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(backimg)
                Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
                    DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(mCallerimg)

                pickintent.putExtra("userid",call.remoteUserId)

            }

            if ("answer".equals(mAction)) {
                mAction = "";
                answerClicked(pickintent);
            }
            else if ("ignore".equals(mAction)) {
                mAction = "";
                declineClicked();
            }

        } else {
            Log.e(TAG, "Started with invalid callId, aborting")
            finish()
        }

        if(com.ayush.flow.Services.Permissions().checkMicpermission(this)){

        }
        else{
            com.ayush.flow.Services.Permissions().requestMicPermission(this)
        }

        text1.setOnClickListener {
            val ref=FirebaseDatabase.getInstance().reference
            val messageKey=ref.push().key
            MessageViewModel(application).insertMessage(
                MessageEntity(messageKey!!,
                    Constants.MY_USERID+"-"+call.remoteUserId,
                    Constants.MY_USERID,text1.text.toString(),System.currentTimeMillis(),"message","","","",false,false,false)
            )
            Message().sendMessageToUser(text1.text.toString(),messageKey,call.remoteUserId,username,usernumber,image).execute()
            call.hangup()
            finish()
        }
        text2.setOnClickListener {
            val ref=FirebaseDatabase.getInstance().reference
            val messageKey=ref.push().key
            MessageViewModel(application).insertMessage(
                MessageEntity(messageKey!!,
                    Constants.MY_USERID+"-"+call.remoteUserId,
                    Constants.MY_USERID,text2.text.toString(),System.currentTimeMillis(),"message","","","",false,false,false)
            )
            Message().sendMessageToUser(text2.text.toString(),messageKey,call.remoteUserId,username,usernumber,image).execute()
            call.hangup()
            finish()
        }


        callMsg.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= callMsg.getRight() - callMsg.getCompoundDrawables()
                        .get(DRAWABLE_RIGHT).getBounds().width()
                ) {
                    val ref=FirebaseDatabase.getInstance().reference
                    val messageKey=ref.push().key
                    MessageViewModel(application).insertMessage(
                        MessageEntity(messageKey!!,
                            Constants.MY_USERID+"-"+call.remoteUserId,
                            Constants.MY_USERID,callMsg.text.toString(),System.currentTimeMillis(),"message","","","",false,false,false)
                    )
                    Message().sendMessageToUser(callMsg.text.toString(),messageKey,call.remoteUserId,username,usernumber,image).execute()
                    call.hangup()
                    finish()
                    return@OnTouchListener true
                }
            }
            false
        })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            104 -> {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                }
//                else{
//                    mAudioPlayer!!.stopRingtone()
//                    val call: Call = sinchServiceInterface!!.getCall(mCallId)
//                    if (call != null) {
//                        call.hangup()
//                        finish()
//                    } else {
//                        finish()
//                    }
//                }
//                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }
    }

    private fun answerClicked(intent: Intent) {
        mAudioPlayer!!.stopRingtone()
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)
        if (call != null) {
            startActivity(intent)

            if(com.ayush.flow.Services.Permissions().checkMicpermission(this)){
                call.answer()
            }
            else{
                com.ayush.flow.Services.Permissions().requestMicPermission(this)
            }

        } else {
            finish()
        }
    }

    private fun declineClicked() {
        mAudioPlayer!!.stopRingtone()
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)
        if (call != null) {
            call.hangup()
        }
        finish()
    }

    private inner class SinchCallListener : CallListener {
        override fun onCallEnded(call: Call) {

            val callDetail = call.details
            val cause = callDetail.endCause
            Log.d("Ended", "Call ended, cause: " + cause)

            var callStatus=CallHistoryViewModel(application).getCallStatus(mCallId)
            if(cause.toString()=="CANCELED"){
                callStatus="missed"
            }

            CallViewModel(application).inserCall(CallEntity(remoteUser.text.toString(),"audio",callStatus,callDetail.startedTime,callDetail.duration,call.remoteUserId))
            CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(remoteUser.text.toString(),"audio",callStatus,callDetail.startedTime,callDetail.duration,call.remoteUserId,mCallId!!))


            mAudioPlayer!!.stopRingtone()
            finish()
        }

        override fun onCallEstablished(call: Call?) {
            Log.d(TAG, "Call established")
        }

        override fun onCallProgressing(call: Call?) {
            Log.d(TAG, "Call progressing")
        }

        override fun onShouldSendPushNotification(call: Call?, pushPairs: List<PushPair?>?) {
            // Send a push through your push provider here, e.g. GCM
        }
    }

    private val mClickListener = View.OnClickListener { v ->
        when (v.id) {
            com.ayush.flow.R.id.pick_btn -> answerClicked(pickintent)
            com.ayush.flow.R.id.end_btn -> declineClicked()
        }
    }

    companion object {
        val TAG = Calling::class.java.simpleName
    }
}