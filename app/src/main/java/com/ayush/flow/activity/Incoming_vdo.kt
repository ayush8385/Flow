package com.ayush.flow.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.ayush.flow.R
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.video.VideoCallListener
import com.sinch.android.rtc.video.VideoScalingType
import de.hdodenhof.circleimageview.CircleImageView


class Incoming_vdo : BaseActivity() {
    private var mCallId: String? = null
    private var mAudioPlayer: AudioPlayer? = null
    lateinit var localView:RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ayush.flow.R.layout.activity_incoming_vdo)
        val answer: CircleImageView = findViewById<View>(com.ayush.flow.R.id.answerButton) as CircleImageView
        answer.setOnClickListener(mClickListener)
        val decline: CircleImageView = findViewById<View>(com.ayush.flow.R.id.declineButton) as CircleImageView
        decline.setOnClickListener(mClickListener)

        localView=findViewById(R.id.localVideo)

        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer!!.playRingtone()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)
    }

    override fun onServiceConnected() {
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {

            val vc = sinchServiceInterface!!.getVideoController()
            vc!!.setLocalVideoResizeBehaviour(VideoScalingType.ASPECT_FILL)
            localView.addView(vc.localView)

            call.addCallListener(SinchCallListener())
            val remoteUser = findViewById<View>(com.ayush.flow.R.id.remoteUser) as TextView
            remoteUser.setText(call.getRemoteUserId())
        } else {
            Log.e(TAG, "Started with invalid callId, aborting")
            finish()
        }
    }

    private fun answerClicked() {
        mAudioPlayer!!.stopRingtone()
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {
            call.answer()
            val intent = Intent(this, Outgoing_vdo::class.java)
            intent.putExtra(SinchService.CALL_ID, mCallId)
            startActivity(intent)
            finish()
            localView.removeView(sinchServiceInterface!!.getVideoController()!!.localView)
        } else {
            finish()
        }
    }

    private fun declineClicked() {
        mAudioPlayer!!.stopRingtone()
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {
            call.hangup()
        }
        finish()
    }

    inner class SinchCallListener : VideoCallListener {
        override fun onCallEnded(call: Call) {
            val cause: CallEndCause = call.getDetails().getEndCause()
            Log.d(TAG, "Call ended, cause: $cause")
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

        override fun onVideoTrackAdded(call: Call?) {
            // Display some kind of icon showing it's a video call
        }

        override fun onVideoTrackPaused(p0: Call?) {
            p0!!.pauseVideo()
        }

        override fun onVideoTrackResumed(p0: Call?) {
            p0!!.resumeVideo()
        }
    }

    private val mClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.getId()) {
                com.ayush.flow.R.id.answerButton -> answerClicked()
                com.ayush.flow.R.id.declineButton -> declineClicked()
            }
        }
    }

    companion object {
        val TAG = Incoming_vdo::class.java.simpleName
    }
}