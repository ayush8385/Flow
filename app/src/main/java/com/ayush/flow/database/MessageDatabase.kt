package com.ayush.flow.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MessageEntity::class], version=1,exportSchema = true)
abstract class MessageDatabase: RoomDatabase() {
    abstract fun messageDao():MessageDao

    companion object{
        @Volatile
        private var INSTANCE:MessageDatabase?=null

        fun getDatabase(context: Context):MessageDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance=
                    Room.databaseBuilder(context.applicationContext,MessageDatabase::class.java,"message-db").build()
                INSTANCE=instance
                instance
            }
        }
    }
}