package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.report.dto.JournalReportData;
import com.nutrehogar.sistemacontable.report.dto.JournalReportRow;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static com.nutrehogar.sistemacontable.config.Util.LARGE_MONTH_YEAR_FORMATTER;

public final class JournalReport extends SimpleReport<JournalReportRow> {
    private static JournalReport instance;
    private JournalReport() throws ReportException {
        super("Libro Diario",
                "Journal.jrxml",
                ConfigLoader.Props.DIR_JOURNAL_NAME.getPath());
    }

    private static JournalReport getInstance() throws ReportException {
        if (instance == null)
            instance = new JournalReport();

        return instance;
    }

    @NotNull
    public static Path generate(@NotNull User user, @NotNull JournalReportData<JournalReportRow> dto) throws ReportException {
        try {
            return getInstance().generateReport(user, dto);
        } catch (ReportException e) {
            instance = null;
            throw e;
        }
    }
}
