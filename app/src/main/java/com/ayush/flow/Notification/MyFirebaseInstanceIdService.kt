package com.ayush.flow.Notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceIdService:FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)


        val  firebaseUser=FirebaseAuth.getInstance().currentUser
        val refreshToken=FirebaseInstanceId.getInstance().token

        if(firebaseUser!=null){
            updateToken(refreshToken)
        }
    }

    private fun updateToken(refreshToken: String?) {
        val  firebaseUser=FirebaseAuth.getInstance().currentUser

        val ref=FirebaseDatabase.getInstance().reference.child("Token")
        val token= Token(refreshToken!!)
        ref.child(firebaseUser!!.uid).setValue(token)

    }

}