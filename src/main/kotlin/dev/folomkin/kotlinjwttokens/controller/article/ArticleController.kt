package dev.folomkin.kotlinjwttokens.controller.article

import dev.folomkin.kotlinjwttokens.model.Article
import dev.folomkin.kotlinjwttokens.service.ArticleService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/article")
class ArticleController(
    private val articleService: ArticleService
) {

    @GetMapping
    fun listAll(): List<ArticleResponse> =
        articleService.findAll().map { it.toResponse() }

    fun Article.toResponse(): ArticleResponse {
        return ArticleResponse(
            id = this.id,
            title = this.title,
            content = this.content
        )
    }
}