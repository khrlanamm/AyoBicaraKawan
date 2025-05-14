package com.khrlanamm.ayobicarakawan.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khrlanamm.ayobicarakawan.R

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val helloText = context.getString(R.string.hello_blank_fragment)
        return HomeViewModel(helloText) as T
    }
}
