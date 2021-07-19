package com.ayush.flow.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallViewModel(application: Application):AndroidViewModel(application) {
    val allCalls:LiveData<List<CallEntity>>
    val repository: CallRepository

    init {
        val database= CallDatabase.getDatabase(application).callDao()
        repository= CallRepository(database)
        allCalls=repository.allCalls
    }

    fun isUserExist(userid:String):Boolean{
        return repository.isUserExist(userid)
    }

    fun inserCall(callEntity: CallEntity)=viewModelScope.launch (Dispatchers.IO){
        repository.insert(callEntity)
    }

    fun deleteCall(userid: String)=viewModelScope.launch (Dispatchers.IO){
        repository.delete(userid)
    }
}