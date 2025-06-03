package com.khrlanamm.ayobicarakawan.ui.chat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.ayobicarakawan.ui.chat.chatdata.AppDatabase

class ChatViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            val chatDao = AppDatabase.getDatabase(application).chatDao()
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
