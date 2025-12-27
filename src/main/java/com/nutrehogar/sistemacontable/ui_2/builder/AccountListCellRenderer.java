package com.nutrehogar.sistemacontable.ui_2.builder;

import com.nutrehogar.sistemacontable.model.Account;
import java.awt.*;
import javax.swing.*;

public class AccountListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setText(switch (value) {
            case Account account -> account.getFormattedNumber() + " " + account.getName();
            case null -> "";
            default -> value.toString();
        });
        return label;
    }
}