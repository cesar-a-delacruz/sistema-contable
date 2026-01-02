package com.nutrehogar.sistemacontable.ui_2.component;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomListCellRenderer;

import javax.swing.*;

/**
 * Combobox personalizado, se usa para definir el tipo de renderizado que tendra
 * cada tipo de dato.
 * <p>
 * Los ya definidos son:
 * <u>
 * <li>{@link AccountType}</li>
 * <li>{@link AccountSubtype}</li>
 * <li>{@link Account}</li>
 * </u>
 *
 * @param <E>
 * @author Calcifer1331
 */
public class CustomComboBox<E> extends JComboBox<E> {
    {
        setRenderer(new CustomListCellRenderer());
    }

    public CustomComboBox() {}

    public CustomComboBox(CustomComboBoxModel<E> model) {
        super(model);
    }
}
