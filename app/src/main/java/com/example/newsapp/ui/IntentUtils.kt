package com.example.newsapp.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.example.newsapp.ui.articledetails.ArticleDetailsActivity
import com.example.newsapp.ui.articledetails.ArticleDetailsFragment

fun launchArticleDetailsActivity(context: Context, id: String) {
    Intent(context, ArticleDetailsActivity::class.java).run {
        putExtra(ArticleDetailsFragment.ARTICLE_ID, id)
    }.also {
        context.startActivity(it)
    }
}

fun launchNetworkSettings(context: Context) {
    startActivity(
        context,
        Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS),
        null,
    )
}