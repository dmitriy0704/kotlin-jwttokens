package dev.folomkin.kotlinjwttokens.controller.user

import java.util.UUID

data class UserResponse(
    val uuid: UUID,
    val email: String
)