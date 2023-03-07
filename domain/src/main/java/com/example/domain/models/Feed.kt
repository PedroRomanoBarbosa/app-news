package com.example.domain.models

data class Feed(val newsProvider: NewsProvider, val headlines: List<News>)