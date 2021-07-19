package com.ayush.flow.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calls")
data class CallEntity(
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "image") val image : String,
    @ColumnInfo(name = "time") val time:String,
    @PrimaryKey var id:String
    )
