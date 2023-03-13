package com.example.newsapp.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.newsapp.R
import com.example.newsapp.databinding.ItemArticleHeadlineBinding
import com.example.newsapp.databinding.ItemLoadingBinding
import com.example.newsapp.viewmodels.NewsFeedViewModel

class NewsListAdapter(
    private val onHeadlineClick: (id: String) -> Unit,
) : ListAdapter<NewsFeedViewModel.Row, RecyclerView.ViewHolder>(
    ArticleDiffCallback()
) {
    enum class ViewType {
        Article,
        Loading,
    }

    class LoadingViewHolder(binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)

    class ArticleViewHolder(internal val binding: ItemArticleHeadlineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(article: NewsFeedViewModel.Row.ArticleRow) {
            binding.tvTitle.text = article.title

            article.imageUrl?.let {
                binding.ivHeadlineImage.isVisible = true
                binding.ivHeadlineImage.load(it) {
                    crossfade(true)
                    placeholder(R.drawable.baseline_image_24)
                    error(R.drawable.baseline_error_24)
                }
            } ?: run {
                binding.ivHeadlineImage.isVisible = false
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is NewsFeedViewModel.Row.ArticleRow -> ViewType.Article.ordinal
            is NewsFeedViewModel.Row.LoadingRow -> ViewType.Loading.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ViewType.Article.ordinal -> ArticleViewHolder(
                ItemArticleHeadlineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ).apply {
                binding.cvHeadline.setOnClickListener {
                    val articleRow = getItem(bindingAdapterPosition) as NewsFeedViewModel.Row.ArticleRow

                    onHeadlineClick(articleRow.url)
                }
            }

            ViewType.Loading.ordinal -> LoadingViewHolder(
                ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)

)
            else -> throw java.lang.IllegalArgumentException("Invalid view type for viewType=$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            ViewType.Article.ordinal -> {
                val articleRow = getItem(position) as NewsFeedViewModel.Row.ArticleRow

                (holder as ArticleViewHolder).onBind(articleRow)
            }
        }
    }
}

class ArticleDiffCallback : DiffUtil.ItemCallback<NewsFeedViewModel.Row>() {
    override fun areItemsTheSame(oldItem: NewsFeedViewModel.Row, newItem: NewsFeedViewModel.Row) = when {
        oldItem is NewsFeedViewModel.Row.ArticleRow && newItem is NewsFeedViewModel.Row.ArticleRow -> oldItem.url == newItem.url

        else -> oldItem is NewsFeedViewModel.Row.LoadingRow && newItem is NewsFeedViewModel.Row.LoadingRow
    }

    override fun areContentsTheSame(oldItem: NewsFeedViewModel.Row, newItem: NewsFeedViewModel.Row) = when {
        oldItem is NewsFeedViewModel.Row.ArticleRow && newItem is NewsFeedViewModel.Row.ArticleRow -> oldItem.url == newItem.url &&
                oldItem.title == newItem.title &&
                oldItem.date == newItem.date &&
                oldItem.imageUrl == newItem.imageUrl

        else -> oldItem is NewsFeedViewModel.Row.LoadingRow && newItem is NewsFeedViewModel.Row.LoadingRow
    }
}