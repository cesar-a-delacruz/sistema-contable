package com.nutrehogar.sistemacontable.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class Util {
    public static final DateTimeFormatter AUDITABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    public static final String NA = "N/A";

    // Convierte cualquier objeto a String de forma segura
    public static @NotNull String toStringSafe(@Nullable Object obj) {
        return (obj != null) ? obj.toString() : "";
    }
    public static @NotNull String toStringSafe(@Nullable Object obj, @NotNull String defaultValue) {
        return (obj != null) ? obj.toString() : defaultValue;
    }

    // Maneja conversiones seguras con funciones
    public static <T> @NotNull String toStringSafe(@Nullable T obj, Function<@NotNull T, @NotNull String> extractor) {
        return (obj != null) ? extractor.apply(obj) : "";
    }
    public static <T> @NotNull String toStringSafe(@Nullable T obj, Function<@NotNull T, @NotNull String> extractor, @NotNull String defaultValue) {
        return (obj != null) ? extractor.apply(obj) : defaultValue;
    }

    // Maneja conversiones seguras de BigDecimal o Double usando DECIMAL_FORMAT
    public static String formatDecimalSafe(Number number) {
        return (number != null) ? DECIMAL_FORMAT.format(number) : "";
    }

}
