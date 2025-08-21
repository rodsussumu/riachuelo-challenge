package com.rodsussumu.riachuelo_backend.application.controllers;

import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserRegisterResponseDTO;
import com.rodsussumu.riachuelo_backend.application.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class UserController {

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
        UserAuthResponseDTO response =  userService.login(userAuthDTO);
        return ResponseEntity.ok(response);
    }

}
