package com.nutrehogar.sistemacontable.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class LabelBuilder {
    @NotNull
    private final StringBuilder builder;

    private LabelBuilder() {
        builder = new StringBuilder();
        builder.append("<html>");
    }

    @NotNull
    @Contract("_ -> new")
    public static LabelBuilder of(@NotNull String p) {
        return new LabelBuilder().p(p);
    }

    @NotNull
    @Contract(" -> new")
    public static LabelBuilder of() {
        return new LabelBuilder();
    }

    @NotNull
    public static String build(@NotNull String p) {
        return LabelBuilder.of(p).build();
    }

    @NotNull
    public String build() {
        return builder.append("</html>").toString();
    }

    @NotNull
    public LabelBuilder append(@NotNull String str) {
        builder.append(str);
        return this;
    }

    @NotNull
    public LabelBuilder p(@NotNull String p) {
        builder.append("<p>").append(p).append("</p>");
        return this;
    }
}
