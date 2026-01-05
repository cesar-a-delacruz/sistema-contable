package com.nutrehogar.sistemacontable;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.nutrehogar.sistemacontable.config.ConfigLoader;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.ui.DashboardView;
import com.nutrehogar.sistemacontable.ui.service.AuthView;
import net.sf.jasperreports.engine.*;

import javax.swing.*;

public class Main {
    void main() {
        ConfigLoader.createDirectories();
        System.setProperty("LOG_DIR", ConfigLoader.Props.DIR_LOG_NAME.getPath().toString());
        Theme.setup();

        var adminUser = new User("0922", "Root", true, Permission.ADMIN, "Roo");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                HibernateUtil
                        .getSessionFactory();
                return null;
            }
        }.execute();

        Thread.startVirtualThread(() -> Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::shutdown)));

//        try {
//            var templateStream = Report.class.getResourceAsStream("/template/PaymentVoucher.jrxml");
//
//            if (templateStream == null)
//                throw new ReportException("Template not found");
//
//            var jasperReport = JasperCompileManager.compileReport(templateStream);
//            var dto = new JournalEntryReport(
//                    "Ingreso-003",
//                    "F-ahdhjdh \n F-78490",
//                    LocalDate.now(),
//                    "Julio C. Guerra",
//                    """
//                            Para cubrir gasto de víatica a la""",
//                    "70.00",
//                    List.of(
//                            new LedgerRecordReportDTO("Ingreso","no se","125364","lorem*30","25.36","125.25"),
//                            new LedgerRecordReportDTO("Ingreso","no se","125364","lorem*30","25.36","125.25"),
//                            new LedgerRecordReportDTO("Ingreso","no se","125364","lorem*30","25.36","125.25"),
//                            new LedgerRecordReportDTO("Ingreso","no se","125364","lorem*30","25.36","125.25")
//                    )
//            );
//            Map<String, Object> parameters =  new HashMap<>();
//            parameters.put("ENTRY_ID", String.valueOf(dto.id()));
//            parameters.put("ENTRY_DATE", dto.date().format(Util.LARGE_DATE_FORMATTER));
//            parameters.put("ENTRY_NAME", dto.name());
//            parameters.put("ENTRY_CONCEPT", dto.concept());
//            parameters.put("ENTRY_AMOUNT", dto.amount());
//            parameters.put("ENTRY_CHECK_NUMBER", dto.checkNumber());
//            parameters.put("TABLE_DATA_SOURCE", new JRBeanCollectionDataSource(dto.ledgerRecords()));
//            parameters.put("IMG_DIR", ConfigLoader.Props.DIR_REPORTS_TEMPLATE_NAME.getPath().toString() + File.separator);
//            parameters.put("MANAGER_NAME", adminUser.getUsername());
//            try {
//                var print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
//                JasperExportManager.exportReportToPdfFile(
//                        print,
//                        ConfigLoader.Props.DIR_PAYMENT_VOUCHER_NAME.getPath()
//                                .resolve(
//                                        String.format("%s#%s_%s.pdf",
//                                                "Comprobante de pago",
//                                                dto.id(),
//                                                dto.date().format(Util.FILE_DATE_FORMATTER)
//                                        )
//                                ).toString());
//            } catch (Exception e) {
//                throw new ReportException(e.getMessage(), e);
//            }
//        } catch (JRException e) {
//            throw new ReportException("Failed to compile report template: " + e.getMessage(), e);
//        }

//        HibernateUtil
//                .getSessionFactory()
//                .inTransaction(session ->
//                        session.persist(new AccountingPeriod(2025,  LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), false, adminUser.getUsername())));
//
//        HibernateUtil
//                .getSessionFactory()
//                .inTransaction(session -> {
//                    for (var type : AccountType.values()) {
//                        for (int i = 1000; i < 1010; i++) {
//                            session.persist(new AccountSubtype(Account.generateNumber(String.valueOf(i), type), type.getName() + " " + i, type, "Root"));
//                        }
//                    }
//                });
//        HibernateUtil
//                .getSessionFactory()
//                .inTransaction(session -> {
//                    for (var type : AccountType.values()) {
//                        for (int i = 1000; i < 1020; i++) {
//                            session.persist(new Account(Account.generateNumber(String.valueOf(i), type), type.getName() + " " + i, type, "Root"));
//                        }
//                    }
//                });
//
//
//        HibernateUtil
//                .getSessionFactory()
//                .inTransaction(session -> {
//                    for (int i = 1000; i < 1020; i++) {
//                        session.persist(new User(PasswordHasher.hashPassword("123456"), "user-" + i, i % 2 == 0, Permission.CONTRIBUTE, adminUser.getUsername()));
//                    }
//                });
//        long t0 = System.currentTimeMillis();
//
//        HibernateUtil.getSessionFactory();
//
//        System.out.println("Hibernate init: " + (System.currentTimeMillis() - t0));


        var frame = new JFrame();
        frame.setIconImage(new FlatSVGIcon("svgs/SistemaContableLogo.svg", 250, 250).getImage());
        frame.setTitle("Sistema Contable");
        frame.setSize(1300, 600);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.getRootPane().setBackground(Theme.Palette.SOLITUDE_100);
        var dashBoard = new DashboardView(adminUser);
        frame.add(dashBoard);
        frame.setVisible(true);

        var authView = new AuthView(frame, true, adminUser);
        authView.setVisible(true);
        var authUser = authView.getAutenicateUser();
        dashBoard.setUser(authUser);
        frame.setTitle("Sistema Contable - " + authUser.getUsername());

    }
}
