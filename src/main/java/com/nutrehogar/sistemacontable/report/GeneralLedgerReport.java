package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.exception.ReportException;


import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Map;

import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.report.dto.GeneralLedgerReportData;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jetbrains.annotations.NotNull;

public final class GeneralLedgerReport extends Report<GeneralLedgerReportData> {
    private static GeneralLedgerReport instance;

    private GeneralLedgerReport() throws ReportException {
        super("Mayor General", "GeneralLedger.jrxml", ConfigLoader.Props.DIR_GENERAL_LEDGER_NAME.getPath());
    }

    private static GeneralLedgerReport getInstance() throws ReportException {
        if (instance == null)
            instance = new GeneralLedgerReport();

        return instance;
    }

    @NotNull
    public static Path generate(@NotNull User user, @NotNull GeneralLedgerReportData dto) throws ReportException {
        try {
            return getInstance().generateReport(user, dto);
        } catch (ReportException e) {
            instance = null;
            throw e;
        }
    }

    @Override
    protected void setProps(@NotNull Map<String, Object> parameters, @NotNull GeneralLedgerReportData dto) {
        parameters.put("DATE", "Periodo " + dto.period());
        parameters.put("ACCOUNT", dto.account());
        parameters.put("TABLE_DATA_SOURCE", new JRBeanCollectionDataSource(dto.rows()));
    }

    @Override
    @NotNull
    protected Path getDirReportPath(@NotNull GeneralLedgerReportData dto) {
        var path = dirPath.resolve(String.valueOf(dto.period()));
        try {
            Files.createDirectories(path);
            return path.resolve(String.format("%s.pdf", dto.account()));
        } catch (Exception e) {
            throw new ReportException(LabelBuilder.build("Error al intentar guardar el pdf en la carpeta: " + path), e);
        }
    }
}