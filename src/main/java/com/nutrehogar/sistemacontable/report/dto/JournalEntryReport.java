package com.nutrehogar.sistemacontable.report.dto;

import java.time.LocalDate;
import java.util.List;

public record JournalEntryReport(
        String doc,
        String checkNumber,
        LocalDate date,
        String name,
        String concept,
        String amount,
        List<LedgerRecordReport> ledgerRecords
) {}
