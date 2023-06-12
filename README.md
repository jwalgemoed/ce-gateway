# Spring Cloud Gateway RI application

Spring Cloud Gateway is (in essence) and already complete application framework that can be used for (dynamic) routing
of requests from the gateway to different services downstream.

`WEB` -> `Spring Cloud Gateway` -> `Local/Internal Service`

Spring cloud gateway also brings different features that can be of use:

- Authentication: Checking credentials and routing (or not) based on this
- API Routing: Core feature, used to make sure traffic is sent to the right (internal) service for handling. The framework is highly customizable, allowing developers to route traffic based on whatever rule they see fit.
- Rate limiting: A common feature in front-facing services, so unsuprisingly rate limiting is part of the proposition. This demo application configures route limiting using a REDIS backed datasource.
- Resiliency: Support for Spring cloud resiliency features (retry, circuitbreaking, timeouts)

## Run the application 

Call the main function in the `SpringCloudGatewayApplication` class. Make sure you have Docker running so the associated images
can be bootstrapped.

## Role based routing feature

Spring Cloud Gateway does its work with filters; a request comes in, travels through a set of filters and the result of
that pipeline is then fed back to the calling user. Filters allow for modifications before and after forwarding, so they are
very flexible.

In the example filter provided, custom configuration is added to the `application.yaml` - the file containing the routing
configuration. A specific filter is applied which checks for the presence of a certain role and changes the host the 
request is forwarded to based on this information.

This is already a slightly more complex example, as routing can be made dynamic based on certain available headers
or any other strategies (even randomization). 

## Considerations

The application rate limits heavily, so if you try to send more than 1 request per second you will already see
rate limiting being applied.

Routing is done to non-existent downstream services; so you will get an error when the gateway tries to forward traffic.
To verify that this does in fact work, configure a service to call or replace the URIs with existing web addresses, and
see how the response is then proxied.

## Customize with care

The conventions and defaults applied by Spring Cloud Gateway generally fit most use cases. In general it should not be
required to customize the application too extensively. Try to stick with the defaults as much as possible as not doing this
can cause bugs and performance issues that can have a big effect since (once fully in place) all requests would flow
through the gateway. 