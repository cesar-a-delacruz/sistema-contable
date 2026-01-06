package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.report.dto.JournalEntryReportData;

import java.nio.file.Path;

public enum EntryFormReportType {
    PAYMENT_VOUCHER {
        @Override
        public Path generate(User user, JournalEntryReportData dto)
                throws ReportException {
            return PaymentVoucherReport.generate(user, dto);
        }
    },
    REGISTRATION_FORM {
        @Override
        public Path generate(User user, JournalEntryReportData dto)
                throws ReportException {
            return RegistrationFormReport.generate(user, dto);
        }
    };

    public abstract Path generate(User user, JournalEntryReportData dto)
            throws ReportException;
}
