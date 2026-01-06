package com.nutrehogar.sistemacontable.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum UIEntityInfo {
    ENTRY_FORM("Formulario de Entrada", "Formularios de Entrada", false, new FlatSVGIcon("svgs/form.svg")),
    JOURNAL("Libro Diario", "Libro Diario", false, new FlatSVGIcon("svgs/journal.svg")),
    TRIAL_BALANCE("Balance General", "Balance General", false, new FlatSVGIcon("svgs/trial_balance.svg")),
    GENERAL_LEDGER("Mayor General", "Mayor General", true, new FlatSVGIcon("svgs/general_ledger.svg")),
    ACCOUNT("Cuenta", "Cuentas", true, new FlatSVGIcon("svgs/account.svg")),
    ACCOUNT_SUBTYPE("Subtipo de Cuenta", "Subtipos de Cuentas", false, new FlatSVGIcon("svgs/account_subtype.svg")),
    USER("Usuario", "Usuarios", false, new FlatSVGIcon("svgs/user.svg")),
    ACCOUNTING_PERIOD("Periodo Contable", "Periodos Contables", false, new FlatSVGIcon("svgs/accounting_period.svg"));

    @NotNull String name;
    @NotNull String plural;
    boolean female;
    @NotNull FlatSVGIcon icon;
}
