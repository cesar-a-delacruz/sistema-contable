package com.nutrehogar.sistemacontable.application;

import com.nutrehogar.sistemacontable.application.config.Theme;
import com.nutrehogar.sistemacontable.HibernateUtil;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;

import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.model.Permission;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.ui.crud.AccountSubtypeView;
import com.nutrehogar.sistemacontable.ui.DashboardView;
import com.nutrehogar.sistemacontable.ui.service.AuthView;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class App {
    @NotNull
    private JFrame frame;

    public App() {
        var adminUser = new User("0922","Root",true, Permission.ADMIN,"Roo");
//        Thread.startVirtualThread(()->{
//            HibernateUtil.getSessionFactory().inTransaction(session -> {
//                session.persist(new AccountSubtype("Ejemlo1",AccountType.ASSETS,user.getUsername()));
//                session.persist(new AccountSubtype("Ejemlo2",AccountType.ASSETS,user.getUsername()));
//                session.persist(new AccountSubtype("Ejemlo3",AccountType.ASSETS,user.getUsername()));
//                session.persist(new AccountSubtype("Ejemlo4",AccountType.ASSETS,user.getUsername()));
//            });
//        });


        Thread.startVirtualThread(() -> Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HibernateUtil.shutdown();
            log.info("Application stopped");
        })));

//        var context = new Context();


        try {
//            AppConfig.setup(context);
            frame = new JFrame();
            frame.setIconImage(new FlatSVGIcon("svgs/SistemaContableLogo.svg", 250, 250).getImage());
            frame.setTitle("Sistema Contable");
            frame.setSize(1300, 600);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.getRootPane().setBackground(Theme.Palette.SOLITUDE_50);
//            frame.add(context.getBean(DashboardView.class));
//            frame.add(new AccountSubtypeView(user));
            var dashBoard = new DashboardView(adminUser);
            frame.add(dashBoard);
            frame.setVisible(true);

            var authView = new AuthView(frame,true, adminUser);
            authView.setVisible(true);
            var authUser = authView.getAutenicateUser();
            dashBoard.setUser(authUser);
            IO.println("AuthView");

//            context.getBean(AuthView.class).setVisible(true);
//            var user = context.getBean(AuthController.class).getAuthenticatedUser();
            frame.setTitle("Sistema Contable - " + authUser.getUsername());
//            AppConfig.init(context, HibernateUtil.getSession(), user, frame);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}