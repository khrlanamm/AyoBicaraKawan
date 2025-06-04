package com.khrlanamm.ayobicarakawan.ui.report.reportdata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReportEntity::class], version = 1, exportSchema = false)
abstract class ReportDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: ReportDatabase? = null

        fun getDatabase(context: Context): ReportDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReportDatabase::class.java,
                    "report_app_database"
                )
                    // .fallbackToDestructiveMigration() // Hati-hati saat development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
    