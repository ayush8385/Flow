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
    @ColumnInfo(name = "last_sender") val last_sender:String="",
    @ColumnInfo(name = "last_mid") var last_mid:String="",
    @ColumnInfo(name = "path") var path:String="",
    @ColumnInfo(name = "time") var time:Long=0,
    @ColumnInfo(name = "hide") var hide:Boolean=false,
    @ColumnInfo(name = "unread") var unread:Int=0,
    @PrimaryKey var id:String
    ){
    constructor():this("","","","","","","",0,false,0,"")
}
