package com.example.newsapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.models.Article
import com.example.domain.repositories.DomainNewsRepository
import com.example.domain.repositories.Response
import com.example.newsapp.INetworkRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

/**
 * Represents an event issued by [NewsFeedViewModel]
 */
enum class Event {
    LoadFailed,
    LoadMoreFailed,
    RefreshFailed,
    StopRefresh,
}

/**
 * News feed flow navigation
 */
sealed interface NewsFeedNavigation : ScreenNavigation {
    data class ArticleDetails(val id: String): NewsFeedNavigation
    object NetworkSettings: NewsFeedNavigation
}

/**
 * View model responsible for the article feed flow
 */
class NewsFeedViewModel(
    private val newsRepository: DomainNewsRepository,
    networkRepository: INetworkRepository,
) : ViewModel() {
    companion object {
        const val TAG = "NewsFeedViewModel"
    }

    private var total = 0
    private var page = 1
    private var localArticlesJob: Job? = null
    private var loadedRemoteArticles = false
    private var articleMap: Map<String, Article> = emptyMap()

    private val _navigation = MutableSharedFlow<NewsFeedNavigation>()
    val navigation = _navigation.asSharedFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    private val _state = MutableStateFlow(
        State(networkAvailable = networkRepository.networkAvailable.value)
    )
    val state = _state.asStateFlow()

    init {
        Log.d(TAG, "init")

        getLocalArticles()

        networkRepository.networkAvailable.onEach { networkAvailable ->
            _state.update { it.copy(networkAvailable = networkAvailable) }

            if (networkAvailable && !loadedRemoteArticles) {
                loadRemoteArticles()
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            loadRemoteArticles()
        }
    }

    private fun getLocalArticles() {
        Log.d(TAG, "Fetching local articles")

        localArticlesJob = viewModelScope.launch {
            val articles = newsRepository.getLocalArticles()

            _state.update { state ->
                state.copy(rows = articles.associateBy { it.url }.values.map { it.toRow() })
            }
        }
    }

    private fun loadRemoteArticles() {
        Log.d(TAG, "Load remote articles")

        _state.update { it.copy(loading = true) }

        viewModelScope.launch {
            when (val response = newsRepository.loadArticles()) {
                is Response.Success -> {
                    localArticlesJob?.cancel()

                    val (totalResults, articles) = response.data

                    loadedRemoteArticles = true
                    total = totalResults
                    articleMap = articles.associateBy { it.url }

                    val canLoadMore = totalResults > articleMap.size
                    val articleRows = articleMap.values.toList().map { it.toRow() }

                    _state.update {
                        it.copy(
                            rows = articleRows + if (canLoadMore) listOf(Row.LoadingRow) else emptyList(),
                            loading = false,
                        )
                    }
                }
                is Response.Error -> {
                    _event.emit(Event.LoadFailed)

                    _state.update { it.copy(loading = false) }
                }
            }
        }
    }

    fun refresh() {
        Log.d(TAG, "Refreshing feed")

        _state.update { it.copy(refreshing = true) }

        viewModelScope.launch {
            when (val response = newsRepository.loadArticles()) {
                is Response.Success -> {
                    localArticlesJob?.cancel()

                    val (totalResults, articles) = response.data

                    total = totalResults
                    articleMap = articleMap + articles.associateBy { it.url }

                    val canLoadMore = totalResults > articleMap.size
                    val articleRows = articleMap.values.map { it.toRow() }

                    _state.update {
                        it.copy(
                            rows = articleRows + if (canLoadMore) listOf(Row.LoadingRow) else emptyList(),
                            refreshing = false,
                        )
                    }
                    _event.emit(Event.StopRefresh)
                }
                is Response.Error -> {
                    _event.emit(Event.RefreshFailed)
                }
            }
        }
    }

    fun loadMore() {
        Log.d(TAG, "Loading more articles")

        if (_state.value.rows.size >= total || _state.value.loadingMore) {
            Log.d(TAG, "Cannot load more articles")

            return
        }

        _state.update { it.copy(loadingMore = true) }

        viewModelScope.launch {
            when (val response = newsRepository.loadMoreArticles(page + 1)) {
                is Response.Success -> {
                    val (totalResults, articles) = response.data

                    page++
                    total = totalResults
                    articleMap = articleMap + articles.associateBy { it.url }

                    val canLoadMore = totalResults > articleMap.size
                    val articleRows = articleMap.values.map { it.toRow() }

                    _state.update {
                        it.copy(rows = articleRows + if (canLoadMore) listOf(Row.LoadingRow) else emptyList())
                    }
                }
                is Response.Error -> {
                    _event.emit(Event.LoadMoreFailed)
                }
            }

            _state.update { it.copy(loadingMore = false) }
        }
    }

    fun onHeadlineClick(id: String) {
        Log.d(TAG, "headline with id=$id selected")

        viewModelScope.launch {
            _navigation.emit(NewsFeedNavigation.ArticleDetails(id))
        }
    }

    fun onGoToNetworkSettings() {
        Log.d(TAG, "navigating to network settings")

        viewModelScope.launch {
            _navigation.emit(NewsFeedNavigation.NetworkSettings)
        }
    }

    fun retry() {
        loadRemoteArticles()
    }

    private fun Article.toRow() = Row.ArticleRow(
        url = url,
        title = title,
        date = publishDate,
        imageUrl = imageUrl,
    )

    /**
     * State representing the feed screen
     */
    data class State(
        val loading: Boolean = false,
        val refreshing: Boolean = false,
        val loadingMore: Boolean = false,
        val rows: List<Row> = emptyList(),
        val networkAvailable: Boolean,
    )

    sealed interface Row {
        data class ArticleRow(
            val url: String,
            val title: String,
            val date: Instant,
            val imageUrl: String? = null,
        ) : Row
        object LoadingRow : Row
    }
}