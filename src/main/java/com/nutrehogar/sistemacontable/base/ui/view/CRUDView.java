package com.nutrehogar.sistemacontable.base.ui.view;

import javax.swing.*;

public abstract class CRUDView extends SimpleView {
    public abstract JButton getBtnDelete();

    public abstract JButton getBtnSave();

    public abstract JButton getBtnAdd();

    public abstract JButton getBtnUpdate();
}
