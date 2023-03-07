package com.example.domain.repositories

import com.example.domain.models.Feed
import com.example.domain.models.News

interface INewsRepository {
    fun getFeed(): Feed

    fun getNews(id: String): News
}