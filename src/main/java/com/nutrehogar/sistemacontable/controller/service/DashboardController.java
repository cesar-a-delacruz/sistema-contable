package com.nutrehogar.sistemacontable.controller.service;

import com.nutrehogar.sistemacontable.application.config.Context;
import com.nutrehogar.sistemacontable.base.controller.Controller;
import com.nutrehogar.sistemacontable.base.ui.view.service.DashboardView;
import com.nutrehogar.sistemacontable.controller.business.*;
import com.nutrehogar.sistemacontable.controller.crud.*;
import com.nutrehogar.sistemacontable.domain.model.User;

import java.awt.*;
import javax.swing.*;

public class DashboardController extends Controller {
    private final Context context;
    private User user;

    public DashboardController(DashboardView view, Context context) {
        super(view);
        this.context = context;
        initialize();
    }

    @Override
    public void initialize() {
        this.user = context.getBean(User.class);
        SwingUtilities.invokeLater(() -> {
            getPnlNav().setVisible(false);
            getPnlContent().setOpaque(false);

            ButtonPermissionSettings();
        });
        Thread.startVirtualThread(this::setupViewListeners);
    }

    public void refreshPermissions() {
        this.user = context.getBean(User.class);
        ButtonPermissionSettings();
    }

    private void ButtonPermissionSettings() {
        if (!user.isAdmin()) {
            getBtnShowUserView().setVisible(false);
            getBtnShowBackupView().setEnabled(false);
        }
    }

    public void setupViewListeners() {
        getBtnShowFormView()
                .addActionListener(e -> setContent(context.getBean(AccountingEntryFormController.class).getView()));
        getBtnShowAccountSubtypeView()
                .addActionListener(e -> setContent(context.getBean(AccountSubtypeController.class).getView()));
        getBtnShowAccountView().addActionListener(e -> {
            setContent(context.getBean(AccountController.class).getView());
            context.getBean(AccountController.class).loadData();
        });
        getBtnShowJournalView().addActionListener(e -> {
            setContent(context.getBean(JournalController.class).getView());
            context.getBean(JournalController.class).loadData();
        });
        getBtnShowTrialBalanceView().addActionListener(e -> {
            setContent(context.getBean(TrialBalanceController.class).getView());
            context.getBean(TrialBalanceController.class).loadData();
        });
        getBtnShowGeneralLedgerView().addActionListener(e -> {
            setContent(context.getBean(GeneralLedgerController.class).getView());
            context.getBean(GeneralLedgerController.class).loadDataSubtype();
            context.getBean(GeneralLedgerController.class).loadDataAccount();
        });
        getBtnShowBackupView().addActionListener(e -> context.getBean(BackupController.class).showView());
        getBtnHome().addActionListener(e -> setContent(getPnlHome()));
        getBtnShowUserView().addActionListener(e -> setContent(context.getBean(UserController.class).getView()));
    }

    public void setContent(JPanel p) {
        SwingUtilities.invokeLater(() -> {
            if (p != getPnlHome()) {
                getPnlNav().setVisible(true);
            } else {
                getPnlNav().setVisible(false);
                getBtnShowFormView().setBackground(Color.WHITE);
                getBtnShowJournalView().setBackground(Color.WHITE);
                getBtnShowTrialBalanceView().setBackground(Color.WHITE);
                getBtnShowGeneralLedgerView().setBackground(Color.WHITE);
                getBtnShowAccountView().setBackground(Color.WHITE);
                getBtnShowAccountSubtypeView().setBackground(Color.WHITE);
            }
            getPnlContent().removeAll();
            getPnlContent().setLayout(new BorderLayout());
            getPnlContent().add(p, BorderLayout.CENTER);
            getPnlContent().revalidate();
            getPnlContent().repaint();
        });
    }

    @Override
    public DashboardView getView() {
        return (DashboardView) super.getView();
    }

    public JButton getBtnShowFormView() {
        return getView().getBtnShowFormView();
    }

    public JButton getBtnShowJournalView() {
        return getView().getBtnShowJournalView();
    }

    public JButton getBtnShowTrialBalanceView() {
        return getView().getBtnShowTrialBalanceView();
    }

    public JButton getBtnShowGeneralLedgerView() {
        return getView().getBtnShowGeneralLedgerView();
    }

    public JButton getBtnShowAccountView() {
        return getView().getBtnShowAccountView();
    }

    public JButton getBtnShowAccountSubtypeView() {
        return getView().getBtnShowAccountSubtypeView();
    }

    public JButton getBtnShowBackupView() {
        return getView().getBtnShowBackupView();
    }

    public JButton getBtnShowUserView() {
        return getView().getBtnShowUserView();
    }

    public JPanel getPnlContent() {
        return getView().getPnlContent();
    }

    public JButton getBtnHome() {
        return getView().getBtnHome();
    }

    public JPanel getPnlHome() {
        return getView().getPnlHome();
    }

    public JPanel getPnlNav() {
        return getView().getPnlNav();
    }
}
