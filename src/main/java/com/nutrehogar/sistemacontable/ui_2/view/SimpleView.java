package com.nutrehogar.sistemacontable.ui_2.view;

import javax.swing.*;

import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;

public abstract class SimpleView extends JPanel implements View {
    public abstract JTable getTblData();

    public abstract JButton getBtnEdit();

    public abstract AuditablePanel getAuditablePanel();

    public abstract JButton getBtnGenerateReport();
}