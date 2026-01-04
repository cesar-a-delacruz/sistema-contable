package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.report.dto.JournalReportDTO;

public class Journal extends SimpleReport<JournalReportDTO> {
    public Journal() throws ReportException {
        super("Libro Diario",
                "Journal.jrxml",
                ConfigLoader.Props.DIR_JOURNAL_NAME.getPath());
    }
}
