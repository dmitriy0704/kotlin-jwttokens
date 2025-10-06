package dev.folomkin.kotlinjwttokens.controller.user

import dev.folomkin.kotlinjwttokens.model.Role
import dev.folomkin.kotlinjwttokens.model.User
import dev.folomkin.kotlinjwttokens.service.UsersService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/user")
class UserController(val usersService: UsersService) {

    @PostMapping
    fun create(@RequestBody userRequest: UserRequest): UserResponse =
        usersService.createUser(user = userRequest.toModel())?.toResponse()
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Can`t create a user"
            )

    @GetMapping
    fun findAll(): List<UserResponse> =
        usersService.findAll().map { it.toResponse() }

    @GetMapping("/{uuid}")
    fun findByUUID(@PathVariable uuid: UUID): UserResponse =
        usersService.findByUUID(uuid)?.toResponse()
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Can`t found a user"
            )

    @DeleteMapping("/{uuid}")
    fun deleteByUUID(@PathVariable uuid: UUID): ResponseEntity<Boolean> {
        val success = usersService.deleteByUUID(uuid)
        return if (success) ResponseEntity.noContent().build()
        else
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Can`t found a user"
            )
    }

    fun UserRequest.toModel(): User =
        User(
            id = UUID.randomUUID(),
            email = this.email,
            password = this.password,
            role = Role.USER
        )

    fun User.toResponse(): UserResponse = UserResponse(
        uuid = this.id,
        email = this.email,
    )
}