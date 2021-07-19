package com.ayush.flow.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CallEntity::class], version=1,exportSchema = false)
abstract class CallDatabase: RoomDatabase() {
    abstract fun callDao(): CallDao

    companion object{
        @Volatile
        private var INSTANCE: CallDatabase?=null

        fun getDatabase(context: Context): CallDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance=
                    Room.databaseBuilder(context.applicationContext, CallDatabase::class.java,"call-db").allowMainThreadQueries().build()
                INSTANCE =instance
                instance
            }
        }
    }
}