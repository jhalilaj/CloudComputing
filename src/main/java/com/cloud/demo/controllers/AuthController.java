package com.cloud.demo.controllers;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cloud.demo.models.Users;
import com.cloud.demo.repositories.UserRepository;
import com.cloud.demo.services.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Validate user credentials with Spring Security
        authenticationManager.authenticate(
            (Authentication) new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        );

        // If authentication is successful, load the User
        Users user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Generate JWT
        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // no roles
                .build()
        );

        // Return token
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // Helper DTOs
    record LoginRequest(String username, String password) {}
    record AuthResponse(String token) {}
}
