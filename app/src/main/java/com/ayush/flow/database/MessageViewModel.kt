package com.ayush.flow.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(application: Application): AndroidViewModel(application) {

    val repository:MessageRepository

    init {
        val database=MessageDatabase.getDatabase(application).messageDao()
        repository=MessageRepository(database)
    }

    fun isMsgExist(userid:String):Boolean{
        return repository.isMsgExist(userid)
    }

    fun allMessages(userid:String):LiveData<List<MessageEntity>>{
        return repository.allMessages(userid)
    }

    fun insertMessage(messageEntity: MessageEntity)=viewModelScope.launch (Dispatchers.IO){
        repository.insert(messageEntity)
    }

    fun updatetMessage(mid: String, rec: Boolean, seen: Boolean) =viewModelScope.launch (Dispatchers.IO){
        repository.update(mid,rec,seen)
    }

    fun deleteMsg(item: MessageEntity) {
        repository.deleteMsg(item)
    }
}