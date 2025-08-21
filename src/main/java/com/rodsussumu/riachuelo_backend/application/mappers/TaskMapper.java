package com.rodsussumu.riachuelo_backend.application.mappers;

import com.rodsussumu.riachuelo_backend.application.dtos.TaskDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.TaskRequestDTO;
import com.rodsussumu.riachuelo_backend.application.models.Task;
import org.mapstruct.*;

import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDTO toDTO(Task task);
    List<TaskDTO> toDTOList(List<Task> tasks);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "createdAt", ignore = true),  // será definido no @PrePersist
            @Mapping(target = "status", ignore = true),     // idem
            @Mapping(source = "dueDate", target = "dueDate")
    })
    Task toEntity(TaskRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(source = "dueDate", target = "dueDate"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "status", ignore = true)
    })
    void updateFromRequest(TaskRequestDTO dto, @MappingTarget Task task);
}
