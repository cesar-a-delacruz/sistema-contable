package com.nutrehogar.sistemacontable.ui_2.builder;

import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.ui.crud.AccountSubtypeMinData;

import javax.swing.*;
import java.awt.*;

public class CustomListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        switch (value) {
            case AccountType type -> label.setText(type.getName());
            case AccountSubtype subtype -> {
                setText(subtype.getFormattedNumber());
                setToolTipText(subtype.getName());
            }
            case Account a -> {
                setText(a.getFormattedNumber());
                setToolTipText(a.getName());
            }
            case DocumentType dt -> setText(dt.getName());
            case Permission p -> setText(p.getName());
            case AccountSubtypeMinData dto -> {
                setText(AccountNumber.getFormattedNumber(dto.number()));
                setToolTipText(dto.name());
            }
            case null -> setText("");
            default -> setText(value.toString());
        }
        return label;
    }
}