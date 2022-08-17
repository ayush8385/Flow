package com.ayush.flow.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ChatEntity::class], version=1,exportSchema = false)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object{

        val migration_1_2 = object :Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE chat ADD COLUMN isseen")
            }

        }

        @Volatile
        private var INSTANCE: ChatDatabase?=null

        fun getDatabase(context: Context): ChatDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance=
                    Room.databaseBuilder(context.applicationContext, ChatDatabase::class.java,"chat-db").allowMainThreadQueries().addMigrations(
                        migration_1_2).build()
                INSTANCE =instance
                instance
            }
        }
    }
}