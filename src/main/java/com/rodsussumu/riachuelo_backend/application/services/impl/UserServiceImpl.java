package com.rodsussumu.riachuelo_backend.application.services.impl;

import com.rodsussumu.riachuelo_backend.application.config.TokenHolder;
import com.rodsussumu.riachuelo_backend.application.config.TokenService;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserRegisterResponseDTO;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.BadCredentialsException;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.UsernameAlreadyExistsException;
import com.rodsussumu.riachuelo_backend.application.models.User;
import com.rodsussumu.riachuelo_backend.application.repositories.UserRepository;
import com.rodsussumu.riachuelo_backend.application.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserRegisterResponseDTO register(UserAuthDTO userAuthDTO) {
        if (userRepository.findByUsername(userAuthDTO.username()).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
        User user = new User();
        user.setUsername(userAuthDTO.username());
        user.setPassword(passwordEncoder.encode(userAuthDTO.password()));
        userRepository.save(user);
        return UserRegisterResponseDTO.builder()
                .username(userAuthDTO.username())
                .message("User created")
                .build();
    }

    public UserAuthResponseDTO login(UserAuthDTO userAuthDTO) {
        try {
            UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(
                    userAuthDTO.username(), userAuthDTO.password()
            );

            Authentication authentication = authenticationManager.authenticate(usernamePassword);

            Optional<User> user = userRepository.findByUsername(userAuthDTO.username());

            if(Objects.isNull(user)) {
                throw new BadCredentialsException();
            }

            String username = authentication.getName();

            String token = tokenService.generateToken(username);

            TokenHolder.setToken(token);

            return UserAuthResponseDTO.builder()
                    .authenticated(true)
                    .username(userAuthDTO.username())
                    .build();
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException();
        }
    }

}
