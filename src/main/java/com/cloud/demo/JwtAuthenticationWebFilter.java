package com.cloud.demo;



import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;


import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import org.springframework.security.core.Authentication;

import com.cloud.demo.services.JwtService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

    private final ReactiveUserDetailsService userDetailsService; 
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        
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

        
        return userDetailsService.findByUsername(username)
                .flatMap(userDetails -> {
                    
                    if (!jwtService.isTokenValid(jwtToken, userDetails)) {
                        // invalid token -> 401
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContext context = new SecurityContextImpl(auth);

                   
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder
                                    .withSecurityContext(Mono.just(context)));
                })
                
                .switchIfEmpty(Mono.defer(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }));
    }

}
