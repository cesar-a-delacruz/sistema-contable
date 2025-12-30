package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Optional;

public record RecordFormData(@NotNull String reference,
                             @NotNull Account account,
                             @NotNull BigDecimal debit,
                             @NotNull BigDecimal credit,
                             @NotNull String username) {
}
