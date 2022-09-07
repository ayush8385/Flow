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

    fun updateImg(id: String, path: String) {
        contactDao.update(id,path)
    }

    fun updateDetails(userid: String, name: String?, phoneNum: String) {
        contactDao.updateDetails(userid,name,phoneNum)
    }

    fun isNumberExist(num: String): Boolean {
        return contactDao.isNumExist(num)
    }

    fun getContactbyNum(num: String): ContactEntity {
        return contactDao.getContactbyNum(num)
    }

    fun getProfileUrl(userid: String,num: String): String {
        if(userid==""){
            return contactDao.getProfileUrlByNum(num)
        }
        else{
            return contactDao.getProfileUrlById(userid)
        }
    }

//    suspend fun delete(noteEntity: NoteEntity){
//        noteDao.delete(noteEntity)
//    }
    
}