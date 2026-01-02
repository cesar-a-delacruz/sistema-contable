package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.model.AccountType;
import org.jetbrains.annotations.NotNull;

public record AccountMinData(
        int number,
        @NotNull String name,
        @NotNull AccountType type
) implements Comparable<AccountMinData>{

    @Override
    public int compareTo(@NotNull AccountMinData o) {
        return Integer.compare(number, o.number);
    }
}
