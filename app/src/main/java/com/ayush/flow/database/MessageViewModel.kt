package com.ayush.flow.database

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(context: Context): ViewModel() {

    val repository:MessageRepository

    init {
        val database=MessageDatabase.getDatabase(context).messageDao()
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

    fun deleteMsg(item: MessageEntity) {
        repository.deleteMsg(item)
    }

    fun deleteMsgWithId(id:String){
        repository.deleteMsgWithId(id)
    }

    fun updateMsgStatus(status: String,mid: String)=viewModelScope.launch(Dispatchers.IO) {
        repository.updateMsgStatus(status, mid)
    }

    fun getUnreads(id: String):LiveData<Int>{
        return repository.getUnreads(id)
    }

    fun getMsgStatus(mid: String):LiveData<String>{
        return repository.getMsgStatus(mid)
    }

    fun setAllMsgSeen(userid: String)=viewModelScope.launch(Dispatchers.IO) {
        repository.setMsgSeen(userid)
    }
}