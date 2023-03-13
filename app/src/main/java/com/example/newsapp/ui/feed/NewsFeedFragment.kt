package com.example.newsapp.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentNewsFeedBinding
import com.example.newsapp.ui.BaseFragment
import com.example.newsapp.ui.launchArticleDetailsActivity
import com.example.newsapp.ui.launchNetworkSettings
import com.example.newsapp.viewmodels.*
import com.example.newsapp.viewmodels.NewsFeedViewModel.State
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Converts the current [Event] into a string id
 */
fun Event.toTextId() = when (this) {
    Event.LoadFailed -> R.string.load_failed
    Event.RefreshFailed -> R.string.refresh_failed
    Event.LoadMoreFailed -> R.string.load_more_failed
    Event.StopRefresh -> throw java.lang.IllegalArgumentException("Invalid event type for event=$this")
}

/**
 * Fragment for the news feed
 */
class NewsFeedFragment : BaseFragment<NewsFeedNavigation, State>(R.layout.fragment_news_feed) {
    private val binding by lazy { FragmentNewsFeedBinding.inflate(layoutInflater) }
    private val newsListAdapter by lazy {
        NewsListAdapter(
            onHeadlineClick = {
                newsFeedViewModel.onHeadlineClick(it)
            }
        )
    }

    private val newsFeedViewModel by viewModel<NewsFeedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
    }

    private fun setupUi() {
        with(binding) {
            with(rvNewsList) {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = newsListAdapter
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                        if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1){
                            newsFeedViewModel.loadMore()
                        }
                    }
                })
            }
            srLayout.setOnRefreshListener {
                newsFeedViewModel.refresh()
            }
            btnRetry.setOnClickListener {
                newsFeedViewModel.retry()
            }
            btnGoToNetworkSettings.setOnClickListener {
                newsFeedViewModel.onGoToNetworkSettings()
            }
        }

        with(newsFeedViewModel) {
            state.asLiveData().observe(viewLifecycleOwner, ::onStateChanged)
            navigation.asLiveData().observe(viewLifecycleOwner, ::onScreenNavigation)
            event.asLiveData().observe(viewLifecycleOwner, ::onEvent)
        }
    }

    override fun onScreenNavigation(screenNavigation: ScreenNavigation) {
        when (screenNavigation) {
            is NewsFeedNavigation.ArticleDetails -> {
                launchArticleDetailsActivity(requireContext(), screenNavigation.id)
            }
            NewsFeedNavigation.NetworkSettings -> {
                launchNetworkSettings(requireContext())
            }
            else -> throw IllegalArgumentException("Invalid navigation for this screenNavigation=$screenNavigation")
        }
    }

    private fun onEvent(event: Event) {
        when(event) {
            Event.LoadFailed,
            Event.RefreshFailed,
            Event.LoadMoreFailed -> {
                Snackbar.make(binding.srLayout, event.toTextId(), Snackbar.LENGTH_LONG).show()
            }
            Event.StopRefresh -> {
                binding.srLayout.isRefreshing = false
            }
        }
    }

    override fun onStateChanged(state: State) = with(binding) {
        newsListAdapter.submitList(state.rows)

        when {
            state.loading -> {
                cpiLoading.isVisible = true
                gpEmpty.isVisible = false
                gpNoNetwork.isVisible = false
                clArticleSection.isVisible = false
            }

            state.rows.isEmpty() -> {
                cpiLoading.isVisible = false

                if(state.networkAvailable) {
                    gpEmpty.isVisible = true
                    clArticleSection.isVisible = false
                    gpNoNetwork.isVisible = false
                }
                else {
                    gpEmpty.isVisible = false
                    clArticleSection.isVisible = false
                    gpNoNetwork.isVisible = true
                }
            }
            else -> {
                cpiLoading.isVisible = false
                gpEmpty.isVisible = false
                clArticleSection.isVisible = true

                vOverlay.isVisible = state.refreshing

                if (state.networkAvailable) {
                    tvBanner.isVisible = false
                    srLayout.isEnabled = true
                    btnRetry.isEnabled = true
                } else {
                    srLayout.isEnabled = false
                    tvBanner.isVisible = true
                    btnRetry.isEnabled = false
                }
            }
        }
    }
}