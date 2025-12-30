package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.service.worker.HibernateAsyncWorker;
import com.nutrehogar.sistemacontable.service.worker.TableDataLoaderWorker;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Clase base para las Vistas de las entidades de base de datos
 * @param <Entity> Entidad de la base de datos
 * @param <FormData> DTO de datos de Formularios
 */
public abstract class CRUDView<Entity, FormData> extends SimpleView<Entity> {

    public CRUDView(@NotNull User user, @NotNull String entityName) {
        super(user,entityName);
    }

    /**
     * Busca los datos en la db y los agrega a la tabla
     */
    protected void loadData() {
        new DataLoader().execute();
    }

    /**
     * Optiene los datos del formualario para actualizar o crear una entidad
     * @return DTO con los datos basicos de la enidad
     */
    protected abstract @NotNull FormData getDataFromForm();

    /**
     * Inserta los datos de la entidad en el formulario
     * @param entity Entidad que se editara
     */
    protected abstract void setEntityDataInForm(@NotNull Entity entity);

    /**
     * Optiene los datos que se mostraran en la tabla
     * @param session Session con transaccion iniciada
     * @return lista a mostra en la tabla
     */
    protected abstract @NotNull List<Entity> getEntities(@NotNull Session session);

    /**
     * prepara la vista para crear un entidad
     */
    protected abstract void prepareToAdd();

    /**
     * Prepara la vista para editar un entidad
     */
    protected abstract void prepareToEdit();

    protected abstract void onSelected(Entity entity);
    protected abstract void onDeselected();

    protected abstract void delete();
    protected abstract void save();
    protected abstract void update();

    /**
     * Busca los datos de forma async y los insertar en el modelo de la tabla
     */
    protected class DataLoader extends TableDataLoaderWorker<Entity> {
        @Override
        protected void onSuccess(List<Entity> result) {
            tblModel.setData(result);
        }

        @Override
        protected void onFailure() {
            showError(error.getMessage(), error);
        }

        @Override
        protected List<Entity> inTransaction(Session session) throws ApplicationException {
            return getEntities(session);
        }
    }
    protected abstract class CRUDWorker extends HibernateAsyncWorker {
        @Override
        protected void onFailure() {
            showError(error.getMessage(), error);
        }

        @Override
        protected void onSuccess() {
            loadData();
        }
    }
    public class RemoveWorker<Entity> extends CRUDWorker {
        @NotNull
        protected final Entity entity;

        public RemoveWorker(@NotNull Entity entity) {
            this.entity = entity;
        }

        @Override
        protected void inTransaction(Session session) throws ApplicationException {
            session.remove(session.merge(this.entity));
        }
    }
    public abstract class MergeWorker<Entity, FormData> extends CRUDWorker {
        @NotNull
        protected final Entity entity;
        @NotNull
        protected final FormData dto;

        public MergeWorker(@NotNull Entity entity, @NotNull FormData dto) {
            this.entity = entity;
            this.dto = dto;
        }
    }
    public abstract class PersistWorker<FormData> extends CRUDWorker {
        @NotNull
        protected final FormData dto;

        public PersistWorker(@NotNull FormData dto) {
            this.dto = dto;
        }

    }

}
