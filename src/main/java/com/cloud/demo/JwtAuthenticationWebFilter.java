package com.cloud.demo;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import org.springframework.security.core.Authentication;

import com.cloud.demo.services.JwtService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

    private final ReactiveUserDetailsService userDetailsService; // or your bridging approach
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 1) Extract "Authorization" header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Just continue if missing or invalid
            return chain.filter(exchange);
        }

        String jwtToken = authHeader.substring(7);
        String username = jwtService.extractUsername(jwtToken);

        if (username == null) {
            return chain.filter(exchange);
        }

        // 2) Look up user reactively
        return userDetailsService.findByUsername(username)
                .flatMap(userDetails -> {
                    // 3) Check token
                    if (!jwtService.isTokenValid(jwtToken, userDetails)) {
                        // invalid token -> 401
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    // 4) If valid, create Authentication object
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContext context = new SecurityContextImpl(auth);

                    // 5) Continue filter chain, but set the SecurityContext
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder
                                    .withSecurityContext(Mono.just(context)));
                })
                // 6) If user not found in DB -> 401
                .switchIfEmpty(Mono.defer(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }));
    }

}
