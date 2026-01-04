package com.nutrehogar.sistemacontable.ui.business;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record AccountTotal(@NotNull BigDecimal debit,@NotNull BigDecimal credit) {
}
