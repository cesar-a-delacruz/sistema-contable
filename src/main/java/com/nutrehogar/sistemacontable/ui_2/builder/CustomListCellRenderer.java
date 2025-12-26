package com.nutrehogar.sistemacontable.ui_2.builder;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.model.DocumentType;
import com.nutrehogar.sistemacontable.model.Permission;

import javax.swing.*;
import java.awt.*;

public class CustomListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setText(switch (value) {
            case AccountType accountType -> AccountType.getCellRenderer(accountType);
            case AccountSubtype tipoCuenta -> tipoCuenta.getFormattedNumber() + " " + tipoCuenta.getName();
            case Account account -> account.getFormattedNumber();
            case DocumentType documentType -> documentType.getName();
            case Permission permissions -> permissions.getName();
            case null -> "";
            default -> value.toString();
        });
        return label;
    }
}