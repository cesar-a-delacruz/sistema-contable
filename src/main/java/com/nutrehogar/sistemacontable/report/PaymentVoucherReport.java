package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.report.dto.JournalEntryReportData;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class PaymentVoucherReport extends EntryFormReport {
    private static PaymentVoucherReport instance;
    private PaymentVoucherReport() throws ReportException {
        super("Comprobante de Pago",
                "PaymentVoucher.jrxml",
                ConfigLoader.Props.DIR_PAYMENT_VOUCHER_NAME.getPath());
    }

    private static PaymentVoucherReport getInstance() throws ReportException {
        if (instance == null)
            instance = new PaymentVoucherReport();

        return instance;
    }

    @NotNull
    public static Path generate(@NotNull User user, @NotNull JournalEntryReportData dto) throws ReportException {
        try {
            return getInstance().generateReport(user, dto);
        } catch (ReportException e) {
            instance = null;
            throw e;
        }
    }

}