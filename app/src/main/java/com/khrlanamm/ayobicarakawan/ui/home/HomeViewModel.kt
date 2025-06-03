package com.khrlanamm.ayobicarakawan.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.khrlanamm.ayobicarakawan.R

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    val articleTitles: Array<String> =
        context.resources.getStringArray(R.array.tempdata_article_title)
    val articleDescriptions: Array<String> =
        context.resources.getStringArray(R.array.tempdata_article_description)
    val articleUrls: Array<String> =
        context.resources.getStringArray(R.array.tempdata_article_url)
    val articleImages: IntArray =
        context.resources.obtainTypedArray(R.array.tempdata_article_image).let {
            IntArray(it.length()) { index -> it.getResourceId(index, -1) }
        }

    // LiveData daftar artikel
    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> get() = _articles

    init {
        val data = mutableListOf<Article>()
        for (i in articleTitles.indices) {
            data.add(
                Article(
                    title = articleTitles[i],
                    description = articleDescriptions[i],
                    image = articleImages[i],
                    url = articleUrls[i]
                )
            )
        }
        _articles.value = data
    }
}
