package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.exception.ReportException;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.nutrehogar.sistemacontable.model.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.jetbrains.annotations.NotNull;

import static com.nutrehogar.sistemacontable.config.Util.LOCALE;

@Slf4j
public abstract class Report<T> {
    protected static final String TEMPLATE_PATH = "/template/";
    protected static final String imgDir = ConfigLoader.Props.DIR_REPORTS_TEMPLATE_NAME.getPath().toString() + File.separator;

    protected final String name;
    protected final String templateName;
    protected final Path dirPath;
    protected final JasperReport jasperReport;

    protected Report(String name, String templateName, Path dirPath) throws ReportException {
        this.name = name;
        this.templateName = templateName;
        this.dirPath = dirPath;
        try {
            var templateStream = Report.class.getResourceAsStream(TEMPLATE_PATH + templateName);
            if (templateStream == null)
                throw new ReportException(
                        LabelBuilder.of("No se encontró la plantilla para los reportes de: " + name)
                                .p("Inténtelo nuevamente, si el problema persiste reinicie el programa")
                                .build()
                );
            this.jasperReport = JasperCompileManager.compileReport(templateStream);
        } catch (JRException e) {
            throw new ReportException(
                    LabelBuilder.of("Ocurrió un error al procesar la plantilla para los reportes de: " + name)
                            .p("Inténtelo nuevamente, si el problema persiste reinicie el programa")
                            .build(), e);
        }
    }

    protected @NotNull Path generateReport(@NotNull User user, @NotNull T dto) throws ReportException {
        Map<String, Object> params = new HashMap<>();
        params.put("MANAGER_NAME", user.getUsername());
        params.put("IMG_DIR", imgDir);
        setProps(params, dto);
        try {
            var print = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
            var path = getDirReportPath(dto);
            JasperExportManager.exportReportToPdfFile(print, path.toString());
            return path;
        } catch (JRException e) {
            throw new ReportException(LabelBuilder.of("Ocurrió un error al generar el reporte y convertirlo en dpf")
                    .p("Inténtelo nuevamente, si el problema persiste reinicie el programa")
                    .build(), e);
        }
    }

    protected abstract void setProps(@NotNull Map<String, Object> params, @NotNull T dto);

    protected abstract @NotNull Path getDirReportPath(@NotNull T dto) throws ReportException;

}