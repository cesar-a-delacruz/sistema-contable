package com.nutrehogar.sistemacontable.base.ui.view;

import javax.swing.*;

import com.nutrehogar.sistemacontable.ui.component.LocalDateSpinner;

public abstract class BusinessView extends SimpleView {
    public abstract LocalDateSpinner getSpnStart();

    public abstract LocalDateSpinner getSpnEnd();

    public abstract JButton getBtnFilter();

    public abstract JButton getBtnResetStart();

    public abstract JButton getBtnResetEnd();
}