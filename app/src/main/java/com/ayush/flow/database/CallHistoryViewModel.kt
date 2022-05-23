package com.ayush.flow.database

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallHistoryViewModel(context: Context):ViewModel() {
    var allCallHistory:LiveData<List<CallHistoryEntity>>?=null
    val repository: CallHistoryRepository

    init {
        val database= CallHistoryDatabase.getDatabase(context).callHistoryDao()
        repository= CallHistoryRepository(database)
    }

    fun getCallHistory(id: String):LiveData<List<CallHistoryEntity>> {
       return repository.getCallHistory(id)
    }

    fun isUserExist(userid:String):Boolean{
        return repository.isUserExist(userid)
    }

    fun insertCallHistory(callHistoryEntity: CallHistoryEntity)=viewModelScope.launch (Dispatchers.IO){
        repository.insert(callHistoryEntity)
    }

    fun deleteCallHistory(userid: String)=viewModelScope.launch (Dispatchers.IO){
        repository.delete(userid)
    }

    fun getCallStatus(mCallId: String?): String {
        return repository.callStatus(mCallId)
    }
}