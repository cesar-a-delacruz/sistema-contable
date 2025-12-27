package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.model.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record UserFormData(@NotNull String username,
                           @NotNull Boolean isEnable,
                           @NotNull Permission permission,
                           @NotNull Optional<String> password,
                           @NotNull String updatedBy) {
}
