package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
@NoArgsConstructor
public abstract class AuditableEntity implements AuditableFields {

    @Basic(optional = false)
    @Column(name = "created_by")
    @Nullable String createdBy;

    @Basic(optional = false)
    @Column(name = "updated_by")
    @Nullable String updatedBy;

    @Column(name = "created_at")
    @Basic(optional = false)
    @Nullable LocalDateTime createdAt;

    @Basic(optional = false)
    @Column(name = "updated_at")
    @Nullable LocalDateTime updatedAt;

    @Version
    int version;

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
