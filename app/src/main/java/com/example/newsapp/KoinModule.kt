package com.example.newsapp

import androidx.room.Room
import com.example.domain.repositories.DomainNewsRepository
import com.example.newsapp.data.datasources.persistence.NewsAppDatabase
import com.example.newsapp.data.datasources.remote.ApiClient
import com.example.newsapp.data.repositories.NewsRepository
import com.example.newsapp.viewmodels.NewsFeedViewModel
import com.example.newsapp.viewmodels.ArticleDetailsViewModel
import io.ktor.client.engine.android.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val startModule = module(createdAtStart = true) {
    single { NetworkRepository(androidContext(), CoroutineScope(Dispatchers.Default)) } bind INetworkRepository::class
    single {
        Room.databaseBuilder(
            androidApplication(),
            NewsAppDatabase::class.java,
            NewsAppDatabase.DATABASE_NAME,
        ).build()
    }
    single { ApiClient(AndroidClientEngine(AndroidEngineConfig())) }
}

val appModule = module {
    singleOf(::NewsRepository) bind DomainNewsRepository::class

    viewModelOf(::NewsFeedViewModel)
    viewModel { parameters -> ArticleDetailsViewModel(parameters.get(), get()) }
}