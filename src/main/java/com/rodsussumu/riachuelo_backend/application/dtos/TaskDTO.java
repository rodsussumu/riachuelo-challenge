package com.rodsussumu.riachuelo_backend.application.dtos;

import com.rodsussumu.riachuelo_backend.application.enums.StatusEnum;
import lombok.Builder;
import java.util.Date;

@Builder
public record TaskDTO(
        Long id,
        String title,
        String description,
        Date createdAt,
        Date dueDate,
        StatusEnum status
) {}
