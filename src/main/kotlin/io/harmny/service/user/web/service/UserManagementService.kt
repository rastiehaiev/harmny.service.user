package io.harmny.service.user.web.service

import arrow.core.Either
import io.harmny.service.user.model.AuthProvider
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.User
import io.harmny.service.user.service.CreateUserRequest
import io.harmny.service.user.service.UserService
import io.harmny.service.user.web.model.OAuth2UserInfo
import io.harmny.service.user.web.model.request.UserUpdateRequest
import org.springframework.stereotype.Service

@Service
class UserManagementService(
    private val userService: UserService,
) {

    fun getOrCreate(
        email: String,
        userInfo: OAuth2UserInfo,
        authProvider: AuthProvider,
    ): Either<Fail, User> {
        val user = userService.findByEmail(email)
        return if (user == null) {
            registerNewUser(authProvider, userInfo)
        } else {
            if (user.authProvider != authProvider) {
                Fail.authentication(
                    key = "auth.provider.invalid",
                    properties = mapOf(
                        "provider.attempted" to authProvider,
                        "provider.user" to user.authProvider,
                    )
                )
            } else {
                updateExistingUser(user, userInfo)
            }
        }
    }

    private fun registerNewUser(
        authProvider: AuthProvider,
        oAuth2UserInfo: OAuth2UserInfo,
    ): Either<Fail, User> {
        val (firstName, lastName) = getFirstAndLastNames(oAuth2UserInfo.getName())
        val userCreateRequest = CreateUserRequest(
            firstName = firstName,
            lastName = lastName,
            email = oAuth2UserInfo.getEmail(),
            authProvider = authProvider,
            profilePhotoUrl = oAuth2UserInfo.getImageUrl(),
        )
        return userService.create(userCreateRequest)
    }

    private fun updateExistingUser(
        existingUser: User,
        oAuth2UserInfo: OAuth2UserInfo,
    ): Either<Fail, User> {
        val (firstName, lastName) = getFirstAndLastNames(oAuth2UserInfo.getName())
        val request = UserUpdateRequest(firstName, lastName, imageUrl = oAuth2UserInfo.getImageUrl())
        return userService.update(existingUser.id, request)
    }

    private fun getFirstAndLastNames(name: String): Pair<String, String?> {
        return name.split(" ").let {
            if (it.size > 1) it[0] to it[1] else it[0] to null
        }
    }
}
