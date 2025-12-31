package com.nutrehogar.sistemacontable.ui_2.builder;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Vector;
import javax.swing.*;

public class CustomComboBoxModel<E> extends DefaultComboBoxModel<E> {
    public CustomComboBoxModel(@NotNull List<E> data) {
        super(new Vector<>(data));
        if (!data.isEmpty())
            setSelectedItem(data.getFirst());
    }

    public CustomComboBoxModel(@NotNull E[] data) {
        super(data);
        setSelectedItem(data[0]);
    }
    public CustomComboBoxModel(){
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getSelectedItem() {
        return (E) super.getSelectedItem();
    }

    /**
     * Metodo para agregar los datos a la lista que se mostraran
     *
     * @param data
     *            lista de subtipo cuentas a mostrar en el combo box
     */
    public void setData(List<E> data) {
        this.removeAllElements();

        if (data == null) return;

        for (var e : data) {
            this.addElement(e);
        }

//        if (data.isEmpty()) return;

//        setSelectedItem(data.getFirst());

//        fireContentsChanged(this, 0, data.size());
    }
}