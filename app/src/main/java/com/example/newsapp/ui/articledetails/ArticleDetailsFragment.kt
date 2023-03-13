package com.example.newsapp.ui.articledetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.asLiveData
import coil.load
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentArticleDetailsBinding
import com.example.newsapp.ui.BaseFragment
import com.example.newsapp.viewmodels.ArticleDetailsNavigation
import com.example.newsapp.viewmodels.ArticleDetailsViewModel
import com.example.newsapp.viewmodels.ArticleDetailsViewModel.State
import com.example.newsapp.viewmodels.ScreenNavigation
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * TODO
 */
class ArticleDetailsFragment : BaseFragment<ArticleDetailsNavigation, State>(R.layout.fragment_article_details) {
    companion object {
        const val ARTICLE_ID = "ArticleDetailsFragment_articleId"

        fun newInstance(articleId: String) = ArticleDetailsFragment().apply {
            arguments = bundleOf(ARTICLE_ID to articleId)
        }
    }

    private val binding by lazy { FragmentArticleDetailsBinding.inflate(layoutInflater) }

    private val newsFeedViewModel by viewModel<ArticleDetailsViewModel> {
        parametersOf(arguments?.getString(ARTICLE_ID) ?: String())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        with(newsFeedViewModel) {
            state.asLiveData().observe(viewLifecycleOwner, ::onStateChanged)
            navigation.asLiveData().observe(viewLifecycleOwner, ::onScreenNavigation)
        }
    }

    override fun onScreenNavigation(screenNavigation: ScreenNavigation) {
        if (screenNavigation is ScreenNavigation.Back) requireActivity().finish()
    }

    override fun onStateChanged(state: State) {
        val article = state as? State.Article ?: return

        with(binding) {
            toolbar.title = article.title
            tvTitle.text = article.title
            tvDescription.text = article.description
            ivImage.load(article.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.baseline_image_24)
                error(R.drawable.baseline_error_24)
            }
        }
    }
}