package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.AccountType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record AccountFormData(@NotNull String name,
                              @NotNull Integer number,
                              @NotNull AccountType type,
                              @NotNull Optional<Integer> subtypeId,
                              @NotNull String username) {
}
