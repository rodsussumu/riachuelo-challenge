package com.rodsussumu.riachuelo_backend.application.dtos;

public record ErrorResponseDTO(
    int status,
    String error,
    String message,
    String code
) {
}
