package io.harmny.service.user.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class HarmnyUserServiceConfiguration {

    @Bean
    fun passwordEncoder(): PasswordEncoder = Argon2PasswordEncoder()
}
