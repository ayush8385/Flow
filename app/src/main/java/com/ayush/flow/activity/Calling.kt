package com.ayush.flow.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.sinch.android.rtc.calling.CallListener
import de.hdodenhof.circleimageview.CircleImageView

class Calling : BaseActivity(){
    private var mCallId: String? = null
    private var mAudioPlayer: AudioPlayer? = null
    lateinit var remoteUser:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)
        val answer = findViewById(R.id.pick_btn) as CircleImageView
        remoteUser = findViewById(R.id.user_name)
        answer.setOnClickListener(mClickListener)
        val decline = findViewById(R.id.end_btn) as CircleImageView
        decline.setOnClickListener(mClickListener)
        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer!!.playRingtone()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)
    }

    override fun onServiceConnected() {
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)
        if (call != null) {
            call.addCallListener(SinchCallListener())


            val cons=ContactViewModel(application).getContact(call.remoteUserId)
            if(cons == null){
                val ref=FirebaseDatabase.getInstance().reference.child("Users")
                ref.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        remoteUser.setText(snapshot.child(call.remoteUserId).child("number").value.toString())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
            else{
                remoteUser.setText(cons.name)
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting")
            finish()
        }
    }

    private fun answerClicked() {
        mAudioPlayer!!.stopRingtone()
        val call: Call = getSinchServiceInterface()!!.getCall(mCallId)
        if (call != null) {
            call.answer()
            val intent = Intent(this, Outgoing::class.java)
            intent.putExtra(SinchService.CALL_ID, mCallId)
            intent.putExtra("name",remoteUser.text)
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
            R.id.pick_btn -> answerClicked()
            R.id.end_btn -> declineClicked()
        }
    }

    companion object {
        val TAG = Calling::class.java.simpleName
    }
}