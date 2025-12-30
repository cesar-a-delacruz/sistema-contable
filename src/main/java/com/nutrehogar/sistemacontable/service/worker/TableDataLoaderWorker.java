package com.nutrehogar.sistemacontable.service.worker;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TableDataLoaderWorker<Entity> extends SwingWorker<List<Entity>, Void> {

    /**
     * Esta funcion debe retornar una lista de {@code Entity} que debe er buscada en la base de datos, esta se ejecuta en el {@link DataLoader}
     *
     * @return lista de {@code Entity} que se busco en la base de datos
     */
    @Nullable
    protected ApplicationException error;

    protected abstract List<Entity> inTransaction(Session session) throws ApplicationException;

    protected abstract void onFailure();

    protected abstract void onSuccess(List<Entity> result);

    @Override
    protected @NotNull List<Entity> doInBackground() {
        AtomicReference<List<Entity>> list = new AtomicReference<>(List.of());
        try {
            HibernateUtil
                    .getSessionFactory()
                    .inTransaction(session -> list.set(inTransaction(session)));
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
        return list.get();
    }

    @Override
    protected void done() {
        List<Entity> list = List.of();
        try {
            if (get() == null) {
                error = new ApplicationException(LabelBuilder.build("Error al optener la lista de subtipos de cuentas"));
            }
            list = get();
        } catch (Exception e) {
            error = new ApplicationException(LabelBuilder.build("Error al optener la lista de subtipos de cuentas"), e);
        }

        onSuccess(list);

        if (error != null) {
            onFailure();
        }

    }
}
