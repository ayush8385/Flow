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

    @Query("SELECT * FROM contacts")
    fun getAllContacts():LiveData<List<ContactEntity>>

    @Query("SELECT EXISTS(SELECT * FROM contacts WHERE id = :id)")
    fun isUserExist(id : String) : Boolean

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun getContactbyId(id : String) : ContactEntity

    @Query("UPDATE contacts SET contact_image = :path WHERE id = :id")
    fun update(id: String, path: String)

    @Query("UPDATE contacts SET contact_name = :name, contact_number = :phoneNum WHERE id = :userid")
    fun updateDetails(userid: String, name: String?, phoneNum: String)

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}