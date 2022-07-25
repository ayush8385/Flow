package com.ayush.flow.activity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import com.ayush.flow.R
import com.ayush.flow.Services.Constants
import com.ayush.flow.Services.Permissions
import com.ayush.flow.database.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.video.VideoCallListener
import com.sinch.android.rtc.video.VideoScalingType
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File


class Incoming_vdo : BaseActivity() {
    private var mCallId: String? = null
    private var mAudioPlayer: AudioPlayer? = null
    lateinit var localView:RelativeLayout
    lateinit var mCallerimg:CircleImageView
    lateinit var pickintent:Intent
    lateinit var remoteUser:TextView
    var mAction=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ayush.flow.R.layout.activity_incoming_vdo)
        val answer: CircleImageView = findViewById<View>(com.ayush.flow.R.id.answerButton) as CircleImageView
        remoteUser = findViewById(com.ayush.flow.R.id.remoteUser)
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

        val decline: CircleImageView = findViewById<View>(com.ayush.flow.R.id.declineButton) as CircleImageView
        decline.setOnClickListener(mClickListener)

        localView=findViewById(R.id.localVideo)
        mCallerimg=findViewById(R.id.user_image)

        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer!!.playRingtone()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)

        pickintent = Intent(this, Outgoing_vdo::class.java)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    override fun onResume() {
        super.onResume()
        val intent=intent
        if(intent!=null){
            if(intent.getSerializableExtra(SinchService.CALL_ID)!=null){
                mCallId = getIntent().getStringExtra(SinchService.CALL_ID)
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)
            if(intent.action!=null){
                mAction= intent.action!!
            }
        }
    }

    override fun onServiceConnected() {
        val call: Call = sinchServiceInterface!!.getCall(mCallId)
        if (call != null) {

            if ("ignore".equals(mAction)) {
                mAction = "";
                declineClicked()
            }

            val vc = sinchServiceInterface!!.getVideoController()
            vc!!.setLocalVideoResizeBehaviour(VideoScalingType.ASPECT_FILL)
            localView.addView(vc.localView)

            call.addCallListener(SinchCallListener())

            val cons= ContactViewModel(application).getContact(call.remoteUserId)

            CallViewModel(application).inserCall(CallEntity(remoteUser.text.toString(),"video","incoming",System.currentTimeMillis(),0,call.remoteUserId))
            CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(remoteUser.text.toString(),"video","incoming",System.currentTimeMillis(),0,call.remoteUserId,mCallId!!))

            if(cons == null){

                var chat = ChatViewModel(application).getChat(call.remoteUserId)
                if(chat==null){
                    val ref=FirebaseDatabase.getInstance().reference.child("Users")
                    ref.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            remoteUser.setText(snapshot.child(call.remoteUserId).child("number").value.toString())

                            val url=snapshot.child(call.remoteUserId).child("profile_photo").value.toString()


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

                    remoteUser.setText(chat.number)

                    pickintent.putExtra("name",remoteUser.text)

                    val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),call.remoteUserId+".jpg")

                    Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
                        DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(mCallerimg)

                    pickintent.putExtra("userid",call.remoteUserId)

                }
            }
            else{
                remoteUser.text = cons.name

                pickintent.putExtra("name",remoteUser.text)

                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),call.remoteUserId+".jpg")

                Glide.with(this).load(f).placeholder(R.drawable.user).into(mCallerimg)

                pickintent.putExtra("userid",call.remoteUserId)
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
    }

    private fun answerClicked() {
        if(Permissions().checkCamerapermission(this)){
            mAudioPlayer!!.stopRingtone()
            val call: Call = sinchServiceInterface!!.getCall(mCallId)
            if (call != null) {
                call.answer()
                localView.removeView(sinchServiceInterface!!.getVideoController()!!.localView)
                pickintent.putExtra(SinchService.CALL_ID, mCallId)
                startActivity(pickintent)
                finish()
            } else {
                finish()
            }
        }
        else{
            Permissions().requestCameraPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mAudioPlayer!!.stopRingtone()
                    val call: Call = sinchServiceInterface!!.getCall(mCallId)
                    if (call != null) {
                        call.answer()
                        localView.removeView(sinchServiceInterface!!.getVideoController()!!.localView)
                        pickintent.putExtra(SinchService.CALL_ID, mCallId)
                        startActivity(pickintent)
                        finish()
                    } else {
                        finish()
                    }
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
            val callDetail = call.details
            val cause = callDetail.endCause
            Log.d("Ended", "Call ended, cause: " + cause)

            var callStatus=CallHistoryViewModel(application).getCallStatus(mCallId)
            if(cause.toString()=="CANCELED"){
                callStatus="missed"
            }

            CallViewModel(application).inserCall(CallEntity(remoteUser.text.toString(),"video",callStatus,callDetail.startedTime.time,callDetail.duration,call.remoteUserId))
            CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(remoteUser.text.toString(),"video",callStatus,callDetail.startedTime.time,callDetail.duration,call.remoteUserId,mCallId!!))

            mAudioPlayer!!.stopRingtone()
            localView.removeView(sinchServiceInterface!!.getVideoController()!!.localView)
            finish()
        }

        override fun onCallEstablished(call: Call?) {
            Log.d(TAG, "Call established")
        }

        override fun onCallProgressing(call: Call?) {
            Log.d(TAG, "Call progressing")
        }

//        override fun onShouldSendPushNotification(call: Call?, pushPairs: List<PushPair?>?) {
//            // Send a push through your push provider here, e.g. GCM
//        }

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