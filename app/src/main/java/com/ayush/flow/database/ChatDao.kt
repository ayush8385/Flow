package com.ayush.flow.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChat(chatEntity: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :id")
    fun deleteChat(id: String)

    @Query("SELECT * FROM chats")
    fun getAllChats():LiveData<List<ChatEntity>>

    @Query("SELECT EXISTS(SELECT * FROM chats WHERE id = :id)")
    fun isUserExist(id : String) : Boolean

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}