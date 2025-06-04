package com.khrlanamm.ayobicarakawan.ui.report.reportdata

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long // Return Long untuk mendapatkan ID

    @Query("SELECT * FROM reports ORDER BY submissionTimestamp DESC")
    fun getAllReports(): LiveData<List<ReportEntity>> // Jika ingin menampilkan riwayat laporan

    @Query("SELECT * FROM reports WHERE id = :reportId")
    suspend fun getReportById(reportId: Long): ReportEntity?
}
    