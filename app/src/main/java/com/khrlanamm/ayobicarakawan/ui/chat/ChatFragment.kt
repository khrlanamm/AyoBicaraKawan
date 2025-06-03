package com.khrlanamm.ayobicarakawan.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.khrlanamm.ayobicarakawan.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.toolbar.setNavigationOnClickListener {
            // Cara 1: Menggunakan NavController (jika Anda menggunakan Jetpack Navigation)
            findNavController().navigateUp()

            // Cara 2: Menggunakan onBackPressedDispatcher (lebih umum jika tidak spesifik ke NavController)
            // requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        val factory = ChatViewModelFactory(requireContext())
        val chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
