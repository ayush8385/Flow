package com.ayush.flow.database

import androidx.lifecycle.LiveData

class MessageRepository(private val messageDao: MessageDao) {

    suspend fun insert(messageEntity: MessageEntity){
       messageDao.insertMessage(messageEntity)
    }

    fun allMessages(userid: String): LiveData<List<MessageEntity>> {
        return messageDao.getAllMessages(userid)
    }

    fun isMsgExist(userid: String): Boolean {
        return messageDao.isMsgExist(userid)
    }
//    suspend fun delete(noteEntity: NoteEntity){
//        noteDao.delete(noteEntity)
//    }
}