package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.LedgerRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

public record RecordTableEntity(
        @NotNull LedgerRecord entity,
        @NotNull BigDecimal total
) implements RecordTableData {
    @Override
    public @NotNull String reference() {
        return entity.getReference();
    }

    @Override
    public @NotNull Account account() {
        return Objects.requireNonNull(entity.getAccount());
    }

    @Override
    public @NotNull BigDecimal debit() {
        return entity.getDebit();
    }

    @Override
    public @NotNull BigDecimal credit() {
        return entity.getCredit();
    }
}
