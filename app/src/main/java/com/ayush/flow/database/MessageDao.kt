package com.ayush.flow.database

import androidx.lifecycle.LiveData
import androidx.room.*

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

    @Delete
    fun delete(item: MessageEntity)

    @Query("DELETE FROM messages WHERE user_id = :id")
    fun deleteChat(id: String)

    @Query("UPDATE messages SET msg_status = :status WHERE mid = :mid")
    fun updateMsgStatus(status: String,mid: String)

    @Query("SELECT COUNT(*) FROM messages WHERE sender_id = :id AND msg_status != 'seen'")
    fun getUnreads(id: String): LiveData<Int>

    @Query("SELECT msg_status FROM messages WHERE mid = :mid")
    fun getMsgStatus(mid: String):LiveData<String>

    @Query("UPDATE messages SET msg_status = 'seen' WHERE sender_id = :userid")
    fun setMsgSeen(userid: String)

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}