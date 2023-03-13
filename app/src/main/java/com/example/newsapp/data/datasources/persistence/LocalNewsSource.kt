package com.example.newsapp.data.datasources.persistence

import androidx.room.*

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey val url: String,
    val title: String,
    val publishTimeStamp: Long,
    val imageUrl: String?,
)

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArticles(articles: List<Article>)

    @Query("SELECT * FROM articles ORDER BY publishTimeStamp DESC")
    fun getAllArticles(): List<Article>

    @Query("SELECT * FROM articles WHERE url=:url")
    fun getArticle(url: String): Article?
}

@Database(version = 1, entities = [Article::class])
abstract class NewsAppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "NewsAppDatabase"
    }

    abstract fun articlesDao(): ArticleDao
}