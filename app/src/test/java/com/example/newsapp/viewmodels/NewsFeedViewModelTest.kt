package com.example.newsapp.viewmodels

import app.cash.turbine.test
import com.example.newsapp.networkRepositoryMocked
import com.example.newsapp.newsRepositoryMock_loadArticles
import com.example.newsapp.newsRepositoryMock_loadArticles_empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class NewsFeedViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Initial Load - empty articles`() = runTest {
        val viewModel = NewsFeedViewModel(
            newsRepositoryMock_loadArticles_empty,
            networkRepositoryMocked,
        )

        viewModel.state.test {
            with(awaitItem()) {
                val expected = NewsFeedViewModel.State(
                    loading = true,
                    rows = emptyList(),
                    networkAvailable = true,
                )
                assertEquals(expected, this)
            }

            with(awaitItem()) {
                val expected = NewsFeedViewModel.State(
                    loading = false,
                    rows = emptyList(),
                    networkAvailable = true,
                )
                assertEquals(expected, this)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Initial Load - articles`() = runTest {
        val viewModel = NewsFeedViewModel(
            newsRepositoryMock_loadArticles,
            networkRepositoryMocked,
        )

        viewModel.state.test {
            assertEquals(
                NewsFeedViewModel.State(
                    loading = true,
                    rows = emptyList(),
                    networkAvailable = true,
                ),
                awaitItem(),
            )

            val state = awaitItem()
            assertEquals(state.rows.size, 1)
        }
    }
}