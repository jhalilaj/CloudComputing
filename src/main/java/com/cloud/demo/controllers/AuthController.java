package com.cloud.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud.demo.controllers.AuthController.AuthResponse;
import com.cloud.demo.services.JwtService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ReactiveUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(ReactiveUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody LoginRequest request) {
        return userDetailsService.findByUsername(request.username())
                .flatMap(userDetails -> {
                    // Check password
                    if (passwordEncoder.matches(request.password(), userDetails.getPassword())) {
                        // If valid, generate JWT
                        String token = jwtService.generateToken(userDetails);
                        return Mono.just(ResponseEntity.ok(new AuthResponse(token)));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                    }
                })
                // If user not found
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    // record or class for request/response
    public static record LoginRequest(String username, String password) {
    }

    public static record AuthResponse(String token) {
    }
}
