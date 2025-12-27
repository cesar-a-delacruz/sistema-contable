package com.nutrehogar.sistemacontable.application.config;

import org.jetbrains.annotations.NotNull;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    private static final int BCRYPT_ROUNDS = 12;

    public static @NotNull String hashPassword(@NotNull String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean verifyPassword(@NotNull String plainPassword,@NotNull String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
