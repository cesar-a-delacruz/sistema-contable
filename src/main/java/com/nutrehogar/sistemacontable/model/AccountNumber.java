package com.nutrehogar.sistemacontable.model;

import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.exception.InvalidFieldException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class AccountNumber {
    @NonNull
    public static Integer generateNumber(@NotNull String subNumber, @NotNull AccountType type) throws InvalidFieldException {
        int length = subNumber.length();
        if (length > 4 || length == 0)
            throw new InvalidFieldException("El numero e cuenta debe tener entres 1 a 4 dígitos", new NumberFormatException("The Account Number length must be between 4 and 0 digits"));
        int remaining = 4 - length;
        return Integer.valueOf(type.getId() + (remaining == 0 ? subNumber : "0".repeat(remaining) + subNumber));
    }

    @NonNull
    public static Integer generateNumber(@NotNull Integer subNumber, @NotNull AccountType type) {
        return generateNumber(subNumber.toString(), type);
    }

    @NotNull
    public static Integer getSubNumber(@NotNull Integer number) {
        return Integer.valueOf(number.toString().substring(1));
    }

    @NotNull
    public static String getFormattedNumber(@NotNull Integer number) {
        var str = number.toString();
        return str.charAt(0) + "." + str.substring(1);
    }
}
