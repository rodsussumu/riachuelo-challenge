package com.rodsussumu.riachuelo_backend.application.dtos;

import lombok.Builder;

@Builder
public record UserAuthResponseDTO(
    String username,
    boolean authenticated
) { }
