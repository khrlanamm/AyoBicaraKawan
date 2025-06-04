package com.khrlanamm.ayobicarakawan.ui.report.reportdata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reporterType: String,
    val reporterName: String?,
    val incidentDate: String,
    val incidentLocation: String,
    val incidentDescription: String,
    val contactNumber: String?,
    val imagePath: String?,
    val submissionTimestamp: Long,
    val status: String = "Terkirim"
)
    