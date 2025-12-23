package com.nutrehogar.sistemacontable.ui;


import com.nutrehogar.sistemacontable.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
@Slf4j
public abstract class SimpleView extends JPanel {

    public void showMessage(Object message, String title) {
        log.info(title, message);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void showMessage(Object message) {
        showMessage(message, "Message");
    }

    public void showError(String message, AppException cause) {
        if (cause != null)
            log.error(cause.getMessage(), cause);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showError(String message) {
        showError(message, null);
    }
}