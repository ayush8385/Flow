package com.ayush.flow.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.video.VideoCallListener


class Incoming_vdo : BaseActivity() {
    private var mCallId: String? = null
    private var mAudioPlayer: AudioPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ayush.flow.R.layout.activity_incoming_vdo)
        val answer: Button = findViewById<View>(com.ayush.flow.R.id.answerButton) as Button
        answer.setOnClickListener(mClickListener)
        val decline: Button = findViewById<View>(com.ayush.flow.R.id.declineButton) as Button
        decline.setOnClickListener(mClickListener)
        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer!!.playRingtone()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)
    }

    override fun onServiceConnected() {
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {
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
            TODO("Not yet implemented")
        }

        override fun onVideoTrackResumed(p0: Call?) {
            TODO("Not yet implemented")
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