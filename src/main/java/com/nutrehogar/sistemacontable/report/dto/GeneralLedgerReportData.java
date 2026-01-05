package com.nutrehogar.sistemacontable.report.dto;


import java.util.List;

public record GeneralLedgerReportData(
        String period,
        String account,
        List<GeneralLedgerReportRow> rows
) {

}
