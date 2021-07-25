package com.ayush.flow.database

import androidx.lifecycle.LiveData

class ChatRepository(private val chatDao: ChatDao) {
    val allChats:LiveData<List<ChatEntity>> = chatDao.getAllChats()

    suspend fun insert(chatEntity: ChatEntity){
        chatDao.insertChat(chatEntity)
    }

    suspend fun delete(userid: String){
        chatDao.deleteChat(userid)
    }

    fun isUserExist(userid: String): Boolean {
        return chatDao.isUserExist(userid)
    }

    fun getChatbyId(userid: String): ChatEntity {
        return chatDao.getChatbyId(userid)
    }

    fun update(mid: String, img: String, name: String) {
        chatDao.update(mid,img,name)
    }
//    suspend fun delete(noteEntity: NoteEntity){
//        noteDao.delete(noteEntity)
//    }
}