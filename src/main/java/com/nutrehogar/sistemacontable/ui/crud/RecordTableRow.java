package com.nutrehogar.sistemacontable.ui.crud;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public sealed interface RecordTableRow permits RecordTableData, RecordTableTotal {
    @NotNull BigDecimal debit();

    @NotNull BigDecimal credit();
}
