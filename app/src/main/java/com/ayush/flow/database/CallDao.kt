package com.ayush.flow.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CallDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCall(callEntity: CallEntity)

    @Query("DELETE FROM calls WHERE id = :id")
    fun deleteCall(id: String)

    @Query("SELECT * FROM calls")
    fun getAllCalls():LiveData<List<CallEntity>>

    @Query("SELECT EXISTS(SELECT * FROM calls WHERE id = :id)")
    fun isUserExist(id : String) : Boolean

//    @Query("SELECT * FROM contacts where contact_number=:number")
//    fun getRestaurantsbyId(number:String):ContactEntity
}