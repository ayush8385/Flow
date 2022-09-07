package com.ayush.flow.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel(application: Application):AndroidViewModel(application) {
    val allContacts:LiveData<List<ContactEntity>>
    val repository:ContactRepository

    init {
        val database=ContactDatabase.getDatabase(application).contactDao()
        repository=ContactRepository(database)
        allContacts=repository.allContacts
    }

    fun isUserExist(userid:String):Boolean{
        return repository.isUserExist(userid)
    }

    fun isContactExist(num:String):Boolean{
        return repository.isNumberExist(num)
    }

    fun inserContact(contactEntity: ContactEntity)=viewModelScope.launch (Dispatchers.IO){
        repository.insert(contactEntity)
    }

    fun updateImage(id:String,path:String)=viewModelScope.launch (Dispatchers.IO){
        repository.updateImg(id,path)
    }

    fun getContact(userid: String):ContactEntity{
        return repository.getContactbyId(userid)
    }

    fun updateDetails(userid: String, name: String?, phoneNum: String) {
        repository.updateDetails(userid,name,phoneNum)
    }

    fun getContactByNum(num: String): ContactEntity {
        return repository.getContactbyNum(num)
    }

    fun getCurrProfileUrl(userid: String,num: String):String{
        return repository.getProfileUrl(userid,num)
    }
}