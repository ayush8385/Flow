package com.ayush.flow.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey var mid:String,
    @ColumnInfo(name = "user_id") val userid : String,
    @ColumnInfo(name = "sender_id") val sender: String,
    @ColumnInfo(name = "message") val message : String,
    @ColumnInfo(name = "msg_type") val type: String
)