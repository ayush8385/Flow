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

    fun getChat(userid: String):ChatEntity{
        return repository.getChatbyId(userid)
    }

    fun deleteChat(userid: String)=viewModelScope.launch (Dispatchers.IO){
        repository.delete(userid)
    }

    fun updatetChat(id: String, img: String) =viewModelScope.launch (Dispatchers.IO){
        repository.update(id,img)
    }

    fun updateName(name:String,id: String) =viewModelScope.launch (Dispatchers.IO){
        repository.updateName(id,name)
    }

    fun setPrivate(id:String,hide:Boolean)=viewModelScope.launch (Dispatchers.IO){
        repository.setPrivate(id,hide)
    }

    fun setUnread(n:Int,userid: String)=viewModelScope.launch (Dispatchers.IO){
        repository.setUnread(n,userid)
    }

    fun setLastMsg(s: String, userId: String)=viewModelScope.launch(Dispatchers.IO){
        repository.setLastMsg(s,userId)
    }
}