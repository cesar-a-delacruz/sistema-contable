package com.nutrehogar.sistemacontable.model;

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
 * <p>
 * Las cuentas de tipo {@code ACTIVO}, {@code PASIVO}, {@code PATRIMONIO}, se mantiene el saldo al pasar de periodo, las cuentas de tipo {@code INGRESO}, {@code GASTO} Y {@code COSTO} no mantinen saldo.
 * </p>
 * {@code COSTO} es el unico que su valor está mal
 *
 * @author Calcifer1331
 * @see com.nutrehogar.sistemacontable.model.AccountEntity
 * @see com.nutrehogar.sistemacontable.model.Account
 * @see com.nutrehogar.sistemacontable.model.AccountSubtype
 */
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AccountType {
    ASSETS(1, "Activo",true) {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal debit, BigDecimal credit) {
            return balance.add(debit, DECIMAL128).subtract(credit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    LIABILITIES(2, "Pasivo",true) {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal debit, BigDecimal credit) {
            return balance.add(credit, DECIMAL128).subtract(debit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    EQUITY(3, "Patrimonio",true) {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal debit, BigDecimal credit) {
            return balance.add(credit, DECIMAL128).subtract(debit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    INCOME(4, "Ingreso", false) {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal debit, BigDecimal credit) {
            return balance.add(credit, DECIMAL128).subtract(debit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    EXPENSE(5, "Gasto", false) {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal debit, BigDecimal credit) {
            return balance.add(debit, DECIMAL128).subtract(credit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    },
    // TODO: El método no esta bien implementado
    COST(6, "Costo", false) {
        @Override
        public BigDecimal getBalance(BigDecimal balance, BigDecimal debit, BigDecimal credit) {
            return balance.add(credit, DECIMAL128).subtract(debit, DECIMAL128).setScale(2, RoundingMode.HALF_UP);
        }
    };

    /**
     * Es el id con el que esta registrado en la base de datos
     */
    int id;
    @NotNull
    String name;
    /**
     * Indica si el tipo de cuenta se acumula entre periodos
     */
    boolean cumulative;

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
    public abstract BigDecimal getBalance(BigDecimal balance, BigDecimal debit, BigDecimal credit);
}
