package com.khrlanamm.ayobicarakawan.ui.report

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportDatabase

class ReportViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            val reportDao = ReportDatabase.getDatabase(application).reportDao()
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(reportDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
