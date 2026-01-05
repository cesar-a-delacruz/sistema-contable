package com.nutrehogar.sistemacontable.report.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class GeneralLedgerReportRow {
    String date;
    String doc;
    String reference;
    String concept;
    String debit;
    String credit;
    String balance;

    public GeneralLedgerReportRow(String concept, String debit, String credit, String balance) {
        this.concept = concept;
        this.debit = debit;
        this.credit = credit;
        this.balance = balance;
        this.date = "";
        this.doc = "";
        this.reference = "";
    }
}
