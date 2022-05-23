package com.ayush.flow.database

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey var mid:String,
    @ColumnInfo(name = "user_id") var userid: String,
    @ColumnInfo(name = "sender_id") var sender: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "time") val time:Long=0,
    @ColumnInfo(name = "msg_type") val type: String,
    @ColumnInfo(name = "msg_path") var path:String,
    @ColumnInfo(name = "doc_path") var docpath: String,
    @ColumnInfo(name = "url") val url:String,
    @ColumnInfo(name = "recev") var recev:Boolean,
    @ColumnInfo(name = "seen") var seen:Boolean,
    @ColumnInfo(name = "sent") var sent:Boolean
)
