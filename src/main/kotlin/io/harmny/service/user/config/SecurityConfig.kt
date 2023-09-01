package io.harmny.service.user.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.harmny.service.user.properties.HarmnyUserServiceProperties
import io.harmny.service.user.service.UserService
import io.harmny.service.user.web.filter.TokenAuthenticationFilter
import io.harmny.service.user.web.handler.OAuth2AuthenticationFailureHandler
import io.harmny.service.user.web.handler.OAuth2AuthenticationSuccessHandler
import io.harmny.service.user.web.instruments.RestAuthenticationEntryPoint
import io.harmny.service.user.web.instruments.TokenProvider
import io.harmny.service.user.web.repository.HttpCookieOAuth2AuthorizationRequestRepository
import io.harmny.service.user.web.service.UserPrincipalService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
    private val tokenProvider: TokenProvider,
    private val userPrincipalService: UserPrincipalService,
    private val properties: HarmnyUserServiceProperties,
) {

    companion object {
        private val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    }

    private val authWhiteList = arrayOf(
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/v2/api-docs/**",
        "/swagger-resources/**",
        "/favicon.ico",
    )

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .csrf().disable()
            .cors().configurationSource(corsConfigurationSource()).and()
            .formLogin().disable()
            .httpBasic().disable()
            .exceptionHandling().authenticationEntryPoint(RestAuthenticationEntryPoint(objectMapper)).and()
            .authorizeHttpRequests {
                it.antMatchers(*authWhiteList).permitAll()
                    .antMatchers("/auth/**", "/oauth2/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login()
            .authorizationEndpoint()
            .baseUri("/oauth2/authorize")
            .authorizationRequestRepository(cookieAuthorizationRequestRepository())
            .and()

            .redirectionEndpoint()
            .baseUri("/oauth2/callback/*")
            .and()

            .userInfoEndpoint()
            .userService(userPrincipalService)
            .and()

            .successHandler(oAuth2AuthenticationSuccessHandler())
            .failureHandler(oAuth2AuthenticationFailureHandler()).and()
            .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = properties.cors.allowedOrigins.split(",").map { it.trim() }
        configuration.allowedMethods = listOf("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD")
        configuration.allowedHeaders = listOf("*")
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        log.info("Configured CORS. Allowed origins: ${configuration.allowedOrigins}.")
        return source
    }

    @Bean
    fun cookieAuthorizationRequestRepository(): HttpCookieOAuth2AuthorizationRequestRepository {
        return HttpCookieOAuth2AuthorizationRequestRepository()
    }

    @Bean
    fun oAuth2AuthenticationSuccessHandler(): OAuth2AuthenticationSuccessHandler {
        return OAuth2AuthenticationSuccessHandler(
            properties,
            userService,
            tokenProvider,
            cookieAuthorizationRequestRepository(),
        )
    }

    @Bean
    fun oAuth2AuthenticationFailureHandler(): OAuth2AuthenticationFailureHandler {
        return OAuth2AuthenticationFailureHandler(objectMapper, cookieAuthorizationRequestRepository())
    }

    @Bean
    fun tokenAuthenticationFilter(): TokenAuthenticationFilter {
        return TokenAuthenticationFilter(tokenProvider)
    }
}
