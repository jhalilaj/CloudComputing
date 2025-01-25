package com.cloud.demo.services;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cloud.demo.models.Users;
import com.cloud.demo.repositories.UserRepository;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MyReactiveUserDetailsService implements ReactiveUserDetailsService {

        private final UserRepository blockingUserRepository;

        @Override
        public Mono<UserDetails> findByUsername(String username) {
                // Because your repo is blocking (JPA),
                // wrap it in Mono.fromCallable(...) so it doesn't lock the event loop
                return Mono.fromCallable(() -> blockingUserRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)))
                                .map(user -> org.springframework.security.core.userdetails.User
                                                .withUsername(user.getUsername())
                                                .password(user.getPassword())
                                                .authorities(Collections.emptyList())
                                                .build());
        }
}
