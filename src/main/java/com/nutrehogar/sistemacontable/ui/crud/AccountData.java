package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.model.AuditableFields;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

public record AccountData(
        @NotNull Integer id,
        @NotNull Integer number,
        @NotNull String name,
        @NotNull AccountType type,
        @Nullable Integer subtypeId,
        @Nullable String subtypeName,
        @NotNull @Getter String createdBy,
        @NotNull @Getter String updatedBy,
        @NotNull @Getter LocalDateTime createdAt,
        @NotNull @Getter LocalDateTime updatedAt,
        @Getter int version
) implements AuditableFields {
}
