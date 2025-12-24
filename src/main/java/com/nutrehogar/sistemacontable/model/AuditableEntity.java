package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class AuditableEntity {

    @Nullable
    @Basic(optional = false)
    @Column(name = "created_by")
    protected String createdBy;

    @Nullable
    @Basic(optional = false)
    @Column(name = "updated_by")
    protected String updatedBy;

    @Nullable
    @Column(name = "created_at")
    @Basic(optional = false)
    protected LocalDateTime createdAt;

    @Nullable
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
