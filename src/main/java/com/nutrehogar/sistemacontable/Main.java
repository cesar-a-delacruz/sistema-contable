package com.nutrehogar.sistemacontable;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.config.PasswordHasher;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.query.BussinessQuery_;
import com.nutrehogar.sistemacontable.ui.DashboardView;
import com.nutrehogar.sistemacontable.ui.service.AuthView;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.time.LocalDate;

public class Main {
    void main() {
        ConfigLoader.createDirectories();
        System.setProperty("LOG_DIR", ConfigLoader.Props.DIR_LOG_NAME.getPath().toString());
        var log = LoggerFactory.getLogger(Main.class);
        Theme.setup();

        var adminUser = new User("0922", "Root", true, Permission.ADMIN, "Roo");

        Thread.startVirtualThread(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HibernateUtil.shutdown();
            log.info("Application stopped");
        })));

//        HibernateUtil
//                .getSessionFactory()
//                .inTransaction(session ->
//                        session.persist(new AccountingPeriod(2025,  LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), false, adminUser.getUsername())));
//
//        HibernateUtil
//                .getSessionFactory()
//                .inTransaction(session -> {
//                    for (var type : AccountType.values()) {
//                        for (int i = 1000; i < 1010; i++) {
//                            session.persist(new AccountSubtype(Account.generateNumber(String.valueOf(i), type), type.getName() + " " + i, type, "Root"));
//                        }
//                    }
//                });
//        HibernateUtil
//                .getSessionFactory()
//                .inTransaction(session -> {
//                    for (var type : AccountType.values()) {
//                        for (int i = 1000; i < 1020; i++) {
//                            session.persist(new Account(Account.generateNumber(String.valueOf(i), type), type.getName() + " " + i, type, "Root"));
//                        }
//                    }
//                });
//
//
//        HibernateUtil
//                .getSessionFactory()
//                .inTransaction(session -> {
//                    for (int i = 1000; i < 1020; i++) {
//                        session.persist(new User(PasswordHasher.hashPassword("123456"), "user-" + i, i % 2 == 0, Permission.CONTRIBUTE, adminUser.getUsername()));
//                    }
//                });

        try {
            var frame = new JFrame();
            frame.setIconImage(new FlatSVGIcon("svgs/SistemaContableLogo.svg", 250, 250).getImage());
            frame.setTitle("Sistema Contable");
            frame.setSize(1300, 600);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.getRootPane().setBackground(Theme.Palette.SOLITUDE_100);
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
