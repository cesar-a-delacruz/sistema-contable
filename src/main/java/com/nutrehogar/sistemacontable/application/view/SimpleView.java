package com.nutrehogar.sistemacontable.application.view;

import javax.swing.*;

import com.nutrehogar.sistemacontable.ui.components.AuditablePanel;

public abstract class SimpleView extends JPanel implements View {
    public abstract JTable getTblData();

    public abstract JButton getBtnEdit();

    public abstract AuditablePanel getAuditablePanel();

    public abstract JButton getBtnGenerateReport();
}