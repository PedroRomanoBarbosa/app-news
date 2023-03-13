package com.example.newsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repositories.DomainNewsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ArticleDetailsNavigation : ScreenNavigation

class ArticleDetailsViewModel(
    articleId: String,
    newsRepository: DomainNewsRepository,
) : ViewModel() {

    private val _navigation = MutableSharedFlow<ScreenNavigation>()
    val navigation = _navigation.asSharedFlow()

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val article = newsRepository.getArticle(articleId) ?: run {
                // TODO

                return@launch
            }

            _state.update { State.Article(
                title = article.title,
                description = article.description ?: String(),
                imageUrl = article.imageUrl ?: String(),
            ) }
        }
    }

    fun onBack() {
        viewModelScope.launch {
            _navigation.emit(ScreenNavigation.Back)
        }
    }

    sealed interface State {
        object Loading: State
        data class Article(
            val title: String,
            val description: String,
            val imageUrl: String,
        ) : State
    }
}