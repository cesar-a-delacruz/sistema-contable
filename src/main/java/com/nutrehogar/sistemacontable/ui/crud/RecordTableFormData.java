package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.Account;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record RecordTableFormData(
        @NotNull RecordFormData formData,
        @NotNull BigDecimal total
) implements RecordTableData {
    @Override
    public @NotNull String reference() {
        return formData.reference();
    }

    @Override
    public @NotNull Account account() {
        return formData.account();
    }

    @Override
    public @NotNull BigDecimal debit() {
        return formData.debit();
    }

    @Override
    public @NotNull BigDecimal credit() {
        return formData.credit();
    }
}
