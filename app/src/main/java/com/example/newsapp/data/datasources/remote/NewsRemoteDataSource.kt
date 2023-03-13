package com.example.newsapp.data.datasources.remote

import com.example.newsapp.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Article(val url: String, val title: String, val publishedAt: Instant, val urlToImage: String?) // TODO

@Serializable
sealed interface Response {
    @Serializable
    @SerialName("ok")
    sealed interface Success : Response

    @Serializable
    @SerialName("error")
    data class Error(val code: String, val message: String) : Response
}

@Serializable
@SerialName("ok")
data class ArticlesResponse(val totalResults: Int, val articles: List<Article>) : Response.Success

private const val TOP_HEADLINES_END_POINT = "https://newsapi.org/v2/top-headlines"
private const val SOURCE = BuildConfig.SOURCE
private const val COUNTRY = BuildConfig.COUNTRY

/**
 * The number of articles by page
 */
const val PAGE_SIZE = 10

/**
 * Client that is responsible for making remote calls to the api
 */
class ApiClient(engine: HttpClientEngine) {
    private val httpClient = HttpClient(engine) {
        defaultRequest {
            headers {
                append("X-Api-Key", BuildConfig.NEWS_API_KEY)
            }
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    classDiscriminator = "status"
                }
            )
        }
    }

    suspend fun getArticles(): HttpResponse {
        val url = when {
            COUNTRY.isEmpty() -> "$TOP_HEADLINES_END_POINT?sources=$SOURCE&pageSize=$PAGE_SIZE"
            else -> "$TOP_HEADLINES_END_POINT?country=$COUNTRY&pageSize=$PAGE_SIZE"
        }

        return httpClient.get(url)
    }

    suspend fun getArticles(page: Int): HttpResponse {
        val url = when {
            COUNTRY.isEmpty() -> "$TOP_HEADLINES_END_POINT?sources=$SOURCE&pageSize=$PAGE_SIZE&page=$page"
            else -> "$TOP_HEADLINES_END_POINT?country=$COUNTRY&pageSize=$PAGE_SIZE&page=$page"
        }

        return httpClient.get(url)
    }
}