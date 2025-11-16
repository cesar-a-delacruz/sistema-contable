package com.nutrehogar.sistemacontable.application;

import com.nutrehogar.sistemacontable.application.config.ConfigLoader;
import com.nutrehogar.sistemacontable.application.config.Theme;

public class Main {
    public static void main(String[] args) {
        ConfigLoader.createDirectories();
        System.setProperty("LOG_DIR", ConfigLoader.Props.DIR_LOG_NAME.getPath().toString());
        Theme.setup();
        new App();
    }
}
