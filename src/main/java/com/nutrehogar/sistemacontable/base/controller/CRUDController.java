package com.nutrehogar.sistemacontable.base.controller;

import com.nutrehogar.sistemacontable.base.domain.repository.CRUDRepository;
import com.nutrehogar.sistemacontable.base.ui.view.CRUDView;
import com.nutrehogar.sistemacontable.domain.model.AuditableEntity;
import com.nutrehogar.sistemacontable.domain.model.User;
import com.nutrehogar.sistemacontable.exception.RepositoryException;
import com.nutrehogar.sistemacontable.report.ReportService;

import jakarta.persistence.EntityExistsException;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectDeletedException;
import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;

@Slf4j
public abstract class CRUDController<T extends AuditableEntity, ID> extends SimpleController<T, T> {
    public CRUDController(CRUDRepository<T, ID> repository, CRUDView view, ReportService reportService, User user) {
        super(repository, view, reportService, user);
    }

    @Override
    protected void initialize() {
        getBtnAdd().setEnabled(user.isAdmin() || user.isContribuitor());
        getBtnSave().setEnabled(user.isAdmin() || user.isContribuitor());
        super.initialize();
    }

    @Override
    public void loadData() {
        new CRUDDataLoader().execute();
    }

    public class CRUDDataLoader extends DataLoader {

        @Override
        protected List<T> doInBackground() {
            return getRepository().findAll();
        }
    }

    @Override
    protected void setupViewListeners() {
        getBtnSave().addActionListener(e -> save(prepareToSave()));
        getBtnDelete().addActionListener(e -> delete(prepareToDelete()));
        getBtnUpdate().addActionListener(e -> update(prepareToUpdate()));
        getBtnEdit().addActionListener(e -> prepareToEdit());
        getBtnAdd().addActionListener(e -> prepareToAdd());
        deselect();
        super.setupViewListeners();
    }

    private void save(T entity) {
        if (entity == null)
            return;
        try {
            entity.setUser(user);
            getRepository().save(entity);
            loadData(); // Recargar datos después de guardar
            prepareToAdd();
        } catch (RepositoryException e) {
            String fullMessage = switch (e.getCause()) {
                case EntityExistsException c -> "Ya existe esa Cuenta";
                case IllegalArgumentException c -> "Los datos no puede ser nulo";
                case ConstraintViolationException c -> "Codigo de cuenta duplicado";
                case null, default -> e.getMessage();
            };
            showError("Error al guardar: " + fullMessage);
        }
    }

    private void update(T entity) {
        if (entity == null)
            return;
        try {
            getSelected().setUser(user);
            getRepository().update(getSelected());
            loadData(); // Recargar datos después de eliminar
            prepareToAdd();
        } catch (RepositoryException | ExecutionException | InterruptedException e) {
            String fullMessage = switch (e.getCause()) {
                case IllegalArgumentException c -> "Los datos no son validos";
                case ObjectDeletedException c -> "No se puede editar una cuenta eliminada";
                case ConstraintViolationException c -> "Operación no valido";
                case null, default -> e.getMessage();
            };
            showError("Error al guardar: " + fullMessage);
        }
    }

    private void delete(ID id) {
        if (id == null) {
            prepareToAdd();
            return;
        }
        var response = JOptionPane.showConfirmDialog(getView(),
                "Desea eliminar? El cambio sera permanente.",
                "Elimination",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (response != JOptionPane.OK_OPTION)
            return;
        try {
            getRepository().deleteById(id);
            loadData(); // Recargar datos después de eliminar
            prepareToAdd();
        } catch (RepositoryException e) {
            showError("Error al guardar: " + e.getMessage());
        }
    }

    protected void deselect() {
        setSelected(null);
        getBtnDelete().setEnabled(false);
        getBtnEdit().setEnabled(false);
    }

    protected void select() {
        if (!user.isAdmin())
            return;
        getBtnDelete().setEnabled(true);
        getBtnEdit().setEnabled(true);
    }

    @Override
    public void setSelected(T selected) {
        super.setSelected(selected);
        select();
    }

    @Override
    protected void setElementSelected(@NotNull MouseEvent e) {
        int row = getTblData().rowAtPoint(e.getPoint());
        if (row != -1) {
            int selectedRow = getTblData().getSelectedRow();
            if (selectedRow < 0) {
                deselect();
                return;
            }
            setSelected(getData().get(selectedRow));
            setAuditoria();
        }
    }

    @Override
    protected void setAuditoria() {
        SwingUtilities.invokeLater(() -> {
            getAuditablePanel().getLblCreateAt().setText(
                    getSelected().getCreatedAt() == null ? NA : getSelected().getCreatedAt().format(DATE_FORMATTER));
            getAuditablePanel().getLblCreateBy()
                    .setText(getSelected().getCreatedBy() == null ? NA : getSelected().getCreatedBy());
            getAuditablePanel().getLblUpdateAt().setText(
                    getSelected().getUpdatedAt() == null ? NA : getSelected().getUpdatedAt().format(DATE_FORMATTER));
            getAuditablePanel().getLblUpdateBy()
                    .setText(getSelected().getUpdatedBy() == null ? NA : getSelected().getUpdatedBy());
            getAuditablePanel().revalidate();
            getAuditablePanel().repaint();
        });
    }

    protected void prepareToEdit() {
        getBtnUpdate().setEnabled(user.isAdmin());
        getBtnSave().setEnabled(false);
    }

    protected void prepareToAdd() {
        deselect();
        getBtnUpdate().setEnabled(false);
        getBtnSave().setEnabled(user.isAdmin() || user.isContribuitor());
    }

    protected abstract ID prepareToDelete();

    protected abstract T prepareToSave();

    protected abstract T prepareToUpdate();

    @SuppressWarnings("unchecked")
    @Override
    public CRUDRepository<T, ID> getRepository() {
        return (CRUDRepository<T, ID>) super.getRepository();
    }

    @Override
    public CRUDView getView() {
        return (CRUDView) super.getView();
    }

    public JButton getBtnAdd() {
        return getView().getBtnAdd();
    }

    public JButton getBtnUpdate() {
        return getView().getBtnUpdate();
    }

    public JButton getBtnEdit() {
        return getView().getBtnEdit();
    }

    public JButton getBtnDelete() {
        return getView().getBtnDelete();
    }

    public JButton getBtnSave() {
        return getView().getBtnSave();
    }
}
