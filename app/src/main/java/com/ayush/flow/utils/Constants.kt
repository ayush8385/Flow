package com.ayush.flow.utils

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
        const val OPEN_CHAT_HOME = 0
        const val OPEN_HIDE_HOME = 1
        const val MAX_API_FOR_PERMISSION = 18
        const val CAMERA_PERMISSION = "camera"
        const val STORAGE_PERMISSION = "storage"
        const val CONTACT_PERMISSION = "contact"
        const val MIC_PERMISSION = "mic"
        const val MIC_CAM_PERMISSION = "mic_cam"
        const val CAMERA_REQUEST_CODE = 110
        const val STORAGE_REQUEST_CODE = 112
        const val DOCUMENT_REQUEST_CODE:Int = 113
        const val PERMISSION_CAMERA_REQUEST_CODE = 101
        const val PERMISSION_STORAGE_REQUEST_CODE = 102
        const val PERMISSION_CONTACT_REQUEST_CODE = 103
        const val PERMISSION_MIC_REQUEST_CODE = 104
        const val PERMISSION_MIC_CAM_REQUEST_CODE = 105
        var isCurrentUser = false

        const val PAINT_STYLE_STROKE = 1

        const val PAINT_STYLE_FILL = 2

        const val OPERATION_NO_OPERATION = -1

        const val OPERATION_DRAW_PENCIL = 0

        const val OPERATION_ERASE = 1

        const val OPERATION_CHOOSE_COLOR = 2

        const val OPERATION_CLEAR_CANVAS = 3

        const val OPERATION_INSERT_TEXT = 4

        const val OPERATION_UNDO = 5

        const val OPERATION_SET_BACKGROUND = 9

        const val OPERATION_SAVE_IMAGE = 10

        const val OPERATION_MOVE_VIEW = 20

        const val OPERATION_FILL_VIEW = 21

        const val RESULT_LOAD_IMAGE = 100

    }
}