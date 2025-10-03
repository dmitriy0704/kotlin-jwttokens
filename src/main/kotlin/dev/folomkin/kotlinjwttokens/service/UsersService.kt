package dev.folomkin.kotlinjwttokens.service

import dev.folomkin.kotlinjwttokens.model.User
import dev.folomkin.kotlinjwttokens.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UsersService(private val userRepository: UserRepository) {

    fun createUser(user: User): User? {
        val found = userRepository.findByEmail(user.email)
        return if (found == null) {
            userRepository.save(user)
            user
        } else null
    }

    fun findByUUID(uuid: UUID): User? =
        userRepository.findByUUID(uuid)

    fun findByEmail(email: String): User? =
        userRepository.findByEmail(email)

    fun findAll(): List<User> =
        userRepository.findAll()

    fun deleteByUUID(uuid: UUID): Boolean =
        userRepository.deleteByUUID(uuid)
}