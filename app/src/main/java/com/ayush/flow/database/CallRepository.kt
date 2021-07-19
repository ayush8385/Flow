package com.ayush.flow.database

import androidx.lifecycle.LiveData

class CallRepository(private val callDao: CallDao) {
    val allCalls:LiveData<List<CallEntity>> = callDao.getAllCalls()

    suspend fun insert(callEntity: CallEntity){
        callDao.insertCall(callEntity)
    }

    suspend fun delete(userid: String){
        callDao.deleteCall(userid)
    }

    fun isUserExist(userid: String): Boolean {
        return callDao.isUserExist(userid)
    }
//    suspend fun delete(noteEntity: NoteEntity){
//        noteDao.delete(noteEntity)
//    }
}