package com.nutrehogar.sistemacontable.ui.business;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record GeneralLedgerPreviousPeriods(
        @NotNull BigDecimal debit,
        @NotNull BigDecimal credit,
        @NotNull BigDecimal total

) implements GeneralLedgerRow {

}
