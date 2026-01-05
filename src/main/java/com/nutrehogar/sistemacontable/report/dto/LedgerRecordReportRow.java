package com.nutrehogar.sistemacontable.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LedgerRecordReportRow {
    String account;
    String reference;
    String debit;
    String credit;
}