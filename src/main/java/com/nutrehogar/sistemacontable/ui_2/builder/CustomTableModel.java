package com.nutrehogar.sistemacontable.ui_2.builder;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public abstract class CustomTableModel<T> extends AbstractTableModel {
    protected final String[] columnsNames;
    @Getter
    protected final List<T> data = new ArrayList<>();

    public CustomTableModel(@NotNull List<T> data, @NotNull String... columnsNames) {
        this.columnsNames = columnsNames;
        setData(data);
    }
    public CustomTableModel(@NotNull String... columnsNames) {
        this.columnsNames = columnsNames;
    }



    public final void setData(@NotNull List<T> data) {
        this.data.clear();
        this.data.addAll(data);
        fireTableDataChanged();
    }

    public final void addData(@NotNull T element) {
        this.data.add(element);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnsNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnsNames[column];
    }
}
