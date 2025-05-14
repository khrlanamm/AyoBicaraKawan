package com.khrlanamm.ayobicarakawan.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel(helloText: String) : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = helloText
    }
    val text: LiveData<String> = _text
}
