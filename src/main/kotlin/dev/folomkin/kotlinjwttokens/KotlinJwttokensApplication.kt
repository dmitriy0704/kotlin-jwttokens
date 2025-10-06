package dev.folomkin.kotlinjwttokens

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinJwttokensApplication

fun main(args: Array<String>) {
    runApplication<KotlinJwttokensApplication>(*args)
}
