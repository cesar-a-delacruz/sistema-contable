package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.LedgerRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public record RecordTableData(
        @Nullable LedgerRecord record,
        @NotNull String reference,
        @Nullable Account account,
        @Nullable BigDecimal debit,
        @Nullable BigDecimal credit,
        @NotNull BigDecimal total
) {
    public RecordTableData(@NotNull String reference, @Nullable BigDecimal debit, @Nullable BigDecimal credit, @NotNull BigDecimal total) {
        this(null, reference, null, debit, credit, total);
    }

    public boolean isEntity() {
        return record != null;
    }
}
