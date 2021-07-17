package com.ayush.flow.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(application: Application):AndroidViewModel(application) {
    val allChats:LiveData<List<ChatEntity>>
    val repository: ChatRepository

    init {
        val database= ChatDatabase.getDatabase(application).chatDao()
        repository= ChatRepository(database)
        allChats=repository.allChats
    }

    fun isUserExist(userid:String):Boolean{
        return repository.isUserExist(userid)
    }

    fun inserChat(chatEntity: ChatEntity)=viewModelScope.launch (Dispatchers.IO){
        repository.insert(chatEntity)
    }

    fun deleteChat(userid: String)=viewModelScope.launch (Dispatchers.IO){
        repository.delete(userid)
    }
}