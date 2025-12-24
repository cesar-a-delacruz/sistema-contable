package com.nutrehogar.sistemacontable.application.config;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class Util {
    public static final DateTimeFormatter AUDITABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    public static final String NA = "N/A";

    // Convierte cualquier objeto a String de forma segura
    public static @NonNull String toStringSafe(@Nullable Object obj) {
        return (obj != null) ? obj.toString() : "";
    }
    public static @NonNull String toStringSafe(@Nullable Object obj, @NonNull String defaultValue) {
        return (obj != null) ? obj.toString() : defaultValue;
    }

    // Maneja conversiones seguras con funciones
    public static <T> @NonNull String toStringSafe(@Nullable T obj, Function<@NonNull T, @NonNull String> extractor) {
        return (obj != null) ? extractor.apply(obj) : "";
    }
    public static <T> @NonNull String toStringSafe(@Nullable T obj, Function<@NonNull T, @NonNull String> extractor, @NonNull String defaultValue) {
        return (obj != null) ? extractor.apply(obj) : defaultValue;
    }

    // Maneja conversiones seguras de BigDecimal o Double usando DECIMAL_FORMAT
    public static String formatDecimalSafe(Number number) {
        return (number != null) ? DECIMAL_FORMAT.format(number) : "";
    }

}
