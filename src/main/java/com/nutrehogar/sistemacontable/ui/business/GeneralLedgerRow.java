package com.nutrehogar.sistemacontable.ui.business;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public sealed interface GeneralLedgerRow permits GeneralLedgerEntity, RowTotal, GeneralLedgerPreviousPeriods {
    @NotNull BigDecimal debit();
    @NotNull BigDecimal credit();
    @NotNull BigDecimal total();
}
