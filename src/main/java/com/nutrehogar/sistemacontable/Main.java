package com.nutrehogar.sistemacontable;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.model.Permission;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.ui.DashboardView;
import com.nutrehogar.sistemacontable.ui.service.AuthView;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Main {
    void main() {
        ConfigLoader.createDirectories();
        System.setProperty("LOG_DIR", ConfigLoader.Props.DIR_LOG_NAME.getPath().toString());
        var log = LoggerFactory.getLogger(Main.class);
        Theme.setup();

        var adminUser = new User("0922", "Root", true, Permission.ADMIN, "Roo");

        HibernateUtil.getSessionFactory();

        Thread.startVirtualThread(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HibernateUtil.shutdown();
            log.info("Application stopped");
        })));


        try {
            var frame = new JFrame();
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
