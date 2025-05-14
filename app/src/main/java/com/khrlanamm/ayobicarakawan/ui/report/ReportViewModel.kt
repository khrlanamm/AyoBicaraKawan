package com.khrlanamm.ayobicarakawan.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReportViewModel(helloText: String) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = helloText
    }
    val text: LiveData<String> = _text
}
