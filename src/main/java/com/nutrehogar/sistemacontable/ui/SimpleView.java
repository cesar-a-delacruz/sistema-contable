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

    @NotNull
    protected final UIEntityInfo entityInfo;
    @NotNull
    protected final String entityName;
    protected SimpleView(@NotNull User user, @NotNull UIEntityInfo entityInfo) {
        super(user);
        this.entityInfo = entityInfo;
        this.entityName = entityInfo.getName().toLowerCase();
    }

}