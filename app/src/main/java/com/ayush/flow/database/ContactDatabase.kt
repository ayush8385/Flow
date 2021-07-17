package com.ayush.flow.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ContactEntity::class], version=1,exportSchema = false)
abstract class ContactDatabase: RoomDatabase() {
    abstract fun contactDao():ContactDao

    companion object{
        @Volatile
        private var INSTANCE:ContactDatabase?=null

        fun getDatabase(context: Context):ContactDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance=
                    Room.databaseBuilder(context.applicationContext,ContactDatabase::class.java,"contact-db").allowMainThreadQueries().build()
                INSTANCE=instance
                instance
            }
        }
    }
}