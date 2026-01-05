package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.report.dto.JournalReportData;
import com.nutrehogar.sistemacontable.report.dto.TrialBalanceReportRow;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class TrialBalanceReport extends SimpleReport<TrialBalanceReportRow> {
    private static TrialBalanceReport instance;
    private TrialBalanceReport() throws ReportException {
        super("Balance de Comprobación",
                "TrialBalance.jrxml",
                ConfigLoader.Props.DIR_TRIAL_BALANCE_NAME.getPath());
    }

    private static TrialBalanceReport getInstance() throws ReportException {
        if (instance == null)
            instance = new TrialBalanceReport();

        return instance;
    }

    @NotNull
    public static Path generate(@NotNull User user, @NotNull JournalReportData<TrialBalanceReportRow> dto) throws ReportException {
        try {
            return getInstance().generateReport(user, dto);
        } catch (ReportException e) {
            instance = null;
            throw e;
        }
    }
}