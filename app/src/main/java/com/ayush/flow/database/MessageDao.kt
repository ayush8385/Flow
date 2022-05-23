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

    @Query("UPDATE messages SET recev = :rec, seen = :seen WHERE mid = :mid")
    fun update(mid: String, rec: Boolean, seen: Boolean)

    @Delete
    fun delete(item: MessageEntity)

    @Query("DELETE FROM messages WHERE user_id = :id")
    fun deleteChat(id: String)

    @Query("UPDATE messages SET sent = :sent WHERE mid = :mid")
    fun isSent(sent: Boolean,mid: String)

    @Query("SELECT COUNT(seen) FROM messages WHERE sender_id = :id AND seen = 0")
    fun getUnreads(id: String): LiveData<Int>

    @Query("UPDATE messages SET seen = :seen WHERE mid = :mid")
    fun isSeen(seen: Boolean, mid: String)

    @Query("UPDATE messages SET seen = 1 WHERE user_id = :userid")
    fun setMsgSeen(userid: String)

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}