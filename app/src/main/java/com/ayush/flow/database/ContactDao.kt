package com.ayush.flow.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertContact(contactEntity: ContactEntity)

//    @Delete
//    fun deleteRestaurant(contactEntity: ContactEntity)

    @Query("SELECT * FROM contacts")
    fun getAllContacts():LiveData<List<ContactEntity>>

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}