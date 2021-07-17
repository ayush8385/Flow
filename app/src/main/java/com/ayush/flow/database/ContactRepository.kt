package com.ayush.flow.database

import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao) {
    val allContacts:LiveData<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun insert(contactEntity: ContactEntity){
        contactDao.insertContact(contactEntity)
    }

//    suspend fun delete(noteEntity: NoteEntity){
//        noteDao.delete(noteEntity)
//    }
}