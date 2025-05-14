package com.khrlanamm.ayobicarakawan.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryViewModel(helloText: String) : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = helloText
    }
    val text: LiveData<String> = _text
}
