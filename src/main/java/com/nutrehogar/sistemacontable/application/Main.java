package com.nutrehogar.sistemacontable.application;

import com.nutrehogar.sistemacontable.application.config.ConfigLoader;
import com.nutrehogar.sistemacontable.application.config.Theme;

public class Main {
    void main() {
        ConfigLoader.createDirectories();
        System.setProperty("LOG_DIR", ConfigLoader.Props.DIR_LOG_NAME.getPath().toString());
//        Thread.startVirtualThread(HibernateUtil::getSessionFactory);
//        Thread.startVirtualThread(()->HibernateUtil.getSessionFactory().inSession(session -> {}));
//        HibernateUtil.getSessionFactory().inTransaction((session) -> {});
        Theme.setup();
        new App();
    }
}
