package com.ayush.flow.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(messageEntity: MessageEntity)

//    @Delete
//    fun deleteRestaurant(contactEntity: ContactEntity)

    @Query("SELECT * FROM messages WHERE user_id is :userid")
    fun getAllMessages(userid:String): LiveData<List<MessageEntity>>

    @Query("SELECT EXISTS(SELECT * FROM messages WHERE mid = :id)")
    fun isMsgExist(id : String) : Boolean

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}