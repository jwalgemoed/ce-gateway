package com.ce.gateway.security

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class CustomJwtAuthenticationConverter : Converter<Jwt, Mono<JwtAuthenticationToken>> {

    private val objectMapper = ObjectMapper()

    override fun convert(jwt: Jwt) = Mono.just(JwtAuthenticationToken(jwt, extractAuthorities(jwt)))

    /**
     * Extractor function, takes the grantedauthority roles from the jwt token and exposes them for usage.
     */
    private fun extractAuthorities(jwt: Jwt): List<GrantedAuthority> {
        if (jwt.getClaim<Any>("resource_access") != null) {
            val realmAccess = jwt.getClaim("resource_access") as Map<*, *>
            val rolesElement = (realmAccess["costengineering.eu"] as Map<*, *>)["roles"]
            val roles: List<String> = objectMapper.convertValue(rolesElement, object : TypeReference<List<String>>() {})
            val authorities: MutableList<GrantedAuthority> = ArrayList()
            for (role in roles) {
                authorities.add(SimpleGrantedAuthority(role))
            }
            return authorities
        }
        return ArrayList()
    }
}
