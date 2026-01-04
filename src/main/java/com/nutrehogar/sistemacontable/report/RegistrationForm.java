package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;

public final class RegistrationForm extends EntryForm {
    public RegistrationForm() throws ReportException {
        super("Formulario de Registro",
                "RegistrationForm.jrxml",
                ConfigLoader.Props.DIR_REGISTRATION_FORM_NAME.getPath());
    }
}
