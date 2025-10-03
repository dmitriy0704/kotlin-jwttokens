package dev.folomkin.kotlinjwttokens.model

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val password: String,
    val roles: Role
)

enum class Role {
    ADMIN, USER
}
