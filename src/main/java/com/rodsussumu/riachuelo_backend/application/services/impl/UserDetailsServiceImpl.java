package com.rodsussumu.riachuelo_backend.application.services.impl;

import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.InvalidTokenException;
import com.rodsussumu.riachuelo_backend.application.repositories.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = repository.findByUsername(username)
                .orElseThrow(InvalidTokenException::new);
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}
