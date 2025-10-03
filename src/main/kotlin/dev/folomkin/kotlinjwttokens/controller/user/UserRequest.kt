package dev.folomkin.kotlinjwttokens.controller.user

data class UserRequest(
    val email: String,
    val password: String
)