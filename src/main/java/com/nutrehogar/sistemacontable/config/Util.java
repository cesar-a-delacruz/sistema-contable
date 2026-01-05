package com.nutrehogar.sistemacontable.config;

import com.nutrehogar.sistemacontable.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

@Slf4j
public class Util {
    public static final Locale LOCALE = Locale.of("es", "PA");
    public static final DateTimeFormatter AUDITABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss", LOCALE);
    public static final DateTimeFormatter SMALL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE);
    public static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", LOCALE);
    public static final DateTimeFormatter LARGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d 'de' LLLL 'del' yyyy", LOCALE);
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

    public static void openFile(File file) throws UnsupportedOperationException, IOException, IllegalArgumentException {
        if (!Desktop.isDesktopSupported())
            throw new UnsupportedOperationException("Desktop no soportado");

        var desktop = Desktop.getDesktop();

        if (!file.exists())
            throw new IllegalArgumentException("El archivo no existe");

        desktop.open(file);
    }

}
