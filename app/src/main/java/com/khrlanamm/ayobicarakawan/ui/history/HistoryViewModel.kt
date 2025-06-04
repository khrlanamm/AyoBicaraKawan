package com.khrlanamm.ayobicarakawan.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportDao // Pastikan import ini benar
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportEntity // Pastikan import ini benar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryViewModel(private val reportDao: ReportDao) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _showEmptyToast = MutableLiveData<Boolean>()
    val showEmptyToast: LiveData<Boolean> = _showEmptyToast

    // Menggunakan Flow dari Room dan mengubahnya menjadi LiveData
    val allReports: LiveData<List<ReportEntity>> = reportDao.getAllReports().asLiveData()

    init {
        loadReportsWithDelay()
    }

    private fun loadReportsWithDelay() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(2000) // Delay 2 detik
            // Data akan otomatis di-load oleh LiveData dari Flow `allReports`
            // Kita hanya perlu menunggu observasi pertama untuk mengecek apakah kosong
            // atau bisa juga mengecek setelah delay di sini jika diperlukan,
            // tapi lebih baik mengobservasi `allReports` di Fragment.
            // _isLoading.value = false // Akan di-set false di Fragment setelah data diobservasi
        }
    }

    fun onDataLoaded(isEmpty: Boolean) {
        _isLoading.value = false // Set loading false setelah data diobservasi di Fragment
        if (isEmpty) {
            _showEmptyToast.value = true
        }
    }

    fun onEmptyToastShown() {
        _showEmptyToast.value = false // Reset agar toast tidak muncul lagi tanpa trigger baru
    }

    override fun onCleared() {
        super.onCleared()
    }
}
