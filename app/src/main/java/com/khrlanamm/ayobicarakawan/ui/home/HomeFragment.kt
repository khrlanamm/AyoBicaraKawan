package com.khrlanamm.ayobicarakawan.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.khrlanamm.ayobicarakawan.databinding.FragmentHomeBinding
import com.khrlanamm.ayobicarakawan.R
import com.google.android.material.bottomnavigation.BottomNavigationView



class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        // Tombol Account
        binding.accountButton.setOnClickListener {
            Toast.makeText(requireContext(), "Sedang dalam pengembangan", Toast.LENGTH_SHORT).show()
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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
