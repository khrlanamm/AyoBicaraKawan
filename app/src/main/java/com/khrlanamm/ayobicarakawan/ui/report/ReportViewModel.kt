package com.khrlanamm.ayobicarakawan.ui.report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportDao
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ReportViewModel(
    private val application: Application,
    private val reportDao: ReportDao
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _reportSubmissionStatus = MutableLiveData<Result<Unit>>()
    val reportSubmissionStatus: LiveData<Result<Unit>> = _reportSubmissionStatus

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun submitReport(
        reporterType: String,
        reporterName: String?,
        incidentDate: String,
        incidentLocation: String,
        incidentDescription: String,
        contactNumber: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            var imagePath: String? = null

            // Save image if selected
            _selectedImageUri.value?.let { uri ->
                imagePath = saveImageToInternalStorage(uri)
            }

            val report = ReportEntity(
                reporterType = reporterType,
                reporterName = reporterName?.ifBlank { null }, // Simpan null jika kosong
                incidentDate = incidentDate,
                incidentLocation = incidentLocation,
                incidentDescription = incidentDescription,
                contactNumber = contactNumber?.ifBlank { null }, // Simpan null jika kosong
                imagePath = imagePath,
                submissionTimestamp = System.currentTimeMillis(),
                status = "Terkirim"
            )

            try {
                reportDao.insertReport(report)
                // Dummy delay
                delay(2000)
                _reportSubmissionStatus.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _reportSubmissionStatus.postValue(Result.failure(e))
            } finally {
                _isLoading.postValue(false)
                _selectedImageUri.postValue(null) // Reset image after submission
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = application.contentResolver.openInputStream(uri)
            val fileName = "report_image_${System.currentTimeMillis()}.jpg"
            val file = File(application.cacheDir, fileName) // Simpan di cacheDir atau filesDir
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}