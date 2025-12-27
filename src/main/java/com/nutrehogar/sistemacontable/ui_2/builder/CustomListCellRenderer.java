package com.nutrehogar.sistemacontable.ui_2.builder;

import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.ui.crud.AccountSubtypeMinData;

import javax.swing.*;
import java.awt.*;

public class CustomListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setText(switch (value) {
            case AccountType type -> type.getCellRenderer();
            case AccountSubtype subtype -> subtype.getFormattedNumber() + " " + subtype.getName();
            case Account a -> a.getFormattedNumber();
            case DocumentType dt -> dt.getName();
            case Permission p -> p.getName();
            case AccountSubtypeMinData dto -> AccountNumber.getFormattedNumber(dto.number()) + " " + dto.name();
            case null -> "";
            default -> value.toString();
        });
        return label;
    }
}