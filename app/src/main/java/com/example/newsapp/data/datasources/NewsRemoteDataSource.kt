package com.example.newsapp.data.repositories.services

data class Article(val title: String) // TODO

data class TopHeadlineResponse(val articles: List<Article>)

typealias Source = String

interface NewsRemoteDataSource {
    fun getTopHeadLines(source: Source): TopHeadlineResponse
}