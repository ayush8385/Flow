package com.ayush.flow.model

import android.os.Parcel
import android.os.Parcelable

data class Contacts(
    val name:String,
    val number:String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!

    ) {
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(number)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Contacts> {
        override fun createFromParcel(parcel: Parcel): Contacts {
            return Contacts(parcel)
        }

        override fun newArray(size: Int): Array<Contacts?> {
            return arrayOfNulls(size)
        }
    }

}
