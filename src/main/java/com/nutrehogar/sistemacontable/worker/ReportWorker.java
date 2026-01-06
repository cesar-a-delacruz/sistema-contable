package com.nutrehogar.sistemacontable.worker;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.exception.ReportException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * SwingWorker para ejecutar una operacion de base de datos mediante hibernate dentro de una transaccion
 */
public class ReportWorker extends SwingWorker<Path, Void> {
    /**
     * Operacion que se ejecutara.
     *
     * @param session Session con una transaccion inicializado
     * @throws ApplicationException Exepcion lanzada intencionalmente, se asigna a {@code error} el mensaje se muestra en un AlertDialog.
     */
    @NotNull
    private final Supplier<Path> work;
    /**
     * Se ejecurta, en el Hilo de Swing principal, al terminal la operacion de forma exitosa
     */
    @NotNull
    private final Consumer<Path> onSuccess;
    /**
     * Se ejecuta si se lanza una {@link ApplicationException}
     */
    @NotNull
    private final Consumer<ReportException> onFailure;
    /**
     * Error capturado durante la operacion, si se captura alguna se ejecuta {@code onFailure} y se le pasa este error.
     */
    @Nullable
    private ReportException error;

    public ReportWorker(@NotNull Supplier<Path> work, @NotNull Consumer<Path> onSuccess, @NotNull Consumer<ReportException> onFailure) {
        this.work = work;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    /**
     * Se ejecuta al terminar la operacion de la base de datos.
     *
     * @return void
     */
    @Override
    protected Path doInBackground() {
        try {
            return work.get();
        } catch (ReportException e) {
            error = e;
        }
        return null;
    }

    @Override
    protected void done() {
        if (error != null)
            onFailure.accept(error);
        else
            try {
                onSuccess.accept(get());
            } catch (Exception e) {
                onFailure.accept(new ReportException("Error al obtener el resultado", e));
            }

    }
}
