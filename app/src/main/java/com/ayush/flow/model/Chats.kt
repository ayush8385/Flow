package com.ayush.flow.model

import android.os.Parcel
import android.os.Parcelable


data class Chats(
        val senderId:String,
        val receiverId:String,
        val message:String,
        val image_url:String,
        val  messageId:String,
        val  time:String,
        val image:String,
        val name:String
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!

    ) {
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(senderId)
        parcel.writeString(receiverId)
        parcel.writeString(message)
        parcel.writeString(image_url)
        parcel.writeString(messageId)
        parcel.writeString(time)
        parcel.writeString(image)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chats> {
        override fun createFromParcel(parcel: Parcel): Chats {
            return Chats(parcel)
        }

        override fun newArray(size: Int): Array<Chats?> {
            return arrayOfNulls(size)
        }
    }

}
