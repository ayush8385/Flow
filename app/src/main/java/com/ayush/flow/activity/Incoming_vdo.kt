package com.ayush.flow.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.ayush.flow.R
import com.ayush.flow.database.ContactViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.video.VideoCallListener
import com.sinch.android.rtc.video.VideoScalingType
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream


class Incoming_vdo : BaseActivity() {
    private var mCallId: String? = null
    private var mAudioPlayer: AudioPlayer? = null
    lateinit var localView:RelativeLayout
    lateinit var mCallerimg:CircleImageView
    lateinit var pickintent:Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ayush.flow.R.layout.activity_incoming_vdo)
        val answer: CircleImageView = findViewById<View>(com.ayush.flow.R.id.answerButton) as CircleImageView
        answer.setOnClickListener(mClickListener)
        val decline: CircleImageView = findViewById<View>(com.ayush.flow.R.id.declineButton) as CircleImageView
        decline.setOnClickListener(mClickListener)

        localView=findViewById(R.id.localVideo)
        mCallerimg=findViewById(R.id.user_image)

        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer!!.playRingtone()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)

        pickintent = Intent(this, Outgoing_vdo::class.java)
    }

    override fun onServiceConnected() {
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {

            val vc = sinchServiceInterface!!.getVideoController()
            vc!!.setLocalVideoResizeBehaviour(VideoScalingType.ASPECT_FILL)
            localView.addView(vc.localView)

            call.addCallListener(SinchCallListener())
            val remoteUser = findViewById<View>(com.ayush.flow.R.id.remoteUser) as TextView

            val cons= ContactViewModel(application).getContact(call.remoteUserId)

            if(cons == null){
                val ref= FirebaseDatabase.getInstance().reference.child("Users")
                ref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        remoteUser.setText(snapshot.child(call.remoteUserId).child("number").value.toString())

                        val url=snapshot.child("profile_photo").value.toString()

                        if(url!=""){
                            Picasso.get().load(url).into(mCallerimg)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
            else{
                remoteUser.text = cons.name

                pickintent.putExtra(SinchService.CALL_ID, mCallId)
                pickintent.putExtra("name",remoteUser.text)

                if(cons.image!=""){
                    val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),cons.image)
                    val b = BitmapFactory.decodeStream(FileInputStream(f))
                    mCallerimg.setImageBitmap(b)

                    pickintent.putExtra("image",cons.image)
                }
            }
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

            startActivity(pickintent)
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