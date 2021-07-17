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

    fun inserContact(contactEntity: ContactEntity)=viewModelScope.launch (Dispatchers.IO){
        repository.insert(contactEntity)
    }
}