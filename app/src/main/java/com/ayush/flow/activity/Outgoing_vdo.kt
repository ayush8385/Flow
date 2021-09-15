package com.ayush.flow.activity

import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import com.ayush.flow.R
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.calling.CallState
import com.sinch.android.rtc.video.VideoCallListener
import com.sinch.android.rtc.video.VideoScalingType
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*











class Outgoing_vdo : BaseActivity() {
    private var mAudioPlayer: AudioPlayer? = null
    private var mTimer: Timer? = null
    private var mDurationTask: UpdateCallDurationTask? = null
    private var mCallId: String? = null
    private var mCallStart: Long = 0
    private var mAddedListener = false
    private var mVideoViewsAdded = false
    private var mCallDuration: TextView? = null
    private var mCallState: TextView? = null
    private var mCallerName: TextView? = null
    private var top:LinearLayout?=null
    private var bottom:RelativeLayout?=null
    lateinit var localView:RelativeLayout
    lateinit var remoteview:RelativeLayout
    lateinit var mute:CircleImageView
    lateinit var offvdo:CircleImageView
    lateinit var flip:CircleImageView
    lateinit var openfull:CircleImageView
    lateinit var localbox:RelativeLayout
    lateinit var mCallerimg:CircleImageView
    var local:Boolean=false
    var paused:Boolean=false
    var muted:Boolean=false



    private inner class UpdateCallDurationTask : TimerTask() {
        override fun run() {
            runOnUiThread { updateCallDuration() }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putLong(CALL_START_TIME, mCallStart)
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        mCallStart = savedInstanceState.getLong(CALL_START_TIME)
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ayush.flow.R.layout.activity_outgoing_vdo)
        mAudioPlayer = AudioPlayer(this)
        mCallDuration = findViewById<View>(com.ayush.flow.R.id.callDuration) as TextView
        mCallerName = findViewById<View>(com.ayush.flow.R.id.remoteUser) as TextView
        mCallState = findViewById<View>(com.ayush.flow.R.id.callState) as TextView
        localView = findViewById<View>(com.ayush.flow.R.id.localVideo) as RelativeLayout
        remoteview = findViewById<View>(com.ayush.flow.R.id.remoteVideo) as RelativeLayout
        mCallerimg=findViewById(R.id.user_image)

        top=findViewById(R.id.top_box) as LinearLayout
        bottom=findViewById(R.id.bottomPanel) as RelativeLayout

        localbox=findViewById(R.id.local_box)

        mute=findViewById(R.id.mute_vdo)
        offvdo=findViewById(R.id.off_vdo)
        flip=findViewById(R.id.flip_vdo)
        openfull=findViewById(R.id.open_full)

        val endCallButton: CircleImageView = findViewById<View>(com.ayush.flow.R.id.hangupButton) as CircleImageView
        endCallButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                endCall()
            }
        })
        mCallId = intent.getStringExtra(SinchService.CALL_ID)
        if (savedInstanceState == null) {
            mCallStart = System.currentTimeMillis()
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public override fun onServiceConnected() {
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {

            call.answer()

            if (!mAddedListener) {
                call.addCallListener(SinchCallListener())
                mAddedListener = true



                val vc=sinchServiceInterface!!.getVideoController()
                vc!!.setLocalVideoResizeBehaviour(VideoScalingType.ASPECT_FILL)
                vc!!.setLocalVideoZOrder(true)


                call.resumeVideo()
                offvdo.setOnClickListener {
                    if(paused){
                        call.resumeVideo()
                        paused=false
                    }
                    else{
                        call.pauseVideo()
                        paused=true
                    }
                }


            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.")
            finish()
        }
        updateUI()
    }

    //method to update video feeds in the UI
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun updateUI() {
        if (sinchServiceInterface == null) {
            return  // early
        }
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {
            mCallerName!!.text=intent.getStringExtra("name")
            mCallState!!.setText(call.getState().toString())

//            if(intent.getStringExtra("image")!=""){
//                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),intent.getStringExtra("image")!!)
//                val b = BitmapFactory.decodeStream(FileInputStream(f))
//                mCallerimg.setImageBitmap(b)
//            }

            if (call.getState() === CallState.ESTABLISHED) {
                //when the call is established, addVideoViews configures the video to  be shown
                addVideoViews()


            }
        }
    }

    //stop the timer when call is ended
    public override fun onStop() {
        super.onStop()
        mDurationTask!!.cancel()
        mTimer!!.cancel()
        removeVideoViews()
    }

    //start the timer for the call duration here
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public override fun onStart() {
        super.onStart()
        mTimer = Timer()
        mDurationTask = UpdateCallDurationTask()
        mTimer!!.schedule(mDurationTask, 0, 500)
        updateUI()
    }

    override fun onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    //method to end the call
    private fun endCall() {
        mAudioPlayer!!.stopProgressTone()
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {
            call.hangup()
        }
        finish()
    }

    private fun formatTimespan(timespan: Long): String {
        val totalSeconds = timespan / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return java.lang.String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    //method to update live duration of the call
    private fun updateCallDuration() {
        if (mCallStart > 0) {
            mCallDuration!!.text = formatTimespan(System.currentTimeMillis() - mCallStart)
        }
    }

    //method which sets up the video feeds from the server to the UI of the activity
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun addVideoViews() {
        if (mVideoViewsAdded || sinchServiceInterface == null) {
            return  //early
        }


//                val handler = Handler()
//                var runnable:Runnable?=null

//                handler.postDelayed(Runnable {
//                    handler.postDelayed(runnable!!, 5000)
//                    if(bottom!!.visibility==View.VISIBLE){
//                        bottom!!.visibility=View.GONE
//
//                        val params:RelativeLayout.LayoutParams= localbox.layoutParams as RelativeLayout.LayoutParams
//                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
//                        params.setMargins(10,10,10,10)
//                        localbox.layoutParams=params
//                    }
//                }.also { runnable = it }, 5000)

        top!!.visibility=View.GONE
        mCallDuration!!.visibility=View.VISIBLE



        remoteview.setOnClickListener {
            if(bottom!!.visibility==View.VISIBLE){
                bottom!!.visibility=View.GONE

                val params:RelativeLayout.LayoutParams= localbox.layoutParams as RelativeLayout.LayoutParams
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                params.setMargins(10,10,10,10)
                localbox.layoutParams=params
            }
            else{
                bottom!!.visibility=View.VISIBLE

                val params:RelativeLayout.LayoutParams= localbox.layoutParams as RelativeLayout.LayoutParams
                params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                localbox.layoutParams=params
            }
        }



        mute.setOnClickListener {
            if(muted){
                sinchServiceInterface!!.unmuteCall()
                muted=false
            }
            else{
                sinchServiceInterface!!.muteCall()
                muted=true
            }
        }



        val vc = sinchServiceInterface!!.getVideoController()
        if (vc != null) {

            vc.setLocalVideoResizeBehaviour(VideoScalingType.ASPECT_FILL)
            val lview = vc.localView
            val rview=vc.remoteView
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            removeVideoViews()

            remoteview.addView(rview)
            localView.addView(lview)
            local=true

            mVideoViewsAdded = true


            localbox.setOnClickListener {

                openfull.visibility=View.VISIBLE

                Handler().postDelayed({
                    openfull.visibility=View.GONE
                },4000)

                openfull.setOnClickListener {


                    if(local==true){
                        removeVideoViews()
                        vc.setLocalVideoZOrder(false)
                        remoteview.addView(lview)
                        localView.addView(rview)
                        local=false
                    }
                    else{
                        removeVideoViews()
                        vc.setLocalVideoZOrder(true)
                        remoteview.addView(rview)
                        localView.addView(lview)
                        local=true
                    }
                }
            }


            flip.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    //this toggles the front camera to rear camera and vice versa
                    vc.toggleCaptureDevicePosition()
                }
            })
        }
    }

    //removes video feeds from the app once the call is terminated
    private fun removeVideoViews() {
        if (sinchServiceInterface == null) {
            return  // early
        }
        val vc = sinchServiceInterface!!.getVideoController()
        if (vc != null) {
            val view = findViewById<View>(com.ayush.flow.R.id.remoteVideo) as RelativeLayout

            val localView = findViewById<View>(com.ayush.flow.R.id.localVideo) as RelativeLayout

            if(!local){
                view.removeView(vc.localView)
                localView.removeView(vc.remoteView)
            }
            else{
                view.removeView(vc.remoteView)
                localView.removeView(vc.localView)
            }

            mVideoViewsAdded = false
        }
    }

    inner class SinchCallListener : VideoCallListener {
        override fun onCallEnded(call: Call) {
            val cause: CallEndCause = call.getDetails().getEndCause()
            Log.d(TAG, "Call ended. Reason: $cause")
            mAudioPlayer!!.stopProgressTone()
            volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
            val endMsg = "Call ended: " + call.getDetails().toString()
            Toast.makeText(this@Outgoing_vdo, endMsg, Toast.LENGTH_LONG).show()
            endCall()
        }

        override fun onCallEstablished(call: Call) {
            Log.d(TAG, "Call established")
            mAudioPlayer!!.stopProgressTone()
            mCallState!!.setText(call.getState().toString())
            volumeControlStream = AudioManager.STREAM_VOICE_CALL
           sinchServiceInterface!!.onSpeaker()
            sinchServiceInterface!!.unmuteCall()
        //    audioController.enableSpeaker()
            mCallStart = System.currentTimeMillis()
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered())
        }

        override fun onCallProgressing(call: Call?) {
            Log.d(TAG, "Call progressing")
            remoteview.addView(sinchServiceInterface!!.getVideoController()!!.localView)
            local=false
            mAudioPlayer!!.playProgressTone()
        }

        override fun onShouldSendPushNotification(call: Call?, pushPairs: List<PushPair?>?) {
            // Send a push through your push provider here, e.g. GCM
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        override fun onVideoTrackAdded(call: Call?) {
            Log.d(TAG, "Video track added")
            addVideoViews()
        }

        override fun onVideoTrackPaused(p0: Call?) {
           // p0!!.pauseVideo()
        }

        override fun onVideoTrackResumed(p0: Call?) {
          //  p0!!.resumeVideo()
        }
    }

    companion object {
        val TAG = Outgoing_vdo::class.java.simpleName
        const val CALL_START_TIME = "callStartTime"
        const val ADDED_LISTENER = "addedListener"
    }
}
