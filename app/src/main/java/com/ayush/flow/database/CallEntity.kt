package com.ayush.flow.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calls")
data class CallEntity(
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "image") val image : String,
    @ColumnInfo(name = "calltype") val calltype :String,
    @ColumnInfo(name = "duration") val duration:String,
    @PrimaryKey var id:String
    )
