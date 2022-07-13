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

    fun deleteMsg(item: MessageEntity) {
        messageDao.delete(item)
    }

    fun deleteMsgWithId(id:String){
        messageDao.deleteChat(id)
    }

    fun updateMsgStatus(status: String,mid: String) {
        messageDao.updateMsgStatus(status,mid)
    }

    fun getUnreads(id:String):LiveData<Int>{
        return messageDao.getUnreads(id)
    }

    fun getMsgStatus(mid: String):LiveData<String>{
        return messageDao.getMsgStatus(mid)
    }

    fun setMsgSeen(userid: String) {
        messageDao.setMsgSeen(userid)
    }
//    suspend fun delete(noteEntity: NoteEntity){
//        noteDao.delete(noteEntity)
//    }
}