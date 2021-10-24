package com.ayush.flow.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @ColumnInfo(name = "name") var name : String="",
    @ColumnInfo(name = "number") var number : String="",
    @ColumnInfo(name = "image") var image : String="",
    @ColumnInfo(name = "last_msg") var lst_msg:String="",
    @ColumnInfo(name = "time") var time:String="",
    @ColumnInfo(name = "hide") var hide:Boolean=false,
    @PrimaryKey var id:String
    ){
    constructor():this("","","","","",false,"")
}
