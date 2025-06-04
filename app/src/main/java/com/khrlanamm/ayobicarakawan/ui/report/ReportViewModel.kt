package com.khrlanamm.ayobicarakawan.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportDao
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportEntity

class ReportViewModel(private val reportDao: ReportDao) : ViewModel() {

    private val _selectedImageFileName = MutableLiveData<String?>()
    val selectedImageFileName: LiveData<String?> = _selectedImageFileName

    private val _reportSubmissionStatus = MutableLiveData<SubmissionStatus>()
    val reportSubmissionStatus: LiveData<SubmissionStatus> = _reportSubmissionStatus

    // To indicate loading state for image "upload"
    private val _isImageUploading = MutableLiveData<Boolean>()
    val isImageUploading: LiveData<Boolean> = _isImageUploading

    fun setSelectedImageFileName(fileName: String?) {
        _selectedImageFileName.value = fileName
    }

    fun setImageUploading(isUploading: Boolean) {
        _isImageUploading.value = isUploading
    }

    fun submitReport(report: ReportEntity) {
        viewModelScope.launch {
            _reportSubmissionStatus.value = SubmissionStatus.LOADING
            try {
                val rowId = reportDao.insertReport(report)
                if (rowId > 0) {
                    _reportSubmissionStatus.value = SubmissionStatus.SUCCESS("Laporan berhasil dikirim dengan ID: $rowId")
                    clearFormState()
                } else {
                    _reportSubmissionStatus.value = SubmissionStatus.ERROR("Gagal menyimpan laporan.")
                }
            } catch (e: Exception) {
                _reportSubmissionStatus.value = SubmissionStatus.ERROR("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    private fun clearFormState() {
        // This could be used to reset any state in the ViewModel after successful submission
        // For now, just resetting the image file name
        _selectedImageFileName.value = null
    }

    // Call this to reset the status, e.g., after the Fragment has shown a message
    fun resetSubmissionStatus() {
        _reportSubmissionStatus.value = SubmissionStatus.IDLE
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel any coroutines if necessary
    }
}

sealed class SubmissionStatus {
    object IDLE : SubmissionStatus()
    object LOADING : SubmissionStatus()
    data class SUCCESS(val message: String) : SubmissionStatus()
    data class ERROR(val message: String) : SubmissionStatus()
}

