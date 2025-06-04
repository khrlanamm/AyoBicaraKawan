package com.khrlanamm.ayobicarakawan.ui.report

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context // Import Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.ayobicarakawan.R
import com.khrlanamm.ayobicarakawan.databinding.FragmentReportBinding
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportEntity
import java.io.File // Import File
import java.io.FileOutputStream // Import FileOutputStream
import java.io.InputStream // Import InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID // Import UUID

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportViewModel: ReportViewModel
    private var selectedImageUri: Uri? = null // Tetap untuk URI sementara
    private var persistedImageUriString: String? = null // Untuk menyimpan URI yang sudah di-persist atau path file internal

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val application = requireActivity().application
        val factory = ReportViewModelFactory(application)
        reportViewModel = ViewModelProvider(this, factory).get(ReportViewModel::class.java)

        setupImagePicker()
        setupUIListeners()
        observeViewModel()

        return root
    }

    private fun setupImagePicker() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        selectedImageUri = uri // Simpan URI asli untuk sementara
                        val fileName = getFileName(uri)

                        // Ambil persistable URI permission
                        try {
                            val contentResolver = requireActivity().contentResolver
                            contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            persistedImageUriString = uri.toString() // Simpan URI sebagai String
                            reportViewModel.setSelectedImageUriString(persistedImageUriString) // Update ViewModel dengan URI String
                            Log.d("ReportFragment", "Persisted URI: $persistedImageUriString")

                            // Update UI seperti sebelumnya
                            reportViewModel.setImageUploading(true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                reportViewModel.setImageUploading(false)
                                binding.tvUploadLabel.text =
                                    getString(R.string.label_gambar_telah_diunggah_custom)
                                binding.btnSelectFile.text =
                                    fileName ?: getString(R.string.button_berkas_terpilih_custom)
                                binding.btnSelectFile.setTextColor(Color.BLACK)
                                binding.btnSelectFile.background = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.button_background_greyed_out
                                )
                            }, 1000) // Kurangi delay jika perlu

                        } catch (e: SecurityException) {
                            Log.e("ReportFragment", "Failed to take persistable URI permission or copy file", e)
                            Toast.makeText(context, "Gagal mengakses gambar. Coba gambar lain.", Toast.LENGTH_LONG).show()
                            // Alternatif: Salin gambar ke penyimpanan internal aplikasi jika persistable permission gagal
                            copyImageToInternalStorage(uri)?.let { internalPath ->
                                persistedImageUriString = internalPath
                                reportViewModel.setSelectedImageUriString(persistedImageUriString)
                                Log.d("ReportFragment", "Copied to internal storage: $persistedImageUriString")
                                // Lanjutkan update UI
                                reportViewModel.setImageUploading(true)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    reportViewModel.setImageUploading(false)
                                    binding.tvUploadLabel.text = getString(R.string.label_gambar_telah_diunggah_custom)
                                    binding.btnSelectFile.text = fileName ?: getString(R.string.button_berkas_terpilih_custom)
                                    // ... (UI updates lainnya)
                                }, 1000)
                            } ?: run {
                                Toast.makeText(context, "Gagal menyimpan gambar.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
    }

    // Fungsi untuk menyalin gambar ke penyimpanan internal aplikasi (opsional, sebagai fallback)
    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val fileName = "IMG_${UUID.randomUUID()}.jpg" // Nama file unik
            val file = File(requireContext().filesDir, fileName) // Simpan di direktori filesDir internal
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file).toString() // Kembalikan URI dari file yang disalin
        } catch (e: Exception) {
            Log.e("ReportFragment", "Error copying file to internal storage", e)
            null
        }
    }


    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.path
            val cut = fileName?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                fileName = fileName?.substring(cut + 1)
            }
        }
        return fileName
    }


    private fun setupUIListeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.etIncidentDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSelectFile.setOnClickListener {
            openGallery()
        }

        binding.btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    private fun observeViewModel() {
        // Ganti observer untuk imageFileName menjadi imageUriString jika ada
        reportViewModel.selectedImageUriString.observe(viewLifecycleOwner) { uriString ->
            // Anda bisa bereaksi terhadap perubahan URI di sini jika perlu
            if (uriString == null && _binding != null) { // Cek _binding untuk menghindari crash saat view dihancurkan
                // Reset tampilan tombol pilih file jika URI null (misalnya setelah form dibersihkan)
                binding.tvUploadLabel.text = getString(R.string.label_unggah_bukti)
                binding.btnSelectFile.text = getString(R.string.button_pilih_berkas)
                binding.btnSelectFile.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_text_color
                    )
                )
                binding.btnSelectFile.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_light_purple)
            }
        }

        reportViewModel.isImageUploading.observe(viewLifecycleOwner) { isUploading ->
            if (_binding == null) return@observe // Cek null untuk binding
            if (isUploading) {
                binding.progressBarContainer.visibility = View.VISIBLE
            } else {
                if (reportViewModel.reportSubmissionStatus.value !is SubmissionStatus.LOADING) {
                    binding.progressBarContainer.visibility = View.GONE
                }
            }
        }

        reportViewModel.reportSubmissionStatus.observe(viewLifecycleOwner) { status ->
            if (_binding == null) return@observe // Cek null untuk binding
            when (status) {
                is SubmissionStatus.LOADING -> {
                    binding.progressBarContainer.visibility = View.VISIBLE
                    binding.btnSubmitReport.isEnabled = false
                }

                is SubmissionStatus.SUCCESS -> {
                    binding.progressBarContainer.visibility = View.VISIBLE
                    binding.btnSubmitReport.isEnabled = false

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (_binding == null) return@postDelayed // Cek null lagi
                        binding.progressBarContainer.visibility = View.GONE
                        binding.btnSubmitReport.isEnabled = true
                        showSuccessDialog()
                    }, 2000)
                }

                is SubmissionStatus.ERROR -> {
                    binding.progressBarContainer.visibility = View.GONE
                    binding.btnSubmitReport.isEnabled = true
                    Toast.makeText(context, "Error: ${status.message}", Toast.LENGTH_LONG).show()
                    reportViewModel.resetSubmissionStatus()
                }

                SubmissionStatus.IDLE -> {
                    binding.progressBarContainer.visibility = View.GONE
                    binding.btnSubmitReport.isEnabled = true
                }
            }
        }
    }

    private fun showSuccessDialog() {
        if (!isAdded) return // Pastikan fragment masih ter-attach
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.abk_logo)
            .setTitle("Berhasil")
            .setMessage("Terima Kasih telah berani melapor. Data Laporan Berhasil diunggah, kami akan membantu anda semaksimal mungkin, mohon bersabar.")
            .setPositiveButton("OK") { dialog, _ ->
                clearForm()
                reportViewModel.resetSubmissionStatus()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) // Pattern yang umum
                binding.etIncidentDate.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Minta izin baca URI
        }
        // Menggunakan resolveActivity untuk keamanan
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            imagePickerLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Tidak ada aplikasi galeri ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        // ... (validasi input lainnya tetap sama) ...
        if (binding.rgReporterType.checkedRadioButtonId == -1) {
            Toast.makeText(context, "Pilih jenis pelapor (Korban/Saksi)", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        if (binding.etReporterName.text.isNullOrBlank()) {
            binding.etReporterName.error = "Nama pelapor tidak boleh kosong"
            if (isValid) binding.etReporterName.requestFocus()
            isValid = false
        }
        if (binding.etIncidentDate.text.isNullOrBlank()) {
            Toast.makeText(context, "Tanggal kejadian tidak boleh kosong", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        if (binding.etIncidentPlace.text.isNullOrBlank()) {
            binding.etIncidentPlace.error = "Tempat kejadian tidak boleh kosong"
            if (isValid) binding.etIncidentPlace.requestFocus()
            isValid = false
        }
        if (binding.etIncidentDescription.text.isNullOrBlank()) {
            binding.etIncidentDescription.error = "Deskripsi kejadian tidak boleh kosong"
            if (isValid) binding.etIncidentDescription.requestFocus()
            isValid = false
        }
        if (binding.etContactNumber.text.isNullOrBlank()) {
            binding.etContactNumber.error = "Nomor kontak tidak boleh kosong"
            if (isValid) binding.etContactNumber.requestFocus()
            isValid = false
        } else if (!android.util.Patterns.PHONE.matcher(binding.etContactNumber.text.toString()).matches()) {
            binding.etContactNumber.error = "Format nomor kontak tidak valid"
            if (isValid) binding.etContactNumber.requestFocus()
            isValid = false
        }
        // Validasi berdasarkan persistedImageUriString atau selectedImageUriString dari ViewModel
        if (reportViewModel.selectedImageUriString.value == null) {
            Toast.makeText(context, "Unggah gambar/bukti pendukung", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun submitReport() {
        if (!validateInputs()) {
            reportViewModel.setImageUploading(false)
            if (_binding != null) { // Cek null
                binding.progressBarContainer.visibility = View.GONE
                binding.btnSubmitReport.isEnabled = true
            }
            return
        }

        val reporterType =
            if (binding.rbVictim.isChecked) getString(R.string.radio_korban) else getString(R.string.radio_saksi)
        val reporterName = binding.etReporterName.text.toString()
        val incidentDate = binding.etIncidentDate.text.toString()
        val incidentPlace = binding.etIncidentPlace.text.toString()
        val incidentDescription = binding.etIncidentDescription.text.toString()
        val contactNumber = binding.etContactNumber.text.toString()
        // Ambil imageUriString dari ViewModel
        val imageUriStringToSave = reportViewModel.selectedImageUriString.value

        val report = ReportEntity(
            reporterType = reporterType,
            reporterName = reporterName,
            incidentDate = incidentDate,
            incidentPlace = incidentPlace,
            incidentDescription = incidentDescription,
            contactNumber = contactNumber,
            imageUriString = imageUriStringToSave, // Simpan URI string
            submissionTimestamp = System.currentTimeMillis()
        )

        reportViewModel.submitReport(report)
    }

    private fun clearForm() {
        if (_binding == null) return // Cek null
        binding.rgReporterType.clearCheck()
        binding.etReporterName.text.clear()
        binding.etIncidentDate.text.clear()
        binding.etIncidentPlace.text.clear()
        binding.etIncidentDescription.text.clear()
        binding.etContactNumber.text.clear()

        binding.etReporterName.error = null
        binding.etIncidentPlace.error = null
        binding.etIncidentDescription.error = null
        binding.etContactNumber.error = null

        binding.tvUploadLabel.text = getString(R.string.label_unggah_bukti)
        binding.btnSelectFile.text = getString(R.string.button_pilih_berkas)
        binding.btnSelectFile.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.button_text_color
            )
        )
        binding.btnSelectFile.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.button_background_light_purple)

        selectedImageUri = null
        persistedImageUriString = null // Reset juga persisted URI
        reportViewModel.setSelectedImageUriString(null) // Reset di ViewModel
        reportViewModel.setImageUploading(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Penting untuk menghindari memory leak
    }
}