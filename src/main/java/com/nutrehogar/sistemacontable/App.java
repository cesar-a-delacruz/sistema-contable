package com.nutrehogar.sistemacontable;

import com.nutrehogar.sistemacontable.config.Theme;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;

import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.ui.DashboardView;
import com.nutrehogar.sistemacontable.ui.service.AuthView;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class App {
    @NotNull
    private JFrame frame;

    public App() {
        var adminUser = new User("0922", "Root", true, Permission.ADMIN, "Roo");

        HibernateUtil.getSessionFactory();

        Thread.startVirtualThread(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HibernateUtil.shutdown();
            log.info("Application stopped");
        })));
//        HibernateUtil.getSessionFactory().inTransaction(session -> {
//            for (int i = 1000; i < 9999; i++) {
//                session.persist(new AccountSubtype(AccountNumber.generateNumber(String.valueOf(i), AccountType.ASSETS), "activo " + i, AccountType.ASSETS, "Root"));
//            }
//            for (int i = 1000; i < 9999; i++) {
//                session.persist(new AccountSubtype(AccountNumber.generateNumber(String.valueOf(i), AccountType.COST), "costo " + i, AccountType.COST, "Root"));
//            }
//            for (int i = 1000; i < 9999; i++) {
//                session.persist(new AccountSubtype(AccountNumber.generateNumber(String.valueOf(i), AccountType.EQUITY), "patrimonio " + i, AccountType.EQUITY, "Root"));
//            }
//        });
//        HibernateUtil.getSessionFactory().inTransaction(session -> {
//            for (int i = 1000; i < 9999; i++) {
//                session.persist(new Account(AccountNumber.generateNumber(String.valueOf(i), AccountType.ASSETS), "activo " + i, AccountType.ASSETS, "Root"));
//            }
//            for (int i = 1000; i < 9999; i++) {
//                session.persist(new Account(AccountNumber.generateNumber(String.valueOf(i), AccountType.COST), "costo " + i, AccountType.COST, "Root"));
//            }
//            for (int i = 1000; i < 9999; i++) {
//                session.persist(new Account(AccountNumber.generateNumber(String.valueOf(i), AccountType.EQUITY), "patrimonio " + i, AccountType.EQUITY, "Root"));
//            }
//        });


        try {
            frame = new JFrame();
            frame.setIconImage(new FlatSVGIcon("svgs/SistemaContableLogo.svg", 250, 250).getImage());
            frame.setTitle("Sistema Contable");
            frame.setSize(1300, 600);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.getRootPane().setBackground(Theme.Palette.SOLITUDE_50);
            var dashBoard = new DashboardView(adminUser);
            frame.add(dashBoard);
            frame.setVisible(true);

            var authView = new AuthView(frame, true, adminUser);
            authView.setVisible(true);
            var authUser = authView.getAutenicateUser();
            dashBoard.setUser(authUser);
            frame.setTitle("Sistema Contable - " + authUser.getUsername());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}