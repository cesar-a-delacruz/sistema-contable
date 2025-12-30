package com.nutrehogar.sistemacontable.ui.crud;

import org.jetbrains.annotations.NotNull;

public interface CRUDView<Entity, FormData> {

    /**
     * Busca los datos en la db y los agrega a la tabla
     */
    void loadData();

    /**
     * Optiene los datos del formualario para actualizar o crear una entidad
     *
     * @return DTO con los datos basicos de la enidad
     */
    @NotNull FormData getDataFromForm();

    /**
     * Inserta los datos de la entidad en el formulario
     *
     * @param entity Entidad que se editara
     */
    void setEntityDataInForm(@NotNull Entity entity);

    /**
     * prepara la vista para crear un entidad
     */
    void prepareToAdd();

    /**
     * Prepara la vista para editar un entidad
     */
    void prepareToEdit();

    void onSelected(@NotNull  Entity entity);

    void onDeselected();

    void delete();

    void save();

    void update();

}
