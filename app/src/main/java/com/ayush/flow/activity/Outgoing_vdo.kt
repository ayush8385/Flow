package com.ayush.flow.activity

import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.calling.CallState
import com.sinch.android.rtc.video.VideoCallListener
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

    public override fun onServiceConnected() {
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(SinchCallListener())
                mAddedListener = true
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.")
            finish()
        }
        updateUI()
    }

    //method to update video feeds in the UI
    private fun updateUI() {
        if (sinchServiceInterface == null) {
            return  // early
        }
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {
            mCallerName!!.setText(call.getRemoteUserId())
            mCallState!!.setText(call.getState().toString())
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
    private fun addVideoViews() {
        if (mVideoViewsAdded || sinchServiceInterface == null) {
            return  //early
        }
        val vc = sinchServiceInterface!!.getVideoController()
        if (vc != null) {
            val localView = findViewById<View>(com.ayush.flow.R.id.localVideo) as RelativeLayout
            val remoteview = findViewById<View>(com.ayush.flow.R.id.remoteVideo) as RelativeLayout

            val lview = vc.localView
            val rview=vc.remoteView


        //    rview.layoutParams= LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,LinearLayoutCompat.LayoutParams.MATCH_PARENT)
            localView.addView(lview)


            remoteview.addView(rview)
            mVideoViewsAdded = true


            localView.setOnClickListener(object : View.OnClickListener {
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
            view.removeView(vc.remoteView)
            val localView = findViewById<View>(com.ayush.flow.R.id.localVideo) as RelativeLayout
            localView.removeView(vc.localView)
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
        //    audioController.enableSpeaker()
            mCallStart = System.currentTimeMillis()
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered())
        }

        override fun onCallProgressing(call: Call?) {
            Log.d(TAG, "Call progressing")
            mAudioPlayer!!.playProgressTone()
        }

        override fun onShouldSendPushNotification(call: Call?, pushPairs: List<PushPair?>?) {
            // Send a push through your push provider here, e.g. GCM
        }

        override fun onVideoTrackAdded(call: Call?) {
            Log.d(TAG, "Video track added")
            addVideoViews()
        }

        override fun onVideoTrackPaused(p0: Call?) {
            TODO("Not yet implemented")
        }

        override fun onVideoTrackResumed(p0: Call?) {
            TODO("Not yet implemented")
        }
    }

    companion object {
        val TAG = Outgoing_vdo::class.java.simpleName
        const val CALL_START_TIME = "callStartTime"
        const val ADDED_LISTENER = "addedListener"
    }
}
