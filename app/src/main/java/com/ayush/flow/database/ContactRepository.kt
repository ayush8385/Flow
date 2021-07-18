package com.ayush.flow.database

import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao) {
    val allContacts:LiveData<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun insert(contactEntity: ContactEntity){
        contactDao.insertContact(contactEntity)
    }
    fun isUserExist(userid: String): Boolean {
        return contactDao.isUserExist(userid)
    }

    fun getContactbyId(userid: String):ContactEntity{
        return contactDao.getContactbyId(userid)
    }

//    suspend fun delete(noteEntity: NoteEntity){
//        noteDao.delete(noteEntity)
//    }
    
}