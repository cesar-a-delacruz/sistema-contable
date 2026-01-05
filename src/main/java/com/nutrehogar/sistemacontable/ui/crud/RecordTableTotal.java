package com.nutrehogar.sistemacontable.ui.crud;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record RecordTableTotal(
        @NotNull BigDecimal debit,
        @NotNull BigDecimal credit
) implements RecordTableRow {
}
