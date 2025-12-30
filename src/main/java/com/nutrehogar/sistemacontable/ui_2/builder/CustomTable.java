package com.nutrehogar.sistemacontable.ui_2.builder;

import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomTable<T> extends JTable {
    @Getter
    @NotNull
    protected Optional<T> selected = Optional.empty();
    @Setter
    @Nullable
    protected Runnable onFailed;
    @Setter
    @Nullable
    protected Consumer<T> onSelected;
    @Setter
    protected Runnable onDeselected;

    {
        setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        setDefaultRenderer(BigDecimal.class, new CustomTableCellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setEmpty();
                if (rowAtPoint(e.getPoint()) == -1) return;
                int selectedRow = getSelectedRow();
                if (selectedRow < 0) return;
                selected = Optional.ofNullable(getModel().getData().get(selectedRow));
                if (selected.isPresent())
                    if (onSelected != null) onSelected.accept(selected.get());
                    else if (onFailed != null) onFailed.run();
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

    public void setEmpty() {
        selected = Optional.empty();
        if (onDeselected != null)
            onDeselected.run();
    }

    @Override
    public CustomTableModel<T> getModel() {
        return (CustomTableModel<T>) super.getModel();
    }
}
