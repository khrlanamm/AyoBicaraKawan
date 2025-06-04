package com.khrlanamm.ayobicarakawan.ui.report.reportdata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long // Returns the row ID of the inserted item

    // Optional: If you want to display reports later
    @Query("SELECT * FROM reports ORDER BY submissionTimestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>
}
