package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.report.dto.JournalEntryReportData;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class RegistrationFormReport extends EntryFormReport {
    private static RegistrationFormReport instance;
    private RegistrationFormReport() throws ReportException {
        super("Formulario de Registro",
                "RegistrationForm.jrxml",
                ConfigLoader.Props.DIR_REGISTRATION_FORM_NAME.getPath());
    }
    private static RegistrationFormReport getInstance() throws ReportException {
        if (instance == null)
            instance = new RegistrationFormReport();

        return instance;
    }
    @NotNull
    public static Path generate(@NotNull User user, @NotNull JournalEntryReportData dto) throws ReportException {
        try {
            return getInstance().generateReport(user, dto);
        } catch (ReportException e) {
            instance = null;
            throw e;
        }    }
}
