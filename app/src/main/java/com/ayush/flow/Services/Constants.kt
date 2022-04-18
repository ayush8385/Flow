package com.ayush.flow.Services

import com.google.firebase.auth.FirebaseAuth

class Constants {
    companion object{
        val MY_USERID= FirebaseAuth.getInstance().currentUser!!.uid
        const val PROFILE_PHOTO_LOCATION = "/Flow/Medias/Profile photos"
        const val ALL_PHOTO_LOCATION = "/Flow/Medias/Images"
        const val APP_SHARED_PREFERENCE: String = "flow_shared_prefs"

    }
}