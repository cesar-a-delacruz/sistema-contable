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
public class JournalReportRow {
    String date;
    String doc;
    String account;
    String reference;
    String concept;
    String debit;
    String credit;
}
