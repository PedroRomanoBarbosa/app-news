package com.example.domain.models

typealias NewsId = String

data class News(
    val id: NewsId,
    val title: String,
    val description: String,
    val content: String,
    val imageUrl: String,
)