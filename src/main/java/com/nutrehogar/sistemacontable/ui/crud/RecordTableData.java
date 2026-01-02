package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.Account;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public sealed interface RecordTableData extends RecordTableRow permits RecordTableFormData, RecordTableEntity {
    @NotNull String reference();

    @NotNull Account account();

    @NotNull BigDecimal debit();

    @NotNull BigDecimal credit();
}
