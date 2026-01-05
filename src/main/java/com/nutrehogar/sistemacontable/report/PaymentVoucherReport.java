package com.nutrehogar.sistemacontable.report;

import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.exception.ReportException;

public final class PaymentVoucherReport extends EntryFormReport {
    private static PaymentVoucherReport instance;

    private PaymentVoucherReport() throws ReportException {
        super("Comprobante de Pago",
                "PaymentVoucher.jrxml",
                ConfigLoader.Props.DIR_PAYMENT_VOUCHER_NAME.getPath());
    }

    public static PaymentVoucherReport getInstance() throws ReportException {
        if (instance == null) {
            instance = new PaymentVoucherReport();
        }
        return instance;
    }

}