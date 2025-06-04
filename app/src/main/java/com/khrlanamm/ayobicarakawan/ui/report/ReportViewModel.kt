package com.khrlanamm.ayobicarakawan.ui.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportDatabase
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportEntity
import kotlinx.coroutines.launch

sealed class SubmissionStatus {
    object IDLE : SubmissionStatus()
    object LOADING : SubmissionStatus()
    data class SUCCESS(val message: String) : SubmissionStatus()
    data class ERROR(val message: String) : SubmissionStatus()
}


class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val reportDao = ReportDatabase.getDatabase(application).reportDao()

    // Untuk menyimpan URI gambar yang dipilih (sebagai String)
    private val _selectedImageUriString = MutableLiveData<String?>()
    val selectedImageUriString: LiveData<String?> = _selectedImageUriString

    // Untuk status upload gambar (simulasi)
    private val _isImageUploading = MutableLiveData<Boolean>(false)
    val isImageUploading: LiveData<Boolean> = _isImageUploading

    // Untuk status pengiriman laporan
    private val _reportSubmissionStatus = MutableLiveData<SubmissionStatus>(SubmissionStatus.IDLE)
    val reportSubmissionStatus: LiveData<SubmissionStatus> = _reportSubmissionStatus


    fun setSelectedImageUriString(uriString: String?) {
        _selectedImageUriString.value = uriString
    }

    fun setImageUploading(isUploading: Boolean) {
        _isImageUploading.value = isUploading
    }

    fun submitReport(report: ReportEntity) {
        _reportSubmissionStatus.value = SubmissionStatus.LOADING
        viewModelScope.launch {
            try {
                reportDao.insertReport(report) // Pastikan DAO memiliki fungsi insertReport
                _reportSubmissionStatus.postValue(SubmissionStatus.SUCCESS("Laporan berhasil dikirim."))
            } catch (e: Exception) {
                _reportSubmissionStatus.postValue(SubmissionStatus.ERROR("Gagal mengirim laporan: ${e.message}"))
            }
        }
    }

    fun resetSubmissionStatus() {
        _reportSubmissionStatus.value = SubmissionStatus.IDLE
    }
}