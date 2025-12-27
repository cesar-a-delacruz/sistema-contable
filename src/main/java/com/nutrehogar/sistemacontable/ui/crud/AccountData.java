package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.AccountType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

public record AccountData(
        @NotNull Integer id,
        @NotNull Integer number,
        @NotNull String name,
        @NotNull AccountType type,
        @Nullable AccountSubtypeMinData subtype,
        @NotNull String createdBy,
        @NotNull String updatedBy,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime updatedAt
) {
}
