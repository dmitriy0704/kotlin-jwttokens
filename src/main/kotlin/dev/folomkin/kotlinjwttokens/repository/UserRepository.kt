package dev.folomkin.kotlinjwttokens.repository

import dev.folomkin.kotlinjwttokens.model.Role
import dev.folomkin.kotlinjwttokens.model.User
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository {

    val users: MutableList<User> = mutableListOf(
        User(
            id = UUID.randomUUID(),
            email = "email-1@gmail.com",
            password = "pass1",
            roles = Role.USER
        ),
        User(
            id = UUID.randomUUID(),
            email = "email-2@gmail.com",
            password = "pass2",
            roles = Role.ADMIN
        ),
        User(
            id = UUID.randomUUID(),
            email = "email-3@gmail.com",
            password = "pass3",
            roles = Role.USER
        )
    )

    fun save(user: User): Boolean = users.add(user)

    fun findByEmail(email: String): User? =
        users.firstOrNull { it.email == email }

    fun findAll(): List<User> = users.toList()

    fun findByUUID(uuid: UUID): User? = users.find { it.id == uuid }

    fun deleteByUUID(uuid: UUID): Boolean {
        val foundUser = findByUUID(uuid)

        return foundUser?.let {
            users.remove(it)
        } ?: false
    }
}