package com.ayush.flow.Services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener

class Constants {
    companion object{
        var firebaseListener: ChildEventListener?=null
        val MY_USERID= FirebaseAuth.getInstance().currentUser!!.uid
        const val PROFILE_PHOTO_LOCATION = "/Flow/Medias/Profile photos"
        const val ALL_PHOTO_LOCATION = "/Flow/Medias/Images"
        const val DOC_LOCATION = "/Flow/Medias/Documents"
        const val APP_SHARED_PREFERENCE: String = "flow_shared_prefs"
        var isCurrentUser = false

    }
}