package com.ayush.flow.flow.model

import android.os.Parcel
import android.os.Parcelable


data class Message(
    val id:Int,
    val message:String,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt()!!,
        parcel.readString()!!

    ) {
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }

}
