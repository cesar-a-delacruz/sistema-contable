package com.nutrehogar.sistemacontable.ui_2.view.service;

import com.nutrehogar.sistemacontable.ui_2.view.View;
import com.nutrehogar.sistemacontable.model.User;
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
