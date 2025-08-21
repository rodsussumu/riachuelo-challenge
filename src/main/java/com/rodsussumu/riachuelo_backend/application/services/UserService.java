package com.rodsussumu.riachuelo_backend.application.services;

import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserRegisterResponseDTO;
import org.springframework.http.ResponseEntity;

public interface UserService {
    UserRegisterResponseDTO register(UserAuthDTO userAuthDTO);
    UserAuthResponseDTO login(UserAuthDTO userAuthDTO);
}
