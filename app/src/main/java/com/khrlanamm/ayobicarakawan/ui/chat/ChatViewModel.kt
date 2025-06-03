package com.khrlanamm.ayobicarakawan.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khrlanamm.ayobicarakawan.ui.chat.chatdata.ChatDao
import com.khrlanamm.ayobicarakawan.ui.chat.chatdata.ChatMessage
import kotlinx.coroutines.launch

class ChatViewModel(private val chatDao: ChatDao) : ViewModel() {

    val allMessages: LiveData<List<ChatMessage>> = chatDao.getAllMessages()

    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            val newMessage = ChatMessage(
                message = messageText,
                timestamp = System.currentTimeMillis(),
                isSentByUser = true // Semua pesan dianggap dari user untuk dummy ini
            )
            chatDao.insertMessage(newMessage)
        }
    }

    // Opsional: fungsi untuk menghapus semua chat
    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearAllMessages()
        }
    }
}
