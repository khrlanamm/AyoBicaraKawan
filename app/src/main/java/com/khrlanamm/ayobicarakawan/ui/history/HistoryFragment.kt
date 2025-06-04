package com.khrlanamm.ayobicarakawan.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.khrlanamm.ayobicarakawan.R
import com.khrlanamm.ayobicarakawan.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inisialisasi ViewModel menggunakan Factory yang sudah diupdate
        val application = requireActivity().application
        val factory = HistoryViewModelFactory(application)
        historyViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            // Pertimbangkan menggunakan NavController jika Anda menggunakan Navigation Component
            // requireActivity().findNavController(R.id.nav_host_fragment_content_main).popBackStack()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.rvReportHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        historyViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Sembunyikan RecyclerView saat loading untuk menghindari tampilan data lama
            binding.rvReportHistory.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        historyViewModel.allReports.observe(viewLifecycleOwner) { reports ->
            // Dipanggil setiap kali data di database berubah
            historyAdapter.submitList(reports)
            // Panggil onDataLoaded setelah observasi pertama atau perubahan data
            // Ini akan menangani visibilitas progress bar dan toast jika kosong
            historyViewModel.onDataLoaded(reports.isNullOrEmpty())
        }

        historyViewModel.showEmptyToast.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow) {
                Toast.makeText(context, getString(R.string.no_report_data_message), Toast.LENGTH_LONG).show()
                historyViewModel.onEmptyToastShown() // Reset flag
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
