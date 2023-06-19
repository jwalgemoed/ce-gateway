package com.ce.gateway.proxytarget

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.AbstractEnvironment
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class ProxyTargetApplication

/**
 * Simple test application. It uses profiles to have two distinct ways of running:
 * Regular: using the config in application.properties
 * Admin: using the config in application.properties and overriding application-admin.properties
 */
@RestController
@RequestMapping("/")
class Endpoint (
    @Value("\${welcome-message}")
    private val message: String,
    @Value("\${server.port}")
    private val port: String,
    private val environment: Environment
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Endpoint::class.java)
    }

    /**
     * Handles any request under the root path, effectively forwarding everything and anything. Of course, this
     * would normally be a (set of) separate endpoints, but this is purely for demonstration purposes.
     */
    @GetMapping("/**")
    fun handleAnyRequest(httpServletRequest: HttpServletRequest) = ApiDetailResponse(message = message, path = httpServletRequest.servletPath, serverPort = port).also {
        LOGGER.info("Received request: '${it.path}' - Returning message: '${it.message}'. Active profile(s): '${environment.activeProfiles.joinToString()}'.")
    }
}

data class ApiDetailResponse(val message: String, val path: String, val serverPort: String)

fun main(args: Array<String>) {
    runApplication<ProxyTargetApplication>(*args)
}