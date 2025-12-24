package com.nutrehogar.sistemacontable.ui;


import com.nutrehogar.sistemacontable.exception.AppException;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.model.User;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class SimpleView<Entity> extends JPanel {
    @NotNull
    protected final User user;
    @NotNull
    protected List<Entity> data = new ArrayList<>();
    protected Optional<Entity> selected;
    protected AbstractTableModel tblModel;

    public SimpleView(@NotNull User user) {
        this.user = user;
        this.selected = Optional.empty();
    }

    public void showMessage(@NotNull Object message,@NotNull String title) {
        log.info(title, message);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }


    public void showMessage(@NotNull Object message) {
        showMessage(message, "Message");
    }

    public void showError(@NotNull String message,@Nullable AppException cause) {
        if (cause != null)
            log.error(cause.getMessage(), cause);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showError(@NotNull String message) {
        showError(message, null);
    }

    /**
     * Esta funcion debe retornar una lista de {@code Entity} que debe er buscada en la base de datos, esta se ejecuta en el {@link DataLoader}
     * @return lista de {@code Entity} que se busco en la base de datos
     */
    protected abstract List<Entity> findEntities();

    /**
     * Funcion que optiene la nueva lista y la carga en el modelo de la tabla principal {@code tblModel}
     */
    protected void loadData() {
        new DataLoader().execute();
    }

    protected class DataLoader extends SwingWorker<List<Entity>, Void> {
        @Override
        protected@NotNull List<Entity> doInBackground() {
            return findEntities();
        }

        @Override
        protected void done() {
            try {
                if (get() == null || get().isEmpty() || get().getFirst() == null) {
                    data = List.of();
                    return;
                }
                data = get();
                tblModel.fireTableDataChanged();
            } catch (Exception e) {
                showError("Error al cargar datos", new ApplicationException("Failure to find", e));
            }
        }
    }

    public abstract class CustomTableModel extends AbstractTableModel {
        @NotNull
        private final String[] columnsNames;

        public CustomTableModel(@NotNull String... columnsNames) {
            this.columnsNames = columnsNames;
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
}