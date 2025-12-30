package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.DocumentType;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public record JournalFormData(
        @NotNull Integer number,
        @NotNull DocumentType type,
        @NotNull String name,
        @NotNull String concept,
        @NotNull String checkNumber,
        @NotNull LocalDate date
        ) {
}
