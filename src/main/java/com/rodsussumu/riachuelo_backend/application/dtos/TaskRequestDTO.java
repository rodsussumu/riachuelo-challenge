package com.rodsussumu.riachuelo_backend.application.dtos;

import lombok.Builder;

import java.util.Date;

@Builder
public record TaskRequestDTO(
        String description,
        Date dueDate
) {}
