package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.model.DocumentType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record JournalData(
        @NotNull Long journalId,
        @NotNull LocalDate date,
        @NotNull Integer number,
        @NotNull DocumentType type,
        @NotNull AccountMinData account,
        @NotNull String reference,
        @NotNull String concept,
        @NotNull BigDecimal debit,
        @NotNull BigDecimal credit
) {
}
