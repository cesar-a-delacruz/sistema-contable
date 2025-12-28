package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.model.AuditableFields;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;
import com.nutrehogar.sistemacontable.ui_2.component.OperationPanel;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Optional;

public abstract class CRUDView<Entity extends AuditableFields, FormData> extends SimpleView<Entity> {
    @NotNull
    protected final String entityName;

    public CRUDView(@NotNull User user, @NotNull String entityName) {
        super(user);
        this.entityName = entityName;
    }

    protected void configureTable(@NotNull JTable tblData) {
        tblData.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        tblData.setDefaultRenderer(BigDecimal.class, new CustomTableCellRenderer());
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tblData.rowAtPoint(e.getPoint());
                if (row != -1) {
                    int selectedRow = tblData.getSelectedRow();
                    if (selectedRow < 0) {
                        deselect();
                        return;
                    }
                    setSelected(data.get(selectedRow));
                }
            }
        });
    }

    protected void deselect() {
        selected = Optional.empty();
    }

    protected void setSelected(@Nullable Entity entity) {
        if (entity == null || !user.isAdmin()) {
            deselect();
            return;
        }
        selected = Optional.of(entity);
        getAuditablePanel().setAuditableFields(entity);
    }

    /**
     * Agregar los ActionListeners a los bonones prepareToAdd prepareToEdit y delete del Operation panel, esta funcion debe llamarse desde del initilizie() en el contructor ya que es en esa funcion en la que se inicilaizan los botones. se se hace antes lanzara un NullPointerExeption
     */
    protected void addListenersToOperationPanel() {
        getOperationPanel().getBtnPrepareToAdd().addActionListener(_ -> prepareToAdd());
        getOperationPanel().getBtnPrepareToEdit().addActionListener(_ -> prepareToEdit());
        getOperationPanel().getBtnDelete().addActionListener(_ -> delete());
    }

    @Override
    protected void loadData() {
        deselect();
        prepareToAdd();
        super.loadData();
    }

    protected void prepareToAdd() {
        resetForm();
    }

    protected void prepareToEdit() {
        selected.ifPresentOrElse(this::setEntityDataInForm, () -> showMessage("Seleccione un elemento de la tabla"));
    }

    protected abstract void delete();

    protected abstract void save();

    protected abstract void update();

    /**
     * Pone los compos del formualrio en su estado pase listo para insertar nuevos datos y limpiando los anteriores.
     */
    protected abstract void resetForm();

    /**
     * Inserta los datos de los txt en la entidad, se debe usar en una transaccion y la entidad que se le pase debe estar en estado {@code persist}, para que al hacer {@code commit} Hibernate actualize los datos en la base de datos
     *
     * @param entity Entidad persistida o en proceso en la base de datos
     */
    protected abstract FormData getDataFromForm();

    /**
     * Rellena los compos del formulario con los datos de la entidad
     *
     * @param entity entidad a editar
     */
    protected abstract void setEntityDataInForm(Entity entity);

    protected abstract AuditablePanel getAuditablePanel();

    protected abstract OperationPanel getOperationPanel();

    protected abstract class CRUDWorker extends SwingWorker<Void, Void> {

        @Nullable
        protected ApplicationException error;

        protected abstract void inTransaction(Session session) throws ApplicationException;

        protected void onSuccess() {
            loadData();
        }

        protected void onFailure() {
            showError(error.getMessage(), error);
        }

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

    protected abstract class SaveWorker extends CRUDWorker {
        @NotNull
        protected final FormData dto;

        public SaveWorker(@NotNull FormData dto) {
            this.dto = dto;
        }

    }

    protected abstract class DeleteWorker extends CRUDWorker {
        @NotNull
        protected final Entity entity;

        public DeleteWorker(@NotNull Entity entity) {
            this.entity = entity;
        }
    }

    protected abstract class EditWorker extends CRUDWorker {
        @NotNull
        protected final Entity entity;
        @NotNull
        protected final FormData dto;

        public EditWorker(@NotNull Entity entity, @NotNull FormData dto) {
            this.entity = entity;
            this.dto = dto;
        }
    }

}
