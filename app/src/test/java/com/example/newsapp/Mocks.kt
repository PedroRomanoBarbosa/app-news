package com.example.newsapp

import com.example.domain.models.Article
import com.example.domain.models.ArticleId
import com.example.domain.repositories.DomainNewsRepository
import com.example.domain.repositories.Response
import com.example.newsapp.data.datasources.remote.ApiClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

val emptySuccessResponse: Response<Pair<Int, List<Article>>> = Response.Success(0 to emptyList())

val successResponse: Response<Pair<Int, List<Article>>> = Response.Success(
    1 to listOf(
        Article(
            url = "",
            title = "",
            publishDate = Clock.System.now(),
        )
    )
)

val networkRepositoryMocked = object : INetworkRepository {
    override val networkAvailable = MutableStateFlow(true)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)

            networkAvailable.value = false

            delay(1000)

            networkAvailable.value = true
        }
    }
}

val newsRepositoryMock_loadArticles_empty = object : DomainNewsRepository {
    override suspend fun loadArticles(): Response<Pair<Int, List<Article>>> {
        return emptySuccessResponse
    }

    override suspend fun loadMoreArticles(page: Int): Response<Pair<Int, List<Article>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalArticles(): List<Article> {
        TODO("Not yet implemented")
    }

    override suspend fun getArticle(id: ArticleId): Article? {
        TODO("Not yet implemented")
    }
}

val newsRepositoryMock_loadArticles = object : DomainNewsRepository {
    override suspend fun loadArticles(): Response<Pair<Int, List<Article>>> {
        return successResponse
    }

    override suspend fun loadMoreArticles(page: Int): Response<Pair<Int, List<Article>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalArticles(): List<Article> {
        TODO("Not yet implemented")
    }

    override suspend fun getArticle(id: ArticleId): Article? {
        TODO("Not yet implemented")
    }
}

fun createMockClient(json: String, status: HttpStatusCode = HttpStatusCode.OK) = ApiClient(
    MockEngine {
        respond(
            content = ByteReadChannel(json),
            status = status,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
)

const val emptyArticlesJson = """
        {
            "status": "ok",
            "totalResults": 0,
            "articles": []
        }
    """

const val errorBody = """
        {
            "status": "error",
            "code": "code",
            "message": "message"
        }
    """