package com.khrlanamm.ayobicarakawan.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback // Import OnBackPressedCallback
import androidx.appcompat.app.AlertDialog // Import AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.khrlanamm.ayobicarakawan.databinding.FragmentHomeBinding
import com.khrlanamm.ayobicarakawan.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khrlanamm.ayobicarakawan.ui.auth.SignInActivity // Import SignInActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var homeViewModel: HomeViewModel

    private var backPressedTime: Long = 0 // Variabel untuk menyimpan waktu penekanan tombol kembali

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        // Tampilkan nama pengguna
        displayUserName()

        // Tombol Logout
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Tombol Menu Lapor → navigasi ke ReportFragment
        binding.menuLapor.setOnClickListener {
            val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            navView.selectedItemId = R.id.navigation_report
        }

        // Tombol Menu Curhat → navigasi ke ChatFragment
        binding.menuCurhat.setOnClickListener {
            val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            navView.selectedItemId = R.id.navigation_chat
        }

        // Inisialisasi ViewModel
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Siapkan adapter dan RecyclerView
        articleAdapter = ArticleAdapter(requireContext(), emptyList())
        binding.ArticlesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ArticlesRecyclerView.adapter = articleAdapter

        // Observe LiveData artikel
        homeViewModel.articles.observe(viewLifecycleOwner) { articles ->
            articleAdapter.updateData(articles)
        }

        // Tangani tombol kembali
        setupOnBackPressedCallback()

        return root
    }

    /**
     * Menampilkan nama pengguna yang login ke TextView accountName.
     * Mengambil 2 kata pertama jika nama lebih dari 2 kata dan membatasi 20 karakter.
     */
    private fun displayUserName() {
        val sharedPreferences = requireContext().getSharedPreferences(SignInActivity.PREF_NAME, Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString(SignInActivity.KEY_USER_NAME, "Pengguna") ?: "Pengguna"

        val words = fullName.split(" ")
        val displayedName = if (words.size > 2) {
            "${words[0]} ${words[1]}"
        } else {
            fullName
        }

        val finalName = if (displayedName.length > 20) {
            displayedName.substring(0, 20) + "..." // Tambahkan elipsis jika dipotong
        } else {
            displayedName
        }

        binding.accountName.text = "Halo, $finalName!"
    }

    /**
     * Menampilkan dialog konfirmasi logout.
     */
    private fun showLogoutConfirmationDialog() {
        val sharedPreferences = requireContext().getSharedPreferences(SignInActivity.PREF_NAME, Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString(SignInActivity.KEY_USER_NAME, "Pengguna") ?: "Pengguna"

        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.abk_logo) // Pastikan abk_logo ada di drawable Anda
            .setTitle("Keluar Akun")
            .setMessage("Apakah Anda yakin akan keluar dari akun \"$userName\"? Jika keluar, Anda harus masuk kembali untuk mengakses akun ini atau akun lain. Pastikan Anda telah menyimpan semua pekerjaan atau informasi penting sebelum melanjutkan.")
            .setPositiveButton("Keluar") { dialog, _ ->
                // Lakukan logout
                SignInActivity.clearUserSession(requireContext())
                Toast.makeText(requireContext(), "Anda telah keluar", Toast.LENGTH_SHORT).show()

                // Arahkan ke SignInActivity
                val intent = Intent(requireActivity(), SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Hapus semua aktivitas sebelumnya
                startActivity(intent)
                requireActivity().finish() // Tutup MainActivity
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Menyiapkan callback untuk menangani penekanan tombol kembali.
     */
    private fun setupOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    // Jika ditekan dua kali dalam 2 detik, keluar dari aplikasi
                    requireActivity().finishAffinity() // Menutup semua aktivitas dalam task ini
                } else {
                    // Tekan pertama, tampilkan toast
                    Toast.makeText(requireContext(), "Tekan sekali lagi untuk keluar dari aplikasi", Toast.LENGTH_SHORT).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
