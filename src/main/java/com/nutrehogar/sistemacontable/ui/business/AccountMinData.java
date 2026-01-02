package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.model.AccountType;
import org.jetbrains.annotations.NotNull;

public record AccountMinData(@NotNull Integer number, @NotNull String name,
                             @NotNull AccountType type) implements Comparable<AccountMinData> {
    public AccountMinData(@NotNull Integer number, @NotNull String name, @NotNull AccountType type) {
        this.number = number;
        this.name = name;
        this.type = type;
    }

    @Override
    public int compareTo(@NotNull AccountMinData o) {
        return Integer.compare(this.number, o.number);
    }
}
