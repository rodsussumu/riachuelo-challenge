package com.rodsussumu.riachuelo_backend.application.models;

import com.rodsussumu.riachuelo_backend.application.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "tb_tasks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private Date create_date;

    private Date expire_date;

    private StatusEnum status;

    @ManyToMany(mappedBy = "tasks")
    private Set<User> users;
}
