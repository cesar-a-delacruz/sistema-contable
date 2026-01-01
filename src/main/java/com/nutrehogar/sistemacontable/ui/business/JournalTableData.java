package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.DocumentType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JournalTableData(
        @NotNull Long journalId,
        @NotNull LocalDate date,
        @NotNull Integer number,
        @NotNull DocumentType type,
        @NotNull Account account,
        @NotNull String reference,
        @NotNull BigDecimal debit,
        @NotNull BigDecimal credit

) {
}
