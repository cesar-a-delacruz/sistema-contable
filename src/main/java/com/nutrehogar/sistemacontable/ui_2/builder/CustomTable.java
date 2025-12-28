package com.nutrehogar.sistemacontable.ui_2.builder;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Optional;

public class CustomTable<T> extends JTable {
    protected Optional<T> selected = Optional.empty();

    {
        setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        setDefaultRenderer(BigDecimal.class, new CustomTableCellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selected = Optional.empty();
                int row = rowAtPoint(e.getPoint());
                if (row != -1) {
                    int selectedRow = getSelectedRow();
                    if (selectedRow < 0) {
                        onFailed();
                        return;
                    }
                    selected = Optional.ofNullable(getModel().getData().get(selectedRow));
                    selected.ifPresentOrElse(CustomTable.this::onSelected, CustomTable.this::onFailed);
                }
            }
        });
    }

    public CustomTable(@NotNull CustomTableModel<T> model) {
        super(model);
    }

    public CustomTable() {
        super(new CustomTableModel<String>("Nada") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return data.get(rowIndex);
            }
        });
    }

    @Override
    public CustomTableModel<T> getModel() {
        return (CustomTableModel<T>) super.getModel();
    }

    protected void onFailed() {
    }

    protected void onSelected(@NotNull T entity) {
    }
}
