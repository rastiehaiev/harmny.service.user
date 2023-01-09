package io.harmny.service.user

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(
    info = Info(
        title = "Harmny User Service",
        description = "User-based operations for Harmny project.",
    )
)
class HarmnyUserServiceApplication

fun main(args: Array<String>) {
    runApplication<HarmnyUserServiceApplication>(*args)
}
