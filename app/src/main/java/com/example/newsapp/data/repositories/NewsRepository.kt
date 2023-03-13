package com.example.newsapp.data.repositories

import com.example.domain.models.Article
import com.example.domain.models.ArticleId
import com.example.domain.repositories.DomainNewsRepository
import com.example.newsapp.data.datasources.persistence.NewsAppDatabase
import com.example.newsapp.data.datasources.remote.*
import com.example.newsapp.data.datasources.persistence.Article as ArticleEntity
import com.example.newsapp.data.datasources.remote.Article as RemoteArticle
import io.ktor.client.call.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import com.example.domain.repositories.Response

fun RemoteArticle.toEntity() = ArticleEntity(
    title = title,
    url = url,
    publishTimeStamp = publishedAt.toEpochMilliseconds(),
    imageUrl = urlToImage,
)

fun RemoteArticle.toArticle() = Article(
    url = url,
    title = title,
    publishDate = publishedAt,
    imageUrl = urlToImage,
)

fun ArticleEntity.toArticle() = Article(
    title = title,
    url = url,
    publishDate = Instant.fromEpochMilliseconds(publishTimeStamp),
    imageUrl = imageUrl,
)

val OkStatusRange = 200..299

class NewsRepository(
    private val client: ApiClient,
    private val database: NewsAppDatabase,
) : DomainNewsRepository {
    override suspend fun loadArticles(): Response<Pair<Int, List<Article>>> {
        val response = runCatching {
            client.getArticles()
        }.getOrNull() ?: run {
            // TODO

            return Response.Error()
        }

        return when (response.status.value) {
            in OkStatusRange -> {
                val body = response.body<ArticlesResponse>()

                val (articles, entities) = body
                    .articles.map { it.toArticle() to it.toEntity() }
                    .unzip()

                withContext(Dispatchers.IO) {
                    database.articlesDao().insertArticles(entities)
                }

                Response.Success(data = body.totalResults to articles)
            }
            else -> Response.Error()
        }
    }

    override suspend fun loadMoreArticles(page: Int): Response<Pair<Int, List<Article>>> {
        val response = runCatching {
            client.getArticles(page)
        }.getOrNull() ?: run {
            // TODO

            return Response.Error()
        }

        return when (response.status.value) {
            in OkStatusRange -> {
                val body = response.body<ArticlesResponse>()

                val (articles, entities) = body
                    .articles.map { it.toArticle() to it.toEntity() }
                    .unzip()

                withContext(Dispatchers.IO) {
                    database.articlesDao().insertArticles(entities)
                }

                Response.Success(data = body.totalResults to articles)
            }
            else -> Response.Error()
        }
    }

    override suspend fun getLocalArticles() = withContext(Dispatchers.IO) {
        database
            .articlesDao()
            .getAllArticles()
            .map { it.toArticle() }
    }

    override suspend fun getArticle(id: ArticleId) = withContext(Dispatchers.IO) {
        database
            .articlesDao()
            .getArticle(id)
            ?.toArticle()
    }
}