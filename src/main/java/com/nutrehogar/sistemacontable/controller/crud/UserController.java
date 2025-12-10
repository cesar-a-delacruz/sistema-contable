package com.nutrehogar.sistemacontable.controller.crud;

import com.nutrehogar.sistemacontable.base.controller.CRUDController;
import com.nutrehogar.sistemacontable.base.domain.repository.UserRepository;
import com.nutrehogar.sistemacontable.domain.model.User;
import com.nutrehogar.sistemacontable.domain.type.PermissionType;
import com.nutrehogar.sistemacontable.report.ReportService;
import com.nutrehogar.sistemacontable.ui.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui.builder.CustomListCellRenderer;
import com.nutrehogar.sistemacontable.ui.view.crud.DefaultUserView;

import lombok.Getter;

import javax.swing.*;
import org.jetbrains.annotations.NotNull;

@Getter
public class UserController extends CRUDController<User, Integer> {
    public CustomComboBoxModel<PermissionType> cbxModelPermissions;

    public UserController(UserRepository repository, DefaultUserView view, ReportService reportService, User user) {
        super(repository, view, reportService, user);
        prepareToAdd();
    }

    @Override
    public void initialize() {
        setTblModel(new CustomTableModel("Nombre", "Contraseña", "Habilitado", "Permiso") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var user = getData().get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> user.getUsername();
                    case 1 -> user.getPassword();
                    case 2 -> user.isEnable();
                    case 3 -> user.getPermissions();
                    default -> "que haces?";
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 2 -> Boolean.class;
                    case 3 -> PermissionType.class;
                    default -> String.class;
                };
            }
        });
        cbxModelPermissions = new CustomComboBoxModel<>(PermissionType.values());
        super.initialize();
    }

    @Override
    public void setupViewListeners() {
        super.setupViewListeners();
        getCbxPermissions().setRenderer(new CustomListCellRenderer());
        getCbxPermissions().setModel(cbxModelPermissions);
    }

    @Override
    public void prepareToEdit() {
        super.prepareToEdit();
        getTxtUsername().setText(getSelected().getUsername());
        getTxtPassword().setText(getSelected().getPassword());
        getChkIsEnable().setSelected(getSelected().isEnable());
        getCbxPermissions().setSelectedItem(getSelected().getPermissions());
    }

    @Override
    public void prepareToAdd() {
        super.prepareToAdd();
        getCbxPermissions().setSelectedIndex(0);
        getChkIsEnable().setSelected(true);
        getTxtUsername().setText("");
        getTxtPassword().setText("");
    }

    @Override
    public Integer prepareToDelete() {
        return getSelected().getId();
    }

    @Override
    public User prepareToSave() {
        if (!checkFields())
            return null;
        return getByForm(new User());
    }

    @Override
    public User prepareToUpdate() {
        if (!checkFields())
            return null;
        getByForm(getSelected());
        return getSelected();
    }

    public @NotNull User getByForm(User user) {
        if (user == null)
            user = new User();
        user.setUser(this.user);
        user.setPermissions(cbxModelPermissions.getSelectedItem());
        user.setUsername(getTxtUsername().getText());
        user.setPassword(getTxtPassword().getText());
        user.setEnable(getChkIsEnable().isSelected());
        return user;
    }

    public boolean checkFields() {
        if (cbxModelPermissions.getSelectedItem() == null || getTxtUsername().getText().isBlank()
                || getTxtPassword().getText().isBlank()) {
            showMessage("Ningun campo puede estar vació.");
            return false;
        }
        if (getTxtUsername().getText().length() < User.MIN_LENGTH) {
            showMessage("El nombre de usuario no puede ser menor a: " + User.MIN_LENGTH);
            return false;
        }
        if (getTxtPassword().getText().length() < User.MIN_LENGTH) {
            showMessage("El contraseña no puede ser menor a: " + User.MIN_LENGTH);
            return false;
        }
        if (getTxtUsername().getText().length() > User.MAX_LENGTH) {
            showMessage("El nombre de usuario no puede ser mayor a: " + User.MAX_LENGTH);
            return false;
        }
        if (getTxtPassword().getText().length() > User.MAX_LENGTH) {
            showMessage("La contraseña no puede ser mayor a: " + User.MAX_LENGTH);
            return false;
        }
        return true;
    }

    @Override
    public DefaultUserView getView() {
        return (DefaultUserView) super.getView();
    }

    @Override
    public UserRepository getRepository() {
        return (UserRepository) super.getRepository();
    }

    public JComboBox<PermissionType> getCbxPermissions() {
        return getView().getCbxPermissions();
    }

    public JTextField getTxtUsername() {
        return getView().getTxtUsername();
    }

    public JTextField getTxtPassword() {
        return getView().getTxtPassword();
    }

    public JCheckBox getChkIsEnable() {
        return getView().getChkIsEnable();
    }
}
