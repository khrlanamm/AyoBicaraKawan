package com.khrlanamm.ayobicarakawan.ui.report

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.khrlanamm.ayobicarakawan.R
import com.khrlanamm.ayobicarakawan.databinding.DialogReportSuccessBinding
import com.khrlanamm.ayobicarakawan.databinding.DialogSelectImageSourceBinding
import com.khrlanamm.ayobicarakawan.databinding.FragmentReportBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportViewModel: ReportViewModel
    private var currentPhotoPath: String? = null
    private var selectedImageUriForSubmission: Uri? = null

    // ActivityResultLauncher for camera
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                val photoFile = File(path)
                selectedImageUriForSubmission = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider", // Sesuaikan dengan provider Anda
                    photoFile
                )
                reportViewModel.setSelectedImageUri(selectedImageUriForSubmission)
            }
        } else {
            Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
        }
    }

    // ActivityResultLauncher for gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUriForSubmission = it
            reportViewModel.setSelectedImageUri(it)
        }
    }

    // ActivityResultLauncher for permissions
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val factory = ReportViewModelFactory(requireActivity().application)
        reportViewModel = ViewModelProvider(this, factory)[ReportViewModel::class.java]

        setupUI()
        observeViewModel()

        return root
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.editTextDate.setOnClickListener {
            showDatePickerDialog()
        }
        // Juga set listener pada TextInputLayout untuk memastikan klik terdeteksi
        binding.textFieldDate.setEndIconOnClickListener {
            showDatePickerDialog()
        }


        binding.buttonSelectFile.setOnClickListener {
            showImageSourceDialog()
        }

        binding.buttonSendReport.setOnClickListener {
            submitReportData()
        }
    }

    private fun observeViewModel() {
        reportViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonSendReport.isEnabled = !isLoading // Disable button while loading
        }

        reportViewModel.reportSubmissionStatus.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    showSuccessDialog()
                    clearForm()
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Gagal mengirim laporan: ${it.message}", Toast.LENGTH_LONG).show()
                }
            )
        }

        reportViewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                binding.imageViewPreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder) // Error placeholder
                    .into(binding.imageViewPreview)
            } else {
                binding.imageViewPreview.visibility = View.GONE
                Glide.with(this).clear(binding.imageViewPreview) // Clear image
                binding.imageViewPreview.setImageResource(R.drawable.ic_image_placeholder) // Reset to placeholder
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.editTextDate.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Tidak bisa memilih tanggal di masa depan
        datePickerDialog.show()
    }

    private fun showImageSourceDialog() {
        val dialogBinding = DialogSelectImageSourceBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonCamera.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
            dialog.dismiss()
        }
        dialogBinding.buttonGallery.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Tampilkan dialog penjelasan jika diperlukan
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Izin Kamera Diperlukan")
                    .setMessage("Aplikasi ini memerlukan izin kamera untuk mengambil gambar bukti. Mohon berikan izin.")
                    .setPositiveButton("OK") { _, _ ->
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(requireContext(), "Gagal membuat file gambar", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider", // Pastikan ini sesuai dengan AndroidManifest.xml
                it
            )
            takePictureLauncher.launch(photoURI)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun submitReportData() {
        val reporterType = if (binding.radioButtonVictim.isChecked) "Korban" else "Saksi"
        val reporterName = binding.editTextName.text.toString().trim()
        val incidentDate = binding.editTextDate.text.toString().trim()
        val incidentLocation = binding.editTextLocation.text.toString().trim()
        val incidentDescription = binding.editTextDescription.text.toString().trim()
        val contactNumber = binding.editTextContact.text.toString().trim()

        if (incidentDate.isEmpty() || incidentLocation.isEmpty() || incidentDescription.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon lengkapi field Tanggal, Tempat, dan Deskripsi Kejadian", Toast.LENGTH_LONG).show()
            return
        }

        reportViewModel.submitReport(
            reporterType,
            reporterName,
            incidentDate,
            incidentLocation,
            incidentDescription,
            contactNumber
        )
    }

    private fun showSuccessDialog() {
        val dialogSuccessBinding = DialogReportSuccessBinding.inflate(LayoutInflater.from(requireContext()))
        val successDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogSuccessBinding.root)
            .setCancelable(false) // User harus menekan OK
            .create()

        // Load logo using Glide or set directly if it's simple
        Glide.with(this)
            .load(R.drawable.abk_logo) // Pastikan abk_logo.png ada di drawable
            .into(dialogSuccessBinding.imageViewLogo)


        dialogSuccessBinding.buttonOk.setOnClickListener {
            successDialog.dismiss()
            // Opsional: Navigasi ke halaman lain atau reset fragment
            findNavController().navigateUp() // Kembali ke halaman sebelumnya
        }
        successDialog.show()
    }

    private fun clearForm() {
        binding.radioButtonVictim.isChecked = true
        binding.editTextName.text = null
        binding.editTextDate.text = null
        binding.editTextLocation.text = null
        binding.editTextDescription.text = null
        binding.editTextContact.text = null
        reportViewModel.setSelectedImageUri(null) // Ini akan trigger observer untuk clear ImageView
        selectedImageUriForSubmission = null
        currentPhotoPath = null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
