package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.report.dto.SimpleReportDTO;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jetbrains.annotations.NotNull;

import static com.nutrehogar.sistemacontable.config.Util.FILE_DATE_FORMATTER;
import static com.nutrehogar.sistemacontable.config.Util.SMALL_DATE_FORMATTER;

public abstract class SimpleReport<D> extends Report<SimpleReportDTO<D>> {
    public SimpleReport(String name, String templateName, Path dirPath) throws ReportException {
        super(name, templateName, dirPath);
    }

    @Override
    protected void setProps(@NotNull Map<String, Object> parameters, @NotNull SimpleReportDTO<D> dto) {
        parameters.put("DATE", LocalDate.now().format(SMALL_DATE_FORMATTER));
        parameters.put("START_DATE", dto.startDate().format(SMALL_DATE_FORMATTER));
        parameters.put("END_DATE", dto.endDate().format(SMALL_DATE_FORMATTER));
        parameters.put("TABLE_DATA_SOURCE", new JRBeanCollectionDataSource(dto.dto()));
    }

    @Override
    protected @NotNull Path getDirReportPath(@NotNull SimpleReportDTO<D> dto) {
        return dirPath.resolve(String.format("%s-%s-%s.pdf", name, dto.startDate().format(FILE_DATE_FORMATTER),
                dto.endDate().format(FILE_DATE_FORMATTER)));
    }
}
