package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.AccountType;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Optional;

public record AccountingPeriodFormData(
        @NotNull Integer year,
        @NotNull Integer periodNumber,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Boolean closed,
        @NotNull String username
) {
}
