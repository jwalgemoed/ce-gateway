package com.ce.gateway.filters

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Example implementation for filtering and, in this case, routing based on a role
 * being present in the token for a user. It takes the configuration, determines if the required role is
 * present, and if it is, reroutes traffic to a different node (same path).
 */
@Component
class RoleBasedRoutingFilter : AbstractGatewayFilterFactory<RoleBasedRoutingFilter.Config>(Config::class.java) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RoleBasedRoutingFilter::class.java)
    }

    override fun apply(config: Config): GatewayFilter {
        return OrderedGatewayFilter({ exchange, chain ->
            // Access the reactive security context to get information for the current user making the requests
            ReactiveSecurityContextHolder.getContext()
                .map { it.authentication as JwtAuthenticationToken }
                .flatMap { token: JwtAuthenticationToken ->
                    // If the role is present, use a different host (uri) to route to. Use the resolved route and only replace the host
                    if (token.hasRole(config.role)) {
                        val route: Route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)!!
                        val newUri = exchange.attributes[GATEWAY_REQUEST_URL_ATTR].toString().replace(route.uri.toString(), config.uri)
                        LOGGER.info("User has role '${config.role}' - changing root URI to '${config.uri}' -> '${route.uri}' to ${newUri}")
                        exchange.attributes[GATEWAY_REQUEST_URL_ATTR] = URI(newUri)
                    } else {
                        LOGGER.info("User does not have role ${config.role} - use default route.")
                    }
                    chain.filter(exchange)
                }
        }, RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1) // Make sure this filter is behind the route to request filter, as it will override the route changes we do here
    }

    data class Config(
        var role: String = "",
        var uri: String = ""
    )
}

// Extension function used to check if a role is present for a user
private fun JwtAuthenticationToken.hasRole(role: String): Boolean {
    return this.authorities.count { it.authority == role } != 0
}
