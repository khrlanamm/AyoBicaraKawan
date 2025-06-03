package com.khrlanamm.ayobicarakawan.ui.chat.chatdata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_database"
                )
                    // .fallbackToDestructiveMigration() // Hati-hati saat production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
