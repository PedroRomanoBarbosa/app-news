package com.example.newsapp

import com.example.newsapp.data.datasources.remote.ArticlesResponse
import com.example.newsapp.data.datasources.remote.Response
import io.ktor.client.call.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GetArticlesTest {
    @Test
    fun getArticles_OkTest() {
        val client = createMockClient(emptyArticlesJson)

        runBlocking {
            val response = client.getArticles()

            when (response.status.value) {
                in 200..299 -> {
                    val body = response.body<Response>()

                    assert(body is ArticlesResponse)
                }
                else -> assert(false)
            }
        }
    }

    @Test
    fun getArticles_ErrorTest() {
        val client = createMockClient(errorBody)

        runBlocking {
            val response = client.getArticles()

            when (response.status.value) {
                in 200..299 -> {
                    val body = response.body<Response>()

                    assert(body is Response.Error)
                }
                else -> assert(false)
            }
        }
    }
}