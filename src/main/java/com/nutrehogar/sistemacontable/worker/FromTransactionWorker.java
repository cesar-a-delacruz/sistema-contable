package com.nutrehogar.sistemacontable.worker;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * SwingWorker para ejecutar una operacion de base de datos mediante hibernate dentro de una transaccion
 */
public class FromTransactionWorker<T> extends SwingWorker<T, Void> {
    /**
     * Operacion que se ejecutara.
     *
     * @param session Session con una transaccion inicializado
     * @throws ApplicationException Exepcion lanzada intencionalmente, se asigna a {@code error} el mensaje se muestra en un AlertDialog.
     */
    @NotNull
    private final Function<Session, T> work;
    /**
     * Se ejecurta, en el Hilo de Swing principal, al terminal la operacion de forma exitosa
     */
    @NotNull
    private final Consumer<T> onSuccess;
    /**
     * Se ejecuta si se lanza una {@link ApplicationException}
     */
    @NotNull
    private final Consumer<ApplicationException> onFailure;
    /**
     * Error capturado durante la operacion, si se captura alguna se ejecuta {@code onFailure} y se le pasa este error.
     */
    @Nullable
    private ApplicationException error;

    public FromTransactionWorker(@NotNull Function<Session, T> work, @NotNull Consumer<T> onSuccess, @NotNull Consumer<ApplicationException> onFailure) {
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
    protected T doInBackground() {
        try {
            return HibernateUtil
                    .getSessionFactory()
                    .fromTransaction(work);
        } catch (ApplicationException ap) {
            error = ap;
        } catch (Exception e) {
            error = new ApplicationException(
                    LabelBuilder.of("Ocurrió un error en la base de datos, inténtelo nuevamente")
                            .p("Revise qe no alla cambos único que se repitan, ejm: Nombres, Números de Cuentas")
                            .p("Si el problema persiste valla al inicio y regrese")
                            .p("Si el problema persiste, cierre el programa y vuelva a intentarlo.")
                            .build(),
                    e);
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
                onFailure.accept(new ApplicationException("Error al obtener resultado", e));
            }

    }
}
