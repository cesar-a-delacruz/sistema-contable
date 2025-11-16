package com.nutrehogar.sistemacontable.controller.service;

import com.nutrehogar.sistemacontable.application.config.Context;
import com.nutrehogar.sistemacontable.application.config.PasswordHasher;
import com.nutrehogar.sistemacontable.base.controller.Controller;
import com.nutrehogar.sistemacontable.base.domain.repository.UserRepository;
import com.nutrehogar.sistemacontable.base.ui.view.service.AuthView;
import com.nutrehogar.sistemacontable.domain.model.User;
import com.nutrehogar.sistemacontable.exception.ApplicationException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AuthController extends Controller {
    private final UserRepository userRepository;
    private final DefaultListModel<User> userListModel;
    private final Context context;
    @Getter
    private User authenticatedUser;
    private User adminUser;

    public AuthController(AuthView view, UserRepository userRepository, User adminUser, Context context) {
        super(view);
        this.userRepository = userRepository;
        this.adminUser = adminUser;
        this.context = context;
        this.userListModel = new DefaultListModel<>();
        initialize();
    }

    @Override
    protected void initialize() {
        getLstUser().setModel(userListModel);
        SwingUtilities.invokeLater(() -> getBtnOk().setEnabled(false));
        new SwingWorker<ArrayList<User>, Void>() {
            @Override
            protected ArrayList<User> doInBackground() {
                return new ArrayList<>(userRepository.findAll()); // Carga en background
            }

            @Override
            protected void done() {
                try {
                    userListModel.addAll(get());
                    userListModel.addElement(adminUser);
                    if (!userListModel.isEmpty()) {
                        getLstUser().setSelectedIndex(0);
                        getBtnOk().setEnabled(true);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    showError("Error al cargar datos de usuario",
                            new ApplicationException("Failure to find all users.", e));
                }
            }
        }.execute();
        setupViewListeners();
    }

    @Override
    protected void setupViewListeners() {
        getBtnOk().addActionListener(e -> {
            if (userListModel.isEmpty())
                return;
            User selectedUser = getLstUser().getSelectedValue();
            if (selectedUser != null) {
                String passwordEntered = String.valueOf(getTxtPing().getPassword());
                String passwordStored = selectedUser.getPassword();

                boolean passwordCorrect = false;

                if (passwordStored != null && passwordStored.startsWith("$2a$")) {
                    passwordCorrect = PasswordHasher.verifyPassword(passwordEntered, passwordStored);
                } else {
                    passwordCorrect = passwordEntered.equals(passwordStored);
                }

                if (passwordCorrect) {
                    authenticatedUser = selectedUser;
                    authenticatedUser.setUser(authenticatedUser);

                    context.removeBean(User.class);
                    context.registerBean(User.class, authenticatedUser);
                    context.getBean(DashboardController.class).refreshPermissions();

                    getView().setVisible(false);
                    getView().dispose();
                } else {
                    showMessage("Contraseña Incorrecta.");
                }
            }
        });
        getBtnCancel().addActionListener(e -> {
            log.info("System.exit");
            System.exit(0);
        });
    }

    @Override
    public AuthView getView() {
        return (AuthView) super.getView();
    }

    public JList<User> getLstUser() {
        return getView().getLstUser();
    }

    public JButton getBtnOk() {
        return getView().getBtnOk();
    }

    public JButton getBtnCancel() {
        return getView().getBtnCancel();
    }

    public JPasswordField getTxtPing() {
        return getView().getTxtPing();
    }
}
