package com.rodsussumu.riachuelo_backend.application.models;

import com.rodsussumu.riachuelo_backend.application.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

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

    private String title;

    private String description;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "due_date")
    private Date dueDate;

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.PENDING;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = new java.util.Date();
        if (status == null) status = StatusEnum.PENDING;
    }
}
