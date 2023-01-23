package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.entity.UserEntity
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.User
import io.harmny.service.user.repository.UserRepository
import io.harmny.service.user.request.UserCreateRequest
import io.harmny.service.user.request.UserUpdateRequest
import io.harmny.service.user.utils.ifLeft
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    companion object {
        private const val MAX_ALLOWED_NAME_LENGTH = 50
        private const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Za-z])(?=\\S+\$).{8,}\$"
        private val PASSWORD_REGEX = PASSWORD_PATTERN.toRegex()
    }

    fun findById(userId: String): User? {
        return userRepository.findByIdOrNull(userId)?.toModel()
    }

    fun findByEmailAndPassword(email: String, password: String): User? {
        val emailNormalised = email.trim().lowercase()

        return userRepository.findByEmail(emailNormalised)
            ?.takeIf { passwordEncoder.matches(password, it.password) }
            ?.toModel()
    }

    fun create(request: UserCreateRequest): Either<Fail, User> {
        val email = validateEmail(request.email).ifLeft { return it.left() }
        userRepository.findByEmail(email)?.also { return Fail.conflict(key = "user.with.email.exists") }
        val passwordValidated = validatePassword(request.password).ifLeft { return it.left() }

        val firstName = validateName(request.firstName).ifLeft { return it.left() }
        val lastName = request.lastName?.let { lastName -> validateName(lastName).ifLeft { return it.left() } }

        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = passwordEncoder.encode(passwordValidated),
            active = true,
        )
        return userRepository.save(user).toModel().right()
    }

    fun update(userId: String, request: UserUpdateRequest): Either<Fail, User> {
        val user = userRepository.findByIdOrNull(userId) ?: return Fail.input(key = "user.not.found")

        val firstName = validateName(request.firstName).ifLeft { return it.left() }
        val lastName = request.lastName?.let { lastName -> validateName(lastName).ifLeft { return it.left() } }

        user.firstName = firstName
        user.lastName = lastName
        return userRepository.save(user).toModel().right()
    }

    private fun validatePassword(password: String): Either<Fail, String> {
        if (password.length > 100) {
            return Fail.input(
                key = "password.invalid.length.exceeded",
                description = "Password is too long. Must not be greater than 100 characters.",
            )
        }
        if (!password.matches(PASSWORD_REGEX)) {
            return Fail.input(
                key = "password.invalid.regex",
                description = "Password validation failed. Must be at least 8 characters long. A digit and a letter must occur at least once. No whitespace allowed.",
                properties = mapOf("pattern" to PASSWORD_PATTERN),
            )
        }
        return password.right()
    }

    private fun validateEmail(email: String): Either<Fail, String> {
        return email.trim()
            .lowercase()
            .takeIf { it.length < 200 }
            ?.takeIf { EmailValidator.getInstance().isValid(email) }
            ?.right()
            ?: Fail.input("user.email.invalid")
    }

    private fun validateName(rawName: String): Either<Fail, String> {
        return rawName.trim().right()
            .flatMap { name ->
                name.takeIf { it.isNotBlank() }?.right() ?: Fail.input(key = "name.blank")
            }.flatMap { name ->
                name.takeIf { it.length < MAX_ALLOWED_NAME_LENGTH }?.right() ?: Fail.input(
                    key = "name.too.long",
                    properties = mapOf("MAX_ALLOWED_NAME_LENGTH" to MAX_ALLOWED_NAME_LENGTH.toString()),
                )
            }
    }

    private fun UserEntity.toModel(): User {
        return User(
            id = this.id,
            firstName = this.firstName,
            lastName = this.lastName,
            active = this.active,
            email = this.email,
        )
    }
}
