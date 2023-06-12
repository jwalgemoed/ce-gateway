package com.ce.gateway.configuration

import com.ce.gateway.security.CustomJwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration {

    /**
     * Simple security setup, configured to to only allow authenticated requests and configure a custom converter for retrieving user roles.
     */
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity, customJwtAuthenticationConverter: CustomJwtAuthenticationConverter): SecurityWebFilterChain {
        return http.authorizeExchange {
            it.anyExchange().authenticated()
        }.oauth2ResourceServer {
            it.jwt {
                it.jwtAuthenticationConverter(customJwtAuthenticationConverter)
            }
        }.build()
    }
}
