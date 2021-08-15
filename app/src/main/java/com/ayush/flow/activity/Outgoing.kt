package com.ayush.flow.activity

import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.calling.CallListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
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
    lateinit var backimg:ImageView


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
                mute.setBackgroundColor(0)
                mute.setBackgroundColor(R.drawable.bottom_back)
                isMute=true
            }
            else{
                sinchServiceInterface!!.unmuteCall()
                mute.setBackgroundColor(0)
                mute.setBackgroundColor(R.drawable.story_unread_back)
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
                speeaker.setBackgroundColor(R.drawable.bottom_back)
                isSpeaker=true
            }
            else{
                sinchServiceInterface!!.offSpeaker()
                speeaker.setBackgroundColor(R.drawable.story_unread_back)
                isSpeaker=false
            }
        }
        mCallStart = System.currentTimeMillis()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)!!

    }

    override fun onServiceConnected() {
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)
        if (call != null) {
            call.addCallListener(SinchCallListener())
            mCallerName.text=intent.getStringExtra("name")
            mCallState.setText(call.getState().toString())

            if(intent.getStringExtra("image")!=""){
                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),intent.getStringExtra("image")!!)
                val b = BitmapFactory.decodeStream(FileInputStream(f))
                mCallerimg.setImageBitmap(b)

                backimg.setImageBitmap(b)
            }
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

//    override fun onPause() {
//        super.onPause()
//     //   mDurationTask.cancel()
//        mTimer!!.cancel()
//    }

//    override fun onResume() {
//        super.onResume()
//        mTimer = Timer()
//        mDurationTask = UpdateCallDurationTask()
//        mTimer!!.schedule(mDurationTask, 0, 500)
//    }

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
            val cause: CallEndCause = call.getDetails().getEndCause()
            Log.d(TAG, "Call ended. Reason: " + cause.toString())
            mAudioPlayer!!.stopProgressTone()
            volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
            val endMsg = "Call ended: " + call.getDetails().toString()
            Toast.makeText(this@Outgoing, endMsg, Toast.LENGTH_LONG).show()
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

        override fun onShouldSendPushNotification(call: Call?, pushPairs: List<PushPair?>?) {
            // Send a push through your push provider here, e.g. GCM
        }
    }

    companion object {
        val TAG = Outgoing::class.java.simpleName
    }

}