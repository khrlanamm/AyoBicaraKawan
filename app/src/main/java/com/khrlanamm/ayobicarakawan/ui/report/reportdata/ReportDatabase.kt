package com.khrlanamm.ayobicarakawan.ui.report.reportdata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Naikkan versi database menjadi 2
@Database(entities = [ReportEntity::class], version = 2, exportSchema = false)
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
                    "ayobicarakawan_report_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}