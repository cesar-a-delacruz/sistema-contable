package com.nutrehogar.sistemacontable.service.worker;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
/**
 * SwingWorker para ejecutar una operacion de base de datos mediante hibernate dentro de una transaccion
 */
public abstract class HibernateAsyncWorker extends SwingWorker<Void, Void> {
    /**
     * Error capturado durante la operacion, si se captura alguna se ejecuta {@code onFailure} y se le pasa este error.
     */
    @Nullable
    protected ApplicationException error;

    /**
     * Operacion que se ejecutara.
     * @param session Session con una transaccion inicializado
     * @throws ApplicationException Exepcion lanzada intencionalmente, se asigna a {@code error} el mensaje se muestra en un AlertDialog.
     */
    protected abstract void inTransaction(Session session) throws ApplicationException;

    /**
     * Se ejecurta, en el Hilo de Swing principal, al terminal la operacion de forma exitosa
     */
    protected abstract void onSuccess();

    /**
     * Se ejecuta si se lanza una {@link ApplicationException}
     */
    protected abstract void onFailure();

    /**
     * Se ejecuta al terminar la operacion de la base de datos.
     * @return void
     */
    @Override
    protected Void doInBackground() {
        try {
            HibernateUtil
                    .getSessionFactory()
                    .inTransaction(this::inTransaction);
        } catch (ApplicationException ap) {
            error = ap;
        } catch (ConstraintViolationException cve) {
            error = new ApplicationException(
                    LabelBuilder.of("Los datos ingresados son inválidos.")
                            .p("Por favor revise qe no alla cambos único que se repitan, ejm: Nombres, Números de Cuentas")
                            .build(),
                    cve);
        } catch (HibernateException he) {
            error = new ApplicationException(
                    LabelBuilder.of("Ocurrió un error en la base de datos, inténtelo nuevamente")
                            .p("Si el problema persiste valla al inicio y regrese")
                            .p("Si el problema persiste, cierre el programa y vuelva a intentarlo.")
                            .build(),
                    he);
        } catch (Exception e) {
            error = new ApplicationException(
                    LabelBuilder.of("Ocurrió un error inesperado, por favor inténtelo de nuevo.")
                            .p("Si el problema persiste, cierre el programa y vuelva a intentarlo.")
                            .build(),
                    e);
        }
        return null;
    }

    @Override
    protected void done() {
        if (error != null) {
            onFailure();
            return;
        }
        onSuccess();
    }
}
