package com.nutrehogar.sistemacontable.ui.business;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public sealed interface TrialBalanceRow permits TrialBalanceData, TrialBalanceTotal {
    @NotNull BigDecimal total();
    @NotNull BigDecimal credit();
    @NotNull BigDecimal debit();
}
