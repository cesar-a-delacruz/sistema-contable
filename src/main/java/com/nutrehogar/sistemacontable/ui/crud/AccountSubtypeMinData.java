package com.nutrehogar.sistemacontable.ui.crud;

import org.jetbrains.annotations.NotNull;

public record AccountSubtypeMinData(
        @NotNull Integer id,
        @NotNull String name,
        @NotNull Integer number
) {
}
