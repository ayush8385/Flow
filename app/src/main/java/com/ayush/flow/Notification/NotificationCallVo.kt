package com.ayush.flow.Notification

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator


class NotificationCallVo : Parcelable {
    private var data: MutableMap<String,String>? = null

    constructor() {}
    private constructor(`in`: Parcel) {
        data = `in`.readValue(HashMap::class.java.classLoader) as MutableMap<String, String>?
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(data)
    }

    fun getData(): MutableMap<String,String> {
        return data!!
    }

    fun setData(data: MutableMap<String, String>) {
        this.data = data
    }

    companion object {
        @JvmField val CREATOR: Creator<NotificationCallVo?> = object : Creator<NotificationCallVo?> {
            override fun createFromParcel(`in`: Parcel): NotificationCallVo? {
                return NotificationCallVo(`in`)
            }

            override fun newArray(size: Int): Array<NotificationCallVo?> {
                return arrayOfNulls(size)
            }
        }
    }
}