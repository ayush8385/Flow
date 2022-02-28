package com.ayush.flow.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @ColumnInfo(name = "contact_name") val name : String,
    @PrimaryKey val number: String,
    @ColumnInfo(name = "contact_image") val image: String,
    @ColumnInfo(name = "isUser") val isUser:Boolean,
    @ColumnInfo(name = "contact_id") var id:String
    )
