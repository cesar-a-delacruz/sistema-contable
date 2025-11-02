package com.nutrehogar.sistemacontable.base.ui.view;

import javax.swing.*;

import com.nutrehogar.sistemacontable.ui.component.AuditablePanel;

public abstract class SimpleView extends JPanel implements View {
    public abstract JTable getTblData();

    public abstract JButton getBtnEdit();

    public abstract AuditablePanel getAuditablePanel();

    public abstract JButton getBtnGenerateReport();
}