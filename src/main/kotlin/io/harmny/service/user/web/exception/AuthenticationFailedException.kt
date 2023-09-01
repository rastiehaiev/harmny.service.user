package io.harmny.service.user.web.exception

import io.harmny.service.user.model.Fail
import org.springframework.security.core.AuthenticationException

class AuthenticationFailedException(val fail: Fail) : AuthenticationException("Authentication failed.")
