package com.ayush.flow.activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ayush.flow.Notification.*
import com.ayush.flow.R
import com.ayush.flow.Services.APIService
import com.ayush.flow.Services.Constants
import com.ayush.flow.database.CallEntity
import com.ayush.flow.database.CallHistoryEntity
import com.ayush.flow.database.CallHistoryViewModel
import com.ayush.flow.database.CallViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.calling.CallListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.util.*


class Outgoing : BaseActivity() {

    private var mAudioPlayer: AudioPlayer? = null
    private var mTimer: Timer? = null
    private var mDurationTask: UpdateCallDurationTask? = null
    lateinit var mCallId: String
    private var mCallStart: Long = 0
    lateinit var mCallDuration: TextView
    lateinit var mCallState: TextView
    lateinit var mCallerName: TextView
    lateinit var mCallerimg:CircleImageView
    lateinit var mute:CircleImageView
    var apiService: APIService?=null
    lateinit var speeaker:CircleImageView
    var isMute:Boolean=false
    var isSpeaker:Boolean=false
    lateinit var userid: String
    lateinit var backimg: ImageView
    var sensorManager: SensorManager? = null
    var proximitySensor: Sensor? = null


    private inner class UpdateCallDurationTask : TimerTask() {
        override fun run() {
            runOnUiThread { updateCallDuration() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing)

        mAudioPlayer = AudioPlayer(this)
        mCallDuration = findViewById<View>(R.id.timer) as TextView
        mCallerName = findViewById<View>(R.id.user_name) as TextView
        mCallState = findViewById<View>(R.id.status) as TextView
        mCallerimg=findViewById(R.id.user_img)
        backimg=findViewById(R.id.back_img)



        apiService= Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)
        val endCallButton = findViewById<View>(R.id.end_btn) as CircleImageView
        endCallButton.setOnClickListener { endCall() }
        mute=findViewById(R.id.mic)
        mute.setOnClickListener {
            if(!isMute){
                sinchServiceInterface!!.muteCall()
                mute.setBackgroundResource(R.drawable.controls_back)
                isMute=true
            }
            else{
                sinchServiceInterface!!.unmuteCall()
                mute.setBackgroundResource(0)
                isMute=false
            }
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        speeaker=findViewById(R.id.speaker)
        speeaker.setOnClickListener {
            if(!isSpeaker){
                sinchServiceInterface!!.onSpeaker()
                speeaker.setBackgroundResource(R.drawable.controls_back)
                isSpeaker=true
            }
            else{
                sinchServiceInterface!!.offSpeaker()
                speeaker.setBackgroundResource(0)
                isSpeaker=false
            }
        }
        mCallStart = System.currentTimeMillis()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)!!


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
                    params.screenBrightness = -2F
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

    override fun onServiceConnected() {
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)
        if (call != null) {
            call.addCallListener(SinchCallListener())
            mCallerName.text=intent.getStringExtra("name")
            mCallState.setText(call.getState().toString())

            val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),intent.getStringExtra("userid")+".jpg")
            Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
                DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(mCallerimg)

            Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
                DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(backimg)

//            CallViewModel(application).inserCall(CallEntity(mCallerName.text.toString(),"audio","out",System.currentTimeMillis(),"", intent.getStringExtra("userid")!!))

        } else {
            Log.e(TAG, "Started with invalid callId, aborting.")
            finish()
        }
    }

    public override fun onPause() {
        super.onPause()
        mDurationTask!!.cancel()
        mTimer!!.cancel()
    }

    public override fun onResume() {
        super.onResume()
        mTimer = Timer()
        mDurationTask = UpdateCallDurationTask()
        mTimer!!.schedule(mDurationTask, 0, 500)
    }

    override fun onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private fun endCall() {
     //   mAudioPlayer.stopProgressTone()
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)
        if (call != null) {
            call.hangup()
        }
        finish()
    }

    private fun formatTimespan(timespan: Long): String {
        val totalSeconds = timespan / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }


    private fun updateCallDuration() {
        if (mCallStart > 0) {
            mCallDuration.text = formatTimespan(System.currentTimeMillis() - mCallStart)
        }
    }

    private inner class SinchCallListener : CallListener {
        override fun onCallEnded(call: Call) {
            val callDetail = call.details
            val cause: CallEndCause = callDetail.getEndCause()

            var callStatus=CallHistoryViewModel(application).getCallStatus(mCallId)
            if(cause.toString()=="CANCELED" && !Constants.isCurrentUser){
                callStatus="missed"
            }

            CallViewModel(application).inserCall(CallEntity(mCallerName.text.toString(),"audio",callStatus,callDetail.startedTime.time,callDetail.duration,call.remoteUserId))
            CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(mCallerName.text.toString(),"audio",callStatus,callDetail.startedTime.time,callDetail.duration,call.remoteUserId,mCallId))

            mAudioPlayer!!.stopProgressTone()
            volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
            val endMsg = "Call ended: " + call.getDetails().toString()
            //Toast.makeText(this@Outgoing, endMsg, Toast.LENGTH_LONG).show()
            Toast.makeText(this@Outgoing,"Call Ended",Toast.LENGTH_SHORT).show()
            sinchServiceInterface!!.offSpeaker()
            sinchServiceInterface!!.unmuteCall()
            endCall()
        }

        override fun onCallEstablished(call: Call) {
            Log.d(TAG, "Call established")
            mAudioPlayer!!.stopProgressTone()
            mCallState!!.setText(call.getState().toString())
            volumeControlStream = AudioManager.STREAM_VOICE_CALL
            mCallStart = System.currentTimeMillis()
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
        override fun onCallProgressing(call: Call) {
            Log.d(TAG, "Call progressing")
            mAudioPlayer!!.playProgressTone()
        }

//        override fun onShouldSendPushNotification(call: Call?, pushPairs: List<PushPair?>?) {
//            // Send a push through your push provider here, e.g. GCM
//        }
    }

    companion object {
        val TAG = Outgoing::class.java.simpleName
    }

}