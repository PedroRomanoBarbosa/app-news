package com.example.domain.models

import kotlinx.datetime.Instant

typealias ArticleId = String

/**
 * TODO
 */
data class Article(
    val url: ArticleId,
    val title: String,
    val publishDate: Instant,
    val description: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
)