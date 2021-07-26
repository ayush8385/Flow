package com.ayush.flow.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ayush.flow.database.ChatEntity

class ForwardViewModel : ViewModel() {
    val mutableLiveData:MutableLiveData<List<ChatEntity>> = MutableLiveData()

    public fun setChat(s:ChatEntity){
        mutableLiveData.value= listOf(s)
    }

    public fun getChats():MutableLiveData<List<ChatEntity>>{
        return mutableLiveData
    }
}