package io.harmny.service.user.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "Authorization"
        val info = Info().title("Harmny Service User API").description("Harmny Service User API application").version("0.0.1")
        val components = Components()
            .addSecuritySchemes(
                securitySchemeName,
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
        val security = listOf(SecurityRequirement().addList(securitySchemeName))
        return OpenAPI().info(info).components(components).security(security)
    }
}
