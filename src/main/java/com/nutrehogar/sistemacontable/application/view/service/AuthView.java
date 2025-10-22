package com.nutrehogar.sistemacontable.application.view.service;

import com.nutrehogar.sistemacontable.application.view.View;
import com.nutrehogar.sistemacontable.domain.model.User;
import javax.swing.*;

public abstract class AuthView extends JDialog implements View {
    public AuthView() {
        setModal(true);
    }

    public abstract JButton getBtnOk();

    public abstract JButton getBtnCancel();

    public abstract JPasswordField getTxtPing();

    public abstract JList<User> getLstUser();
}
