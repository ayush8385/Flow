package com.ayush.flow.activity

import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
    lateinit var mute:CircleImageView
    var apiService: APIService?=null
    lateinit var speeaker:CircleImageView

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
        apiService= Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)
        val endCallButton = findViewById<View>(R.id.end_btn) as CircleImageView
        endCallButton.setOnClickListener { endCall() }
        mute=findViewById(R.id.mic)
        mute.setOnClickListener {
            if(mute.background.equals(R.drawable.story_unread_back)){
                sinchServiceInterface!!.muteCall()
                mute.setBackgroundColor(R.drawable.bottom_back)
            }
            else{
                sinchServiceInterface!!.unmuteCall()
                mute.setBackgroundColor(R.drawable.story_unread_back)
            }
        }
        speeaker=findViewById(R.id.speaker)
        speeaker.setOnClickListener {
            
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

//    fun sendNotification(
//        mid: String,
//        senderid: String,
//        recieverid: String,
//        username: String,
//        msg: String,
//        type:Int
//    ) {
//
//        sinchServiceInterface!!.startClient(FirebaseAuth.getInstance().currentUser!!.uid)
//        AudioPlayer(this).playRingtone()
//
//        Log.d("Here you","reached....")
//        val ref= FirebaseDatabase.getInstance().reference.child("Token")
//        val query=ref.orderByKey().equalTo(recieverid)
//
//        var nf_url:String?=null
//        val refer= FirebaseDatabase.getInstance().reference.child("Users")
//        refer.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (snapshot in snapshot.children) {
//                    val id = snapshot.child("uid").value.toString()
//                    if (id.equals(recieverid)) {
//                        nf_url = snapshot.child("profile_photo").value.toString()
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//
//        query.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (snapshoshot in snapshot.children) {
//                    val token: Token? = snapshoshot.getValue(Token::class.java)
//
//                    val data = Data(
//                        mid,
//                        senderid,
//                        R.drawable.flow,
//                        nf_url!!,
//                        msg,
//                        username,
//                        recieverid,
//                        type
//                    )
//
//                    val sender = Sender(data, token!!.getToken().toString())
//
//                    apiService!!.sendNotification(sender)
//                        .enqueue(object : retrofit2.Callback<MyResponse> {
//                            override fun onResponse(
//                                call: retrofit2.Call<MyResponse>,
//                                response: Response<MyResponse>
//                            ) {
//                                if (response.code() == 200) {
//                                    if (response.body()!!.success != 1) {
//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Hey you",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//                                    }
//                                }
//                            }
//
//                            override fun onFailure(call: retrofit2.Call<MyResponse>, t: Throwable) {
//                                TODO("Not yet implemented")
//                            }
//
//                        })
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//    }
}