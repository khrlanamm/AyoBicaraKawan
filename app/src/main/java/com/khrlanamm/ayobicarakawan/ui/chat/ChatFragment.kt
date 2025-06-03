package com.khrlanamm.ayobicarakawan.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.khrlanamm.ayobicarakawan.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup ViewModel
        val factory = ChatViewModelFactory(requireActivity().application)
        chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        // Setup Toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Setup RecyclerView
        setupRecyclerView()

        // Setup Send Button
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                chatViewModel.sendMessage(messageText)
                binding.editTextMessage.text.clear()
            }
        }

        // Observe chat messages
        chatViewModel.allMessages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages) {
                // Scroll ke posisi terakhir setelah daftar diperbarui
                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        return root
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Pesan baru muncul dari bawah dan scroll ke atas
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
