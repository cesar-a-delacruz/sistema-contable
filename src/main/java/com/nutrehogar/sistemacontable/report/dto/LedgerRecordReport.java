package com.nutrehogar.sistemacontable.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LedgerRecordReport {
    String account;
    String reference;
    String debit;
    String credit;
}