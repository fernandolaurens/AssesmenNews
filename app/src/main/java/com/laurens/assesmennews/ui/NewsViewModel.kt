package com.laurens.assesmennews.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.laurens.assesmennews.data.local.entity.NewsEntity
import com.laurens.assesmennews.data.local.room.AppDatabase
import com.laurens.assesmennews.data.local.room.RecentSearchEntity
import com.laurens.assesmennews.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val _newsList = MutableLiveData<List<NewsEntity>>()
    val newsList: LiveData<List<NewsEntity>> get() = _newsList

    private val _newsSites = MutableLiveData<List<String>>()
    val newsSites: LiveData<List<String>> get() = _newsSites

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    val recentSearches: LiveData<List<RecentSearchEntity>>

    private var currentNewsSite: String? = null

    private val recentSearchDao by lazy {
        Room.databaseBuilder(application, AppDatabase::class.java, "news_app.db")
            .fallbackToDestructiveMigration()
            .build()
            .recentSearchDao()
    }

    init {
        recentSearches = recentSearchDao.getRecentSearches()
        fetchNews()
        fetchNewsSites()
    }

    fun fetchNews(newsSite: String? = null) {
        currentNewsSite = newsSite
        _isLoading.value = true
        viewModelScope.launch {
            val apiService = ApiConfig.getApiService()
            try {
                val response = apiService.getNews(null, newsSite)
                if (response.isSuccessful) {
                    response.body()?.results?.let { articles ->
                        val newsEntities = articles.map {
                            NewsEntity(
                                id = it?.id ?: 0,
                                title = it?.title.orEmpty(),
                                summary = it?.summary.orEmpty(),
                                publishedAt = it?.publishedAt.orEmpty(),
                                url = it?.url.orEmpty(),
                                urlToImage = it?.imageUrl.orEmpty()
                            )
                        }
                        _newsList.postValue(newsEntities)
                    }
                } else {
                    // Handle unsuccessful response, log error or show message
                }
            } catch (e: Exception) {
                // Handle the exception, log error or show message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchNews(title: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            val apiService = ApiConfig.getApiService()
            try {
                val response = apiService.getNews(title, currentNewsSite)
                if (response.isSuccessful) {
                    response.body()?.results?.let { articles ->
                        val newsEntities = articles.map {
                            NewsEntity(
                                id = it?.id ?: 0,
                                title = it?.title.orEmpty(),
                                summary = it?.summary.orEmpty(),
                                publishedAt = it?.publishedAt.orEmpty(),
                                url = it?.url.orEmpty(),
                                urlToImage = it?.imageUrl.orEmpty()
                            )
                        }
                        _newsList.postValue(newsEntities)
                    }
                } else {
                    // Handle unsuccessful response, log error or show message
                }
                saveRecentSearch(title)
            } catch (e: Exception) {
                // Handle the exception, log error or show message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        fetchNews(currentNewsSite)
    }

    private fun saveRecentSearch(query: String?) {
        if (query != null && query.isNotEmpty()) {
            viewModelScope.launch {
                recentSearchDao.insertRecentSearch(RecentSearchEntity(query = query))
            }
        }
    }

    private fun fetchNewsSites() {
        viewModelScope.launch {
            val apiService = ApiConfig.getApiService()
            try {
                val response = apiService.getNewsSites()
                if (response.isSuccessful) {
                    _newsSites.postValue(response.body())
                } else {
                    // Handle unsuccessful response, log error or show message
                }
            } catch (e: Exception) {
                // Handle the exception, log error or show message
            }
        }
    }
}