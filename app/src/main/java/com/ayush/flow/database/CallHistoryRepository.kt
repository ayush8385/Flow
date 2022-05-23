package com.ayush.flow.database

import android.util.Log
import androidx.lifecycle.LiveData

class CallHistoryRepository(private val callHistoryDao: CallHistoryDao) {
//    val allCallHistory:LiveData<List<CallHistoryEntity>> = callHistoryDao.getAllCallHistory()

    suspend fun insert(callHistoryEntity: CallHistoryEntity){
        callHistoryDao.insertCallHistory(callHistoryEntity)
    }

    fun getCallHistory(id:String):LiveData<List<CallHistoryEntity>>{
        return callHistoryDao.getCallHistory(id)
    }

    suspend fun delete(userid: String){
        callHistoryDao.deleteCallHistory(userid)
    }

    fun isUserExist(userid: String): Boolean {
        return callHistoryDao.isUserExist(userid)
    }

    fun callStatus(mCallId: String?): String {
        return callHistoryDao.callStatus(mCallId)
    }

//    suspend fun delete(noteEntity: NoteEntity){
//        noteDao.delete(noteEntity)
//    }
}