package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;

public final class RegistrationFormReport extends EntryFormReport {
    private static RegistrationFormReport instance;
    private RegistrationFormReport() throws ReportException {
        super("Formulario de Registro",
                "RegistrationForm.jrxml",
                ConfigLoader.Props.DIR_REGISTRATION_FORM_NAME.getPath());
    }
    public static RegistrationFormReport getInstance() throws ReportException {
        if (instance == null) {
            instance = new RegistrationFormReport();
        }
        return instance;
    }
}
