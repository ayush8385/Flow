package com.ayush.flow.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CallHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCallHistory(callHistoryEntity: CallHistoryEntity)

    @Query("DELETE FROM callhistory WHERE id = :id")
    fun deleteCallHistory(id: String)

    @Query("SELECT * FROM callhistory WHERE userid is :userid")
    fun getCallHistory(userid:String): LiveData<List<CallHistoryEntity>>

    @Query("SELECT EXISTS(SELECT * FROM callhistory WHERE id = :id)")
    fun isUserExist(id : String) : Boolean

    @Query("SELECT status FROM callhistory WHERE id=:mCallId")
    fun callStatus(mCallId: String?): String

}