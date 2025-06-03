package com.khrlanamm.ayobicarakawan.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khrlanamm.ayobicarakawan.databinding.ItemChatUserBinding
import com.khrlanamm.ayobicarakawan.ui.chat.chatdata.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatMessageViewHolder>(ChatMessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val binding = ItemChatUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatMessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatMessageViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            binding.textViewMessageContent.text = chatMessage.message
            binding.textViewMessageTimestamp.text = formatTimestamp(chatMessage.timestamp)
            // Karena ini dummy dan semua dari user, kita tidak perlu membedakan viewType
            // Jika ada pesan dari "TPPA", Anda akan memerlukan viewType berbeda di sini
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy - HH.mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
