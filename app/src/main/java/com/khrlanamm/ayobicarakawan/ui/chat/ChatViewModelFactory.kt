package com.khrlanamm.ayobicarakawan.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.ayobicarakawan.R

class ChatViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val helloText = context.getString(R.string.hello_blank_fragment)
        return ChatViewModel(helloText) as T
    }
}
