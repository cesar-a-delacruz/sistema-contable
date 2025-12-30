package com.nutrehogar.sistemacontable.ui;

import com.nutrehogar.sistemacontable.exception.AppException;
import com.nutrehogar.sistemacontable.exception.InvalidFieldException;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableModel;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Optional;

/**
 * Clase base para las vistas de la applicacion, cuenta con las funciones esenciales para mostrar y registrar errores y mensajes.
 */
@Slf4j
public abstract class View extends JPanel {
    /**
     * Usuario de la persona que inicio session, se usa para restringir funcionalidades
     */
    @NotNull
    protected final User user;

    public View(@NotNull User user) {
        this.user = user;
    }

    public void showMessage(@NotNull Object message) {
        showMessage(message, "Message");
    }

    /**
     * Menage a mostrar al usuario, por lo general, mensages de sugerencias y confirmaciones.
     *
     * @param message mensaje a mostrar
     * @param title   titulo del dialog
     */
    public void showMessage(@NotNull Object message, @NotNull String title) {
        log.info(title, message);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(@NotNull String message, @Nullable Exception cause) {
        showError(message, "Error", cause);
    }

    /**
     * Mustra y registra un mensage de error
     *
     * @param message texto que se mostrara en el dialog
     * @param title   titulo del dialog
     * @param cause   Error que causo el error, se registrara en logback
     */
    public void showError(@NotNull String message, @NotNull String title, @Nullable Exception cause) {
        if (cause != null && !(cause instanceof AppException))
            log.error(cause.getMessage(), cause);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void showError(@NotNull Exception cause) {
        if (cause instanceof InvalidFieldException i) {
            showWarning(i);
            return;
        }
        showError(cause.getMessage(), cause);
    }

    public void showWarning(@NotNull InvalidFieldException cause) {
        JOptionPane.showMessageDialog(this, cause.getMessage(), "Advertencia!", JOptionPane.INFORMATION_MESSAGE);
    }

}