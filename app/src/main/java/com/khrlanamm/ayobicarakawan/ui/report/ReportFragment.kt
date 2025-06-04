package com.khrlanamm.ayobicarakawan.ui.report

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportViewModel: ReportViewModel
    private var selectedImageUri: Uri? = null // To hold the URI temporarily

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize ViewModel
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
                        selectedImageUri = uri // Store URI
                        val fileName = getFileName(uri)
                        reportViewModel.setSelectedImageFileName(fileName) // Update ViewModel

                        // Simulate upload
                        reportViewModel.setImageUploading(true)
                        Handler(Looper.getMainLooper()).postDelayed({
                            reportViewModel.setImageUploading(false)
                            binding.tvUploadLabel.text =
                                getString(R.string.label_gambar_telah_diunggah_custom) // Make sure this string exists
                            binding.btnSelectFile.text =
                                fileName ?: getString(R.string.button_berkas_terpilih_custom) // Make sure this string exists
                            binding.btnSelectFile.setTextColor(Color.BLACK)
                            binding.btnSelectFile.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.button_background_greyed_out // Make sure this drawable exists
                            )
                        }, 2000) // 2 seconds delay
                    }
                }
            }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        // Try to get the display name from the content resolver
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
        // If the display name is not available, try to get it from the path
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
            submitReport() // This will now trigger the flow including the new delay on success
        }
    }

    private fun observeViewModel() {
        reportViewModel.selectedImageFileName.observe(viewLifecycleOwner) { fileName ->
            // This observer can be used if you need to react to filename changes elsewhere
        }

        reportViewModel.isImageUploading.observe(viewLifecycleOwner) { isUploading ->
            // This progress bar is for the image "upload" simulation.
            // If you want the main progressBarContainer to also show for this, you'll need to manage its visibility here too.
            // For now, it's separate.
            if (isUploading) {
                binding.progressBarContainer.visibility = View.VISIBLE
            } else {
                // Only hide if not in report submission loading state
                if (reportViewModel.reportSubmissionStatus.value !is SubmissionStatus.LOADING) {
                    binding.progressBarContainer.visibility = View.GONE
                }
            }
        }

        reportViewModel.reportSubmissionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is SubmissionStatus.LOADING -> {
                    binding.progressBarContainer.visibility = View.VISIBLE
                    binding.btnSubmitReport.isEnabled = false
                }

                is SubmissionStatus.SUCCESS -> {
                    // Show progress bar for 2 seconds BEFORE showing the success dialog
                    binding.progressBarContainer.visibility = View.VISIBLE
                    binding.btnSubmitReport.isEnabled = false // Keep button disabled during this phase

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.progressBarContainer.visibility = View.GONE
                        binding.btnSubmitReport.isEnabled = true // Re-enable before showing dialog or after, depending on desired UX
                        showSuccessDialog()
                        // clearForm() and resetSubmissionStatus() are called after dialog dismissal
                    }, 2000) // 2 seconds delay
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
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.abk_logo) // Make sure abk_logo exists
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
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) // Corrected pattern
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
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            imagePickerLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Tidak ada aplikasi galeri ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

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
            // No requestFocus() here as etIncidentDate is not directly editable
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

        if (reportViewModel.selectedImageFileName.value == null) {
            Toast.makeText(context, "Unggah gambar/bukti pendukung", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun submitReport() {
        if (!validateInputs()) {
            reportViewModel.setImageUploading(false) // Ensure image upload simulation is also stopped
            binding.progressBarContainer.visibility = View.GONE
            binding.btnSubmitReport.isEnabled = true
            return
        }

        val reporterType =
            if (binding.rbVictim.isChecked) getString(R.string.radio_korban) else getString(R.string.radio_saksi)
        val reporterName = binding.etReporterName.text.toString()
        val incidentDate = binding.etIncidentDate.text.toString()
        val incidentPlace = binding.etIncidentPlace.text.toString()
        val incidentDescription = binding.etIncidentDescription.text.toString()
        val contactNumber = binding.etContactNumber.text.toString()
        val imageFileName = reportViewModel.selectedImageFileName.value

        val report = ReportEntity(
            reporterType = reporterType,
            reporterName = reporterName,
            incidentDate = incidentDate,
            incidentPlace = incidentPlace,
            incidentDescription = incidentDescription,
            contactNumber = contactNumber,
            imageFileName = imageFileName,
            submissionTimestamp = System.currentTimeMillis()
        )

        reportViewModel.submitReport(report)
    }

    private fun clearForm() {
        binding.rgReporterType.clearCheck()
        binding.etReporterName.text.clear()
        binding.etIncidentDate.text.clear() // Also clear the text for etIncidentDate
        binding.etIncidentPlace.text.clear()
        binding.etIncidentDescription.text.clear()
        binding.etContactNumber.text.clear()

        binding.etReporterName.error = null
        // binding.etIncidentDate.error = null // Not needed as it's not an error field
        binding.etIncidentPlace.error = null
        binding.etIncidentDescription.error = null
        binding.etContactNumber.error = null

        binding.tvUploadLabel.text = getString(R.string.label_unggah_bukti) // Make sure this string exists
        binding.btnSelectFile.text = getString(R.string.button_pilih_berkas) // Make sure this string exists
        binding.btnSelectFile.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.button_text_color // Make sure this color exists
            )
        )
        binding.btnSelectFile.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.button_background_light_purple) // Make sure this drawable exists

        selectedImageUri = null
        reportViewModel.setSelectedImageFileName(null)
        reportViewModel.setImageUploading(false) // Ensure this is reset
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
