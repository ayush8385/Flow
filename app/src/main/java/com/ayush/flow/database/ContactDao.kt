package com.ayush.flow.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContact(contactEntity: ContactEntity)

//    @Delete
//    fun deleteRestaurant(contactEntity: ContactEntity)
//    @Update
//    fun updateContact(id:String)

    @Query("SELECT * FROM contacts ORDER BY isUser DESC,contact_name")
    fun getAllContacts():LiveData<List<ContactEntity>>

    @Query("SELECT EXISTS(SELECT * FROM contacts WHERE contact_id= :id)")
    fun isUserExist(id : String) : Boolean

    @Query("SELECT * FROM contacts WHERE contact_id = :id")
    fun getContactbyId(id : String) : ContactEntity

    @Query("UPDATE contacts SET contact_image = :path WHERE contact_id= :id")
    fun update(id: String, path: String)

    @Query("UPDATE contacts SET contact_name = :name, number = :phoneNum WHERE contact_id = :userid")
    fun updateDetails(userid: String, name: String?, phoneNum: String)

    @Query("SELECT EXISTS(SELECT * FROM contacts WHERE number = :num)")
    fun isNumExist(num: String): Boolean

    @Query("SELECT * FROM contacts WHERE number = :num")
    fun getContactbyNum(num: String): ContactEntity

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}