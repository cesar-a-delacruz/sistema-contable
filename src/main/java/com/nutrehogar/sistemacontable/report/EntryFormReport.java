package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.report.dto.JournalEntryReportData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jetbrains.annotations.NotNull;

import static com.nutrehogar.sistemacontable.config.Util.*;

public sealed class EntryFormReport extends Report<JournalEntryReportData> permits PaymentVoucherReport, RegistrationFormReport {
    protected EntryFormReport(String name, String templateName, Path dirPath) throws ReportException {
        super(name, templateName, dirPath);
    }

    @Override
    protected void setProps(@NotNull Map<String, Object> parameters, @NotNull JournalEntryReportData dto) {
        parameters.put("DOC", dto.doc());
        parameters.put("DATE", dto.date().format(LARGE_DATE_FORMATTER));
        parameters.put("NAME", dto.name());
        parameters.put("CONCEPT", dto.concept());
        parameters.put("AMOUNT", dto.amount());
        parameters.put("CHECK_NUMBER", dto.checkNumber());
        parameters.put("TABLE_DATA_SOURCE", new JRBeanCollectionDataSource(dto.rows()));
    }

    @Override
    protected @NotNull Path getDirReportPath(@NotNull JournalEntryReportData dto) throws ReportException {
        var path = dirPath.resolve(String.valueOf(dto.date().getYear()));
        try {
            Files.createDirectories(path);
            return path.resolve(String.format("%s %s %s.pdf", name, dto.doc(), dto.date().format(LARGE_DATE_FORMATTER)));
        } catch (Exception e) {
            throw new ReportException(LabelBuilder.build("Error al intentar guardar el pdf en la carpeta: " + path), e);
        }
    }
}