package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.report.dto.JournalEntryReport;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jetbrains.annotations.NotNull;

import static com.nutrehogar.sistemacontable.config.Util.*;

public sealed class EntryFormReport extends Report<JournalEntryReport> permits PaymentVoucherReport, RegistrationFormReport {
    protected EntryFormReport(String name, String templateName, Path dirPath) throws ReportException {
        super(name, templateName, dirPath);
    }
    @Override
    protected void setProps(@NotNull Map<String, Object> parameters, @NotNull JournalEntryReport dto) {
        parameters.put("DOC", dto.doc());
        parameters.put("DATE", dto.date().format(LARGE_DATE_FORMATTER));
        parameters.put("NAME", dto.name());
        parameters.put("CONCEPT", dto.concept());
        parameters.put("AMOUNT", dto.amount());
        parameters.put("CHECK_NUMBER", dto.checkNumber());
        parameters.put("TABLE_DATA_SOURCE", new JRBeanCollectionDataSource(dto.ledgerRecords()));
    }
    @Override
    protected @NotNull Path getDirReportPath(@NotNull JournalEntryReport dto) {
        return dirPath.resolve(String.format("%s#%s_%s.pdf", name, dto.doc(), dto.date().format(FILE_DATE_FORMATTER)));
    }
}