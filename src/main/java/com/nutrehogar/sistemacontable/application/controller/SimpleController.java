package com.nutrehogar.sistemacontable.application.controller;

import com.nutrehogar.sistemacontable.application.config.Util;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.infrastructure.report.ReportService;
import com.nutrehogar.sistemacontable.application.repository.SimpleRepository;
import com.nutrehogar.sistemacontable.domain.model.User;
import com.nutrehogar.sistemacontable.ui.builders.CustomTableCellRenderer;
import com.nutrehogar.sistemacontable.ui.components.AuditablePanel;
import com.nutrehogar.sistemacontable.application.view.SimpleView;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Getter
@Setter
public abstract class SimpleController<T, R> extends Controller {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
    protected final SimpleRepository<R> repository;
    protected List<T> data = new ArrayList<>();
    protected T selected;
    protected AbstractTableModel tblModel;
    protected ReportService reportService;
    protected final User user;
    protected static final String NA = Util.NA;

    public SimpleController(SimpleRepository<R> repository, SimpleView view, ReportService reportService, User user) {
        super(view);
        this.repository = repository;
        this.reportService = reportService;
        this.user = user;
        initialize();
    }

    public abstract class DataLoader extends SwingWorker<List<T>, Void> {

        @Override
        protected void done() {
            try {
                if (get() == null || get().isEmpty() || get().getFirst() == null) {
                    setData(List.of());
                    return;
                }
                setData(get());
                getTblModel().fireTableDataChanged();
            } catch (Exception e) {
                showError("Error al cargar datos", new ApplicationException("Failure to find", e));
            }
        }
    }

    public abstract class CustomTableModel extends AbstractTableModel {
        private final String[] COLUMN_NAMES;

        public CustomTableModel(@NotNull String... COLUMN_NAMES) {
            this.COLUMN_NAMES = COLUMN_NAMES;
        }

        @Override
        public int getRowCount() {
            return getData().size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }
    }

    protected abstract void setElementSelected(@NotNull MouseEvent e);

    protected abstract void setAuditoria();

    @Override
    protected void initialize() {
        getTblData().setModel(getTblModel());
        getTblData().setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        getTblData().setDefaultRenderer(BigDecimal.class, new CustomTableCellRenderer());
        getTblData().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadData();
        setupViewListeners();
    }

    @Override
    protected void setupViewListeners() {
        getTblData().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setElementSelected(e);
            }
        });
    }

    @Override
    public SimpleView getView() {
        return (SimpleView) super.getView();
    }

    protected void updateView() {
        SwingUtilities.invokeLater(getTblModel()::fireTableDataChanged);
    }

    protected void loadData() {
        updateView();
    }

    public JTable getTblData() {
        return getView().getTblData();
    }

    public JButton getBtnEdit() {
        return getView().getBtnEdit();
    }

    public AuditablePanel getAuditablePanel() {
        return getView().getAuditablePanel();
    }

    public JButton getBtnGenerateReport() {
        return getView().getBtnGenerateReport();
    }
}