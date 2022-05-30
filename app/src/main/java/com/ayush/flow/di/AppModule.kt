package com.ayush.flow.di

import android.content.Context
import com.ayush.flow.database.ChatDao
import com.ayush.flow.database.ChatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun getChatDB(@ApplicationContext context: Context):ChatDatabase{
        return ChatDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun getChatDao(chatDB:ChatDatabase):ChatDao{
        return chatDB.chatDao()
    }
}