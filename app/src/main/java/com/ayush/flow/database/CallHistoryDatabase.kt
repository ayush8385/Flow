package com.ayush.flow.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CallHistoryEntity::class], version=1,exportSchema = false)
abstract class CallHistoryDatabase: RoomDatabase() {
    abstract fun callHistoryDao(): CallHistoryDao

    companion object{
        @Volatile
        private var INSTANCE: CallHistoryDatabase?=null

        fun getDatabase(context: Context): CallHistoryDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance=
                    Room.databaseBuilder(context.applicationContext, CallHistoryDatabase::class.java,"callhistory-db").allowMainThreadQueries().build()
                INSTANCE =instance
                instance
            }
        }
    }
}