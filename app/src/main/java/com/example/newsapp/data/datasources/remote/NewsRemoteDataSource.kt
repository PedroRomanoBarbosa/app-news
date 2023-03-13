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

const val TOP_HEADLINES_END_POINT = "https://newsapi.org/v2/top-headlines"
const val PAGE_SIZE = 10

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

    suspend fun getArticles() = httpClient.get(
        //"$TOP_HEADLINES_END_POINT?sources=bbc-news&pageSize=$PAGE_SIZE"
        "$TOP_HEADLINES_END_POINT?country=us&pageSize=$PAGE_SIZE"
    )

    suspend fun getArticles(page: Int) = httpClient.get(
        //"$TOP_HEADLINES_END_POINT?sources=bbc-news&pageSize=$PAGE_SIZE&page=$page"
        "$TOP_HEADLINES_END_POINT?country=us&pageSize=$PAGE_SIZE&page=$page"
    )
}