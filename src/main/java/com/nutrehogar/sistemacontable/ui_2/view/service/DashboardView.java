package com.nutrehogar.sistemacontable.ui_2.view.service;

import javax.swing.*;
import com.nutrehogar.sistemacontable.ui_2.view.View;

public abstract class DashboardView extends JPanel implements View {
    public abstract JButton getBtnShowFormView();

    public abstract JButton getBtnShowJournalView();

    public abstract JButton getBtnShowTrialBalanceView();

    public abstract JButton getBtnShowGeneralLedgerView();

    public abstract JButton getBtnShowAccountView();

    public abstract JButton getBtnShowAccountSubtypeView();

    public abstract JButton getBtnShowBackupView();

    public abstract JButton getBtnShowUserView();

    public abstract JPanel getPnlContent();

    public abstract JButton getBtnHome();

    public abstract JPanel getPnlHome();

    public abstract JPanel getPnlNav();
}
