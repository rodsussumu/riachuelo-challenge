package com.rodsussumu.riachuelo_backend.application.dtos;

import lombok.Builder;

@Builder
public record UserAuthDTO(
    String username,
    String password
) { }
