package com.example.domain.repositories

import com.example.domain.models.Article
import com.example.domain.models.ArticleId

sealed interface Response<T> {
    data class Success<T>(val data: T) : Response<T>
    class Error<T>: Response<T>
}

interface DomainNewsRepository {
    suspend fun loadArticles(): Response<Pair<Int, List<Article>>>

    suspend fun loadMoreArticles(page: Int): Response<Pair<Int, List<Article>>>

    suspend fun getLocalArticles(): List<Article>

    suspend fun getArticle(id: ArticleId): Article?
}