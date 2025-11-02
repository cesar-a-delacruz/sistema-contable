package com.nutrehogar.sistemacontable.base.ui.view.service;

import javax.swing.*;
import com.nutrehogar.sistemacontable.base.ui.view.View;

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
