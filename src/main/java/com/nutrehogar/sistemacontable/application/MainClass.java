package com.nutrehogar.sistemacontable.application;

import com.nutrehogar.sistemacontable.application.config.ConfigLoader;
import com.nutrehogar.sistemacontable.ui.ThemeConfig;

public class MainClass {
    public static void main(String[] args) {
        ConfigLoader.createDirectories();
        System.setProperty("LOG_DIR", ConfigLoader.Props.DIR_LOG_NAME.getPath().toString());
        ThemeConfig.setup();
        new App();
    }
}
