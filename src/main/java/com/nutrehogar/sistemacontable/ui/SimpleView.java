package com.nutrehogar.sistemacontable.ui;

import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableModel;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

/**
 * Clase de vista qe se centra en una clase concreta.
 * @param <Entity> Entidad que mostra la {@link javax.swing.JTable} en la vista
 */
@Slf4j
public abstract class SimpleView<Entity> extends View {
    /**
     * Modelo que se usara en la Tabla
     */
    protected CustomTableModel<Entity> tblModel;
    /**
     * Nombre de la entidad, se usara para mostrar en la UI
     */
    @NotNull
    protected final String entityName;
    public SimpleView(@NotNull User user, @NotNull String entityName) {
        super(user);
        this.entityName = entityName;
    }

}