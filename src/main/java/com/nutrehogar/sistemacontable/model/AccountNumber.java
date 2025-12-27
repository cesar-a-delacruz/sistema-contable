package com.nutrehogar.sistemacontable.model;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class AccountNumber {
    @NonNull
    public static Integer generateNumber(@NotNull String subNumber, @NotNull AccountType type) {
        int remaining = 4 - subNumber.length();
        return Integer.valueOf(type.getId() + (remaining <= 0 ? subNumber : subNumber + "0".repeat(remaining)));
    }

    @NotNull
    public static String getSubNumber(@NotNull Integer number) {
        return number.toString().substring(1);
    }

    @NotNull
    public static String getFormattedNumber(@NotNull Integer number) {
        var str = number.toString();
        return str.charAt(0) + "." + str.substring(1);
    }
}
