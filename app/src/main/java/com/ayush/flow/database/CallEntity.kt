package com.ayush.flow.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calls")
data class CallEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type:String,
    @ColumnInfo(name = "status") val calltype:String,
    @ColumnInfo(name = "time") val time: Long?,
    @ColumnInfo(name = "duration") val duration:Int,
    @PrimaryKey var id:String
    )
