package io.harmny.service.user.web.service

import arrow.core.Either
import io.harmny.service.user.model.AuthProvider
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.Fails
import io.harmny.service.user.model.User
import io.harmny.service.user.service.UserService
import io.harmny.service.user.utils.ifLeft
import io.harmny.service.user.web.exception.AuthenticationFailedException
import io.harmny.service.user.web.model.GoogleOAuth2UserInfo
import io.harmny.service.user.web.model.OAuth2UserInfo
import io.harmny.service.user.web.model.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import javax.naming.AuthenticationException

@Service
class UserPrincipalService(
    private val userService: UserService,
    private val userManagementService: UserManagementService,
) : DefaultOAuth2UserService(), UserDetailsService {

    companion object {
        private val log = LoggerFactory.getLogger(UserPrincipalService::class.java)
    }

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        return try {
            processOAuth2User(userRequest, oAuth2User)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to load user. Reason: ${e.message}", e)
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw InternalAuthenticationServiceException(e.message, e.cause)
        }
    }

    private fun processOAuth2User(oAuth2UserRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        return defineUser(oAuth2User, oAuth2UserRequest).map { user ->
            UserPrincipal.create(user, oAuth2User.attributes)
        }.ifLeft {
            throw AuthenticationFailedException(it)
        }
    }

    private fun defineUser(
        oAuth2User: OAuth2User,
        oAuth2UserRequest: OAuth2UserRequest,
    ): Either<Fail, User> {
        val authProvider = AuthProvider.valueOf(oAuth2UserRequest.clientRegistration.registrationId)
        val oAuth2UserInfo = getOAuth2UserInfo(authProvider, oAuth2User.attributes)

        val email = oAuth2UserInfo.getEmail().takeIf { it.isNotBlank() }
            ?: return Fail.authentication("email.from.oauth2.provider.empty")

        return userManagementService.getOrCreate(email, oAuth2UserInfo, authProvider)
    }

    private fun getOAuth2UserInfo(authProvider: AuthProvider, attributes: Map<String, Any>): OAuth2UserInfo {
        return if (authProvider == AuthProvider.google) {
            GoogleOAuth2UserInfo(attributes)
        } else {
            throw AuthenticationFailedException(Fails.authentication("auth.provider.unsupported"))
        }
    }

    override fun loadUserByUsername(username: String): UserDetails {
        return userService.findByEmail(username)
            ?.let { UserPrincipal.create(it) }
            ?: throw UsernameNotFoundException("User '$username' not found.")
    }
}
