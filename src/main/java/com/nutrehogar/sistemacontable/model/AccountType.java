package com.nutrehogar.sistemacontable.model;

import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.MathContext.DECIMAL128;

/**
 * Enum que define los tipos que pueden ser las cunetas.
 * <p>
 * Dependiendo del tipo de cuenta el saldo es el que suma es el haber o debe y
 * vise versa con la resta
 * <p>
 * {@code COSTO} es el unico que su valor está mal
 *
 * @author Calcifer1331
 * @see com.nutrehogar.sistemacontable.domain.model.TipoCuenta
 * @see com.nutrehogar.sistemacontable.ui_2.view.components.MayorGenTableModel
 */
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AccountType {
    ASSETS(1, "ACTIVO") {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal credit, BigDecimal debit) {
            return balance.add(debit, DECIMAL128).subtract(credit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    LIABILITIES(2, "PASIVO") {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal credit, BigDecimal debit) {
            return balance.add(credit, DECIMAL128).subtract(debit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    EQUITY(3, "PATRIMONIO") {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal credit, BigDecimal debit) {
            return balance.add(credit, DECIMAL128).subtract(debit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    INCOME(4, "INGRESO") {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal credit, BigDecimal debit) {
            return balance.add(credit, DECIMAL128).subtract(debit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    EXPENSE(5, "GASTO") {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal credit, BigDecimal debit) {
            return balance.add(debit, DECIMAL128).subtract(credit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    // TODO: El metodo no esta bien implementado
    COST(6, "COSTO") {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal credit, BigDecimal debit) {
            return balance.add(credit, DECIMAL128).subtract(debit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    };

    /**
     * Es el id con el que esta registrado en la base de datos
     */
    @NotNull
    int id;
    @NotNull
    String name;

    public static @NotNull AccountType fromId(int id) {
        for (AccountType tipo : values()) {
            if (tipo.getId() == id) {
                return tipo;
            }
        }
        return ASSETS;
    }

    public @NotNull String getCellRenderer() {
        return AccountType.getCellRenderer(this);
    }

    public static @NotNull String getCellRenderer(@NotNull AccountType tipo) {
        return tipo.getId() + " " + tipo.getName();
    }

    /**
     * Dependiendo del tipo de cuenta el saldo es el que suma es el haber o debe y
     * vise versa con la resta
     *
     * @param balance
     * @param credit
     * @param debit
     * @return suma de {@code saldo} y el resultado de la resta o suma de
     * {@code haber} y {@code debe}
     */
    public abstract BigDecimal getBalance(BigDecimal balance, BigDecimal credit, BigDecimal debit);
}
