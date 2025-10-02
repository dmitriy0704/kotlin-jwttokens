package dev.folomkin.kotlinjwttokens.repository

import dev.folomkin.kotlinjwttokens.model.Article
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ArticleRepository {
    private val articles = listOf(
        Article(id = UUID.randomUUID(), title = "Article1", content = "Content1"),
        Article(id = UUID.randomUUID(), title = "Article2", content = "Content2")
    )

    fun findAll(): List<Article> = articles
}