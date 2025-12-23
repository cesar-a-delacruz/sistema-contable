package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class AuditableEntity {

    @NotNull
    @Basic(optional = false)
    @Column(name = "created_by")
    protected String createdBy;

    @NotNull
    @Basic(optional = false)
    @Column(name = "updated_by")
    protected String updatedBy;

    @NotNull
    @Column(name = "created_at")
    @Basic(optional = false)
    protected LocalDateTime createdAt;

    @NotNull
    @Basic(optional = false)
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    @Version
    protected int version;

    protected AuditableEntity(@NotNull String updatedBy) {
        this.createdBy = updatedBy;
        this.updatedBy = updatedBy;
    }

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
