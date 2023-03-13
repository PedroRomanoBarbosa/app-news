package com.example.newsapp.ui.articledetails

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivityArticleDetailsBinding

/**
 * TODO
 */
class ArticleDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityArticleDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val articleId = intent.extras?.getString(ArticleDetailsFragment.ARTICLE_ID) ?: String()

        val fragment = ArticleDetailsFragment.newInstance(articleId)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}