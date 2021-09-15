package com.ayush.flow.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chatEntity: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :id")
    fun deleteChat(id: String)

    @Query("SELECT * FROM chats")
    fun getAllChats():LiveData<List<ChatEntity>>

    @Query("SELECT EXISTS(SELECT * FROM chats WHERE id = :id)")
    fun isUserExist(id : String) : Boolean

    @Query("SELECT * FROM chats WHERE id = :userid")
    fun getChatbyId(userid: String): ChatEntity

    @Query("UPDATE chats SET image = :img WHERE id = :userid")
    fun update(userid: String, img: String)

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}