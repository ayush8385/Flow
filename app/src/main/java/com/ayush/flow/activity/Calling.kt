package com.ayush.flow.activity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
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
    var mAction=""
    var sensorManager: SensorManager? = null
    var proximitySensor: Sensor? = null
    protected var proximityWakelock: PowerManager.WakeLock? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ayush.flow.R.layout.activity_calling)
        val answer = findViewById(com.ayush.flow.R.id.pick_btn) as CircleImageView
        remoteUser = findViewById(com.ayush.flow.R.id.user_name)
        answer.setOnClickListener(mClickListener)
        val decline = findViewById(com.ayush.flow.R.id.end_btn) as CircleImageView
        decline.setOnClickListener(mClickListener)

        window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer!!.playRingtone()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)


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
                    params.screenBrightness = -1F
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

    override fun onResume() {
        super.onResume()
        val intent=intent
        if(intent!=null){
            if(intent.getSerializableExtra(SinchService.CALL_ID)!=null){
                mCallId = getIntent().getStringExtra(SinchService.CALL_ID)
            }
            val notificationManager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)
            if(intent.action!=null){
                mAction= intent.action!!
            }
        }

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

            if ("answer".equals(mAction)) {
                mAction = "";
                answerClicked();
            } else if ("ignore".equals(mAction)) {
                mAction = "";
                declineClicked();
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
            com.ayush.flow.R.id.pick_btn -> answerClicked()
            com.ayush.flow.R.id.end_btn -> declineClicked()
        }
    }

    companion object {
        val TAG = Calling::class.java.simpleName
    }
}