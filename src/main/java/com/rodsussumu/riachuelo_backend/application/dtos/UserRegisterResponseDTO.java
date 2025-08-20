package com.rodsussumu.riachuelo_backend.application.dtos;

import lombok.Builder;

@Builder
public record UserRegisterResponseDTO(
    String username,
    String message
) {
}
