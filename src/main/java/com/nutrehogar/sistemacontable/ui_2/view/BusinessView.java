package com.nutrehogar.sistemacontable.ui_2.view;

import javax.swing.*;

import com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner;

public abstract class BusinessView extends SimpleView {
    public abstract LocalDateSpinner getSpnStart();

    public abstract LocalDateSpinner getSpnEnd();

    public abstract JButton getBtnFilter();

    public abstract JButton getBtnResetStart();

    public abstract JButton getBtnResetEnd();
}