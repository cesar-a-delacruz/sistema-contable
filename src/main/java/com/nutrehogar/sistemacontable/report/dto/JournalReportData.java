package com.nutrehogar.sistemacontable.report.dto;

import java.time.LocalDate;
import java.util.List;

public record JournalReportData<Row>(
        LocalDate date,
        List<Row> rows
) {

}
