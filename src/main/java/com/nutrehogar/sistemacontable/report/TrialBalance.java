package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.report.dto.TrialBalanceReportDTO;

public class TrialBalance extends SimpleReport<TrialBalanceReportDTO> {
    public TrialBalance() throws ReportException {
        super("Balance General",
                "TrialBalance.jrxml",
                ConfigLoader.Props.DIR_TRIAL_BALANCE_NAME.getPath());
    }
}