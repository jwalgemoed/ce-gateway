package com.ce.gateway

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@SpringBootApplication
class SpringCloudGatewayApplication {

    companion object {
        private val LOGGER = LoggerFactory.getLogger("KeyResolver")
    }

    /**
     * KeyResolver bean, this is used to define the identity of the instance to rate limit on. For now,
     * we rate limit using the remote address (client IP) but ideally we'd use the contents of a JWT token
     * to uniquely identify a user.
     */
    @Bean
    fun keyResolver() = KeyResolver { exchange: ServerWebExchange ->
        ReactiveSecurityContextHolder.getContext().map {
            LOGGER.info("Resolving identity for rate limiting: ${it.authentication.name}.")
            it.authentication.name
        }
    }
}

/**
 * Bootstrap the application, and before doing so, start the two docker containers required by the app. Redis for rate limiting and
 * keycloak for token functionality, used to route traffic based on the user (roles).
 */
fun main(args: Array<String>) {
    GenericContainer(DockerImageName.parse("redis")).apply {
        withExposedPorts(6379)
        portBindings = mutableListOf("6379:6379")
    }.start()

    KeycloakContainer().withAdminPassword("admin").withAdminUsername("admin").apply {
        portBindings = mutableListOf("8081:8080")
        withRealmImportFile("keycloak/realm-export.json")
    }.start()

    runApplication<SpringCloudGatewayApplication>(*args)
}
