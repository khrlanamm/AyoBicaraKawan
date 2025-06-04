package com.khrlanamm.ayobicarakawan.ui.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportDatabase // Pastikan import ini benar

class HistoryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            val reportDao = ReportDatabase.getDatabase(application).reportDao()
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(reportDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
