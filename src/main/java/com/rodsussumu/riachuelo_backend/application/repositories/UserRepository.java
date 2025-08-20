package com.rodsussumu.riachuelo_backend.application.repositories;

import com.rodsussumu.riachuelo_backend.application.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
