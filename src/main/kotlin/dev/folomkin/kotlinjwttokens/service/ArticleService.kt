package dev.folomkin.kotlinjwttokens.service

import dev.folomkin.kotlinjwttokens.model.Article
import dev.folomkin.kotlinjwttokens.repository.ArticleRepository
import org.springframework.stereotype.Service

@Service
class ArticleService(
    private val repository: ArticleRepository
) {
    fun findAll(): List<Article> = repository.findAll()
}