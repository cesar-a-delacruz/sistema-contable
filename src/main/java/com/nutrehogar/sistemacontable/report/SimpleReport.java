package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.report.dto.JournalReportData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jetbrains.annotations.NotNull;

import static com.nutrehogar.sistemacontable.config.Util.*;

public abstract sealed class SimpleReport<Row> extends Report<JournalReportData<Row>> permits JournalReport, TrialBalanceReport {
    protected SimpleReport(String name, String templateName, Path dirPath) throws ReportException {
        super(name, templateName, dirPath);
    }

    @Override
    protected void setProps(@NotNull Map<String, Object> parameters, @NotNull JournalReportData<Row> dto) {
        parameters.put("DATE", dto.date().format(LARGE_MONTH_YEAR_FORMATTER));
        parameters.put("TABLE_DATA_SOURCE", new JRBeanCollectionDataSource(dto.rows()));
    }

    @Override
    protected @NotNull Path getDirReportPath(@NotNull JournalReportData<Row> dto) {
        var path = dirPath.resolve(String.valueOf(dto.date().getYear()));
        try {
            Files.createDirectories(path);
            return path.resolve(String.format("%s %s.pdf", name, dto.date().format(LARGE_MONTH_YEAR_FORMATTER)));
        } catch (Exception e) {
            throw new ReportException(LabelBuilder.build("Error al intentar guardar el pdf en la carpeta: " + path), e);
        }
    }
}
