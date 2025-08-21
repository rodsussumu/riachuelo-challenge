package com.rodsussumu.riachuelo_backend.application.controllers;

import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserRegisterResponseDTO;
import com.rodsussumu.riachuelo_backend.application.services.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class UserController {

    private static final String TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final Duration TOKEN_TTL = Duration.ofSeconds(6000);

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<UserRegisterResponseDTO> register(@RequestBody UserAuthDTO userAuthDTO) {
        UserRegisterResponseDTO response = userService.register(userAuthDTO);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("login")
    public ResponseEntity<UserAuthResponseDTO> login(@RequestBody UserAuthDTO userAuthDTO) {
        UserAuthResponseDTO response = userService.login(userAuthDTO);

        return ResponseEntity.ok()
                .body(response);
    }
}
