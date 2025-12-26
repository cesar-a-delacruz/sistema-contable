package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.model.AuditableEntity;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;
import com.nutrehogar.sistemacontable.ui_2.component.OperationPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Optional;

public abstract class CRUDView<Entity extends AuditableEntity> extends SimpleView<Entity> {
    @NotNull
    protected final String entityName;
    public CRUDView(@NotNull User user,@NotNull String entityName) {
        super(user);
        this.entityName = entityName;
    }

    protected void configureTable(@NotNull JTable tblData){
        tblData.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        tblData.setDefaultRenderer(BigDecimal.class, new CustomTableCellRenderer());
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.addMouseListener(new MouseAdapter(){
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
    protected void addListenersToOperationPanel(){
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

    protected void prepareToAdd(){
        resetForm();
    }
    protected void prepareToEdit(){
        selected.ifPresentOrElse(this::setEntityDataInForm,()->showMessage("Seleccione un elemento de la tabla"));
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
     * @param entity Entidad persistida o en proceso en la base de datos
     */
    protected abstract Entity setEntityDataFromForm(Entity entity);

    /**
     * Rellena los compos del formulario con los datos de la entidad
     * @param entity entidad a editar
     */
    protected abstract Entity setEntityDataInForm(Entity entity);

    protected abstract AuditablePanel getAuditablePanel();
    protected abstract OperationPanel getOperationPanel();
}
