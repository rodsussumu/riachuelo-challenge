package com.rodsussumu.riachuelo_backend.application.dtos;

import com.rodsussumu.riachuelo_backend.application.enums.StatusEnum;
import lombok.Builder;

import java.util.Date;

@Builder
public record TaskRequestDTO(
        String title,
        String description,
        Date dueDate
) {}
