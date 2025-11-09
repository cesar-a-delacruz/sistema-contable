package com.nutrehogar.sistemacontable.domain.model;
import jakarta.persistence.*;
import com.nutrehogar.sistemacontable.application.config.PasswordHasher;

public class UserEntityListener {
    @PrePersist
    @PreUpdate
    public void hashPassword(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()
                && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(PasswordHasher.hashPassword(user.getPassword()));
        }
    }
}
