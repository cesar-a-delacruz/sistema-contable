package com.nutrehogar.sistemacontable.ui_2.builder;

import com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Modelo de un spinner que tiene como valor un {@link LocalDate}
 *
 * @author Calcifer1331
 * @see LocalDate
 * @see LocalDateSpinner
 */
public class LocalDateSpinnerModel extends AbstractSpinnerModel {
    /**
     * Valor actual del spinner
     */
    @NotNull
    private LocalDate currentDate;
    /**
     * Fecha minima que puede optiene el valor del spinner (opcional)
     */
    @Getter
    @Setter
    @Nullable
    private LocalDate minDate;
    /**
     * Fecha maxima que puede optiene el valor del spinner (opcional)
     */
    @Getter
    @Setter
    @Nullable
    private LocalDate maxDate;
    /**
     * Unidad en la que aumenta o disminuye la fecha
     */
    @Getter
    @Setter
    @NotNull
    private ChronoUnit incrementUnit;

    public LocalDateSpinnerModel() {
        this.currentDate = LocalDate.now();
        this.incrementUnit = ChronoUnit.DAYS;
    }

    public LocalDateSpinnerModel(@Nullable LocalDate currentDate) {
        this.currentDate = ifDateNull(currentDate);
        this.incrementUnit = ChronoUnit.DAYS;
    }

    public LocalDateSpinnerModel(@Nullable LocalDate initialDate,
                                 @Nullable LocalDate minDate,
                                 @Nullable LocalDate maxDate,
                                 @Nullable ChronoUnit incrementUnit) {
        this.currentDate = ifDateNull(initialDate);
        this.minDate = ifDateNull(minDate);
        this.maxDate = ifDateNull(maxDate);
        this.incrementUnit = incrementUnit == null ? ChronoUnit.DAYS : incrementUnit;
    }

    private @NotNull LocalDate ifDateNull(@Nullable LocalDate date) {
        return date == null ? LocalDate.now() : date;
    }

    @Override
    public LocalDate getValue() {
        return currentDate;
    }

    public void setValue(LocalDate value) {
        currentDate = ifDateNull(value);
        fireStateChanged();
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof LocalDate newDate)) return;
        if (newDate.equals(currentDate)) return;
        if (!isWithinBounds(newDate)) return;
        currentDate = newDate;
        fireStateChanged();
    }

    public void resetValue() {
        currentDate = LocalDate.now();
        fireStateChanged();
    }

    @Override
    public Object getNextValue() {
        LocalDate nextDate = currentDate.plus(1, incrementUnit);
        return isWithinBounds(nextDate) ? nextDate : null;
    }

    @Override
    public Object getPreviousValue() {
        LocalDate previousDate = currentDate.minus(1, incrementUnit);
        return isWithinBounds(previousDate) ? previousDate : null;
    }

    /**
     * Verifica si la fecha es valida
     *
     * @param date fecha a verifica
     * @return
     */
    private boolean isWithinBounds(LocalDate date) {
        return (minDate == null || !date.isBefore(minDate)) && (maxDate == null || !date.isAfter(maxDate));
    }
}
