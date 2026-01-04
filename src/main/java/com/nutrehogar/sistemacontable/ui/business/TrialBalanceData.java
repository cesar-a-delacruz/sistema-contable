package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.model.DocumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrialBalanceData(@NotNull Long journalId, @NotNull LocalDate date, @NotNull Integer number,
                               @NotNull DocumentType type, @NotNull AccountMinData account,
                               @NotNull String reference, @NotNull String concept, @NotNull BigDecimal debit, @NotNull BigDecimal credit,
                               @NotNull BigDecimal total) implements TrialBalanceRow {
}
