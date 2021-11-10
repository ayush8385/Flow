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
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.PowerManager
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
import com.ayush.flow.database.CallEntity
import com.ayush.flow.database.CallViewModel
import com.ayush.flow.database.ChatViewModel
import com.ayush.flow.database.ContactViewModel
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
import com.bumptech.glide.Glide
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

        window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
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

                            val url=snapshot.child("profile_photo").value.toString()
                            usernumber=snapshot.child("number").value.toString()
                            if(url!=""){
                                Picasso.get().load(url).into(mCallerimg)
                                Picasso.get().load(url).into(backimg)

                            }
                            image="";
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

                    image=chat.image
                    username=chat.name
                    usernumber=chat.number

                    val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),chat.image)

                    Glide.with(this).load(f).placeholder(R.drawable.user).into(backimg)
                    Glide.with(this).load(f).placeholder(R.drawable.user).into(mCallerimg)

                    pickintent.putExtra("image",chat.image)

                }
            }
            else{
                remoteUser.setText(cons.name)

                pickintent.putExtra(SinchService.CALL_ID, mCallId)
                pickintent.putExtra("name",remoteUser.text)

                image=cons.image
                username=cons.name
                usernumber=cons.number

                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),cons.image)

                Glide.with(this).load(f).placeholder(R.drawable.user).into(backimg)
                Glide.with(this).load(f).placeholder(R.drawable.user).into(mCallerimg)

                pickintent.putExtra("image",cons.image)

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
            Message().sendMessageToUser(text1.text.toString(),call.remoteUserId,username,usernumber,image).execute()
            call.hangup()
            finish()
        }
        text2.setOnClickListener {
            Message().sendMessageToUser(text2.text.toString(),call.remoteUserId,username,usernumber,image).execute()
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
                    Message().sendMessageToUser(callMsg.text.toString(),call.remoteUserId,username,usernumber,image).execute()
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else{
                    mAudioPlayer!!.stopRingtone()
                    val call: Call = sinchServiceInterface!!.getCall(mCallId)
                    if (call != null) {
                        call.hangup()
                        finish()
                    } else {
                        finish()
                    }
                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }
    }

    private fun answerClicked(intent: Intent) {
        mAudioPlayer!!.stopRingtone()
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)
        if (call != null) {
            call.answer()

            startActivity(intent)
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
            val cause: CallEndCause = call.getDetails().getEndCause()
            Log.d(TAG, "Call ended, cause: " + cause.toString())

            CallViewModel(application).inserCall(
                CallEntity(remoteUser.text.toString(), image,cause.toString(), call.details.duration.toString(),call.remoteUserId)
            )

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