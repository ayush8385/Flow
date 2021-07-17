package com.ayush.flow.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @ColumnInfo(name = "contact_name") val name : String,
    @ColumnInfo(name = "contact_number") val number: String,
    @ColumnInfo(name = "contact_image") val image: String,
    @PrimaryKey var id:String
    )
