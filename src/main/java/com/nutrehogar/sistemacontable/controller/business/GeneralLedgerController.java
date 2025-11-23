package com.nutrehogar.sistemacontable.controller.business;

import com.nutrehogar.sistemacontable.base.controller.BusinessController;
import com.nutrehogar.sistemacontable.base.domain.repository.*;
import com.nutrehogar.sistemacontable.base.ui.view.business.GeneralLedgerView;
import com.nutrehogar.sistemacontable.controller.business.dto.GeneralLedgerTableDTO;
import com.nutrehogar.sistemacontable.domain.model.*;
import com.nutrehogar.sistemacontable.domain.type.AccountType;
import com.nutrehogar.sistemacontable.domain.type.DocumentType;
import com.nutrehogar.sistemacontable.ui.builder.*;
import com.nutrehogar.sistemacontable.exception.RepositoryException;
import com.nutrehogar.sistemacontable.report.GeneralLedgerReport;
import com.nutrehogar.sistemacontable.report.ReportService;
import com.nutrehogar.sistemacontable.report.dto.GeneralLedgerDTOReport;
import com.nutrehogar.sistemacontable.report.dto.GeneralLedgerReportDTO;

import static com.nutrehogar.sistemacontable.application.config.Util.*;

import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.AbstractDocument;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class GeneralLedgerController extends BusinessController<GeneralLedgerTableDTO, Account> {
    private final AccountSubtypeRepository subtypeRepository;
    private final LedgerRecordRepository ledgerRecordRepository;
    private CustomComboBoxModel<AccountType> cbxModelAccountType;
    private CustomComboBoxModel<Account> cbxModelAccount;
    private CustomComboBoxModel<AccountSubtype> cbxModelSubtype;

    public GeneralLedgerController(AccountRepository repository, GeneralLedgerView view,
            Consumer<JournalEntryPK> editJournalEntry, AccountSubtypeRepository subtypeRepository,
            LedgerRecordRepository ledgerRecordRepository, ReportService reportService, User user) {
        super(repository, view, editJournalEntry, reportService, user);
        this.ledgerRecordRepository = ledgerRecordRepository;
        this.subtypeRepository = subtypeRepository;
        loadDataSubtype();
    }

    public void loadDataSubtype() {
        var accountType = cbxModelAccountType.getSelectedItem();
        if (accountType == null)
            return;
        List<AccountSubtype> list = subtypeRepository.findAllByAccountType(accountType);
        cbxModelSubtype.setData(list);
    }

    public void loadDataAccount() {
        var accountSubtype = cbxModelSubtype.getSelectedItem();
        assert accountSubtype != null;
        Hibernate.initialize(accountSubtype.getAccounts());
        List<Account> list = accountSubtype.getAccounts();
        cbxModelAccount.setData(list);
    }

    @Override
    protected void initialize() {
        setTblModel(new CustomTableModel("Fecha", "Comprobante", "Tipo Documento", "Cuenta", "Referencia", "Debíto",
                "Crédito", "Saldo") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var dto = getData().get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> dto.getEntryDate();
                    case 1 -> dto.getVoucher();
                    case 2 -> dto.getDocumentType();
                    case 3 -> Account.getCellRenderer(dto.getAccountId());
                    case 4 -> dto.getReference();
                    case 5 -> dto.getDebit();
                    case 6 -> dto.getCredit();
                    case 7 -> dto.getBalance();
                    default -> null;
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> LocalDate.class;
                    case 1 -> Integer.class;
                    case 2 -> DocumentType.class;
                    case 3, 4 -> String.class;
                    case 5, 6, 7 -> BigDecimal.class;
                    default -> Object.class;
                };
            }
        });
        cbxModelAccountType = new CustomComboBoxModel<>(AccountType.values());
        cbxModelSubtype = new CustomComboBoxModel<>(List.of());
        cbxModelAccount = new CustomComboBoxModel<>(List.of());
        getRbtSearchFilter().setSelected(true);
        getRbtSearchText().setSelected(false);
        toggleSearch(false);
        super.initialize();
    }

    @Override
    protected void setupViewListeners() {
        super.setupViewListeners();
        getCbxAccountType().setRenderer(new CustomListCellRenderer());
        getCbxAccountSubtype().setRenderer(new CustomListCellRenderer());
        getCbxAccount().setRenderer(new AccountListCellRenderer());
        getCbxAccountType().setModel(cbxModelAccountType);
        getCbxAccountSubtype().setModel(cbxModelSubtype);
        getCbxAccount().setModel(cbxModelAccount);
        cbxModelAccountType.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {}

            @Override
            public void intervalRemoved(ListDataEvent e) {}

            @Override
            public void contentsChanged(ListDataEvent e) {
                loadDataSubtype();
            }
        });

        cbxModelSubtype.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {}

            @Override
            public void intervalRemoved(ListDataEvent e) {}

            @Override
            public void contentsChanged(ListDataEvent e) {
                loadDataAccount();
            }
        });
        cbxModelAccount.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {}

            @Override
            public void intervalRemoved(ListDataEvent e) {}

            @Override
            public void contentsChanged(ListDataEvent e) {
                loadData();
            }
        });

        getRbtSearchText().addActionListener(e -> {
            toggleSearch(true);
        });
        getRbtSearchFilter().addActionListener(e -> {
            toggleSearch(false);
        });
        getBtnSearch().addActionListener(e -> {
            getBtnFilter().doClick();
        });

        ((AbstractDocument) getTxtId().getDocument())
                .setDocumentFilter(new CustomDocumentFilter(CustomDocumentFilter.Type.DECIMAL));
        getBtnGenerateReport().addActionListener(e -> {
            try {
                var dtos = new ArrayList<GeneralLedgerReportDTO>();
                data.forEach(t -> dtos.add(new GeneralLedgerReportDTO(
                        toStringSafe(t.getEntryId()), toStringSafe(t.getEntryDate()),
                        toStringSafe(t.getDocumentType(), DocumentType::getName), toStringSafe(t.getAccountId()),
                        toStringSafe(t.getAccountType(), AccountType::getName), toStringSafe(t.getVoucher()),
                        toStringSafe(t.getReference()), formatDecimalSafe(t.getDebit()),
                        formatDecimalSafe(t.getCredit()), formatDecimalSafe(t.getBalance()))));
                var dto = new GeneralLedgerDTOReport(spnModelStartPeriod.getValue(),
                        spnModelEndPeriod.getValue(),
                        Account.getCellRenderer(cbxModelAccount.getSelectedItem().getId()) + " "
                                + cbxModelAccount.getSelectedItem().getName(),
                        dtos);
                reportService.generateReport(GeneralLedgerReport.class, dto);
                showMessage("Reporte generado!");
            } catch (RepositoryException ex) {
                showError("Error al crear el Reporte.", ex);
            }
        });
    }

    @Override
    public void loadData() {
        new GeneralLedgerDataLoader().execute();
    }

    public class GeneralLedgerDataLoader extends DataLoader {

        @Override
        protected List<GeneralLedgerTableDTO> doInBackground() {
            List<LedgerRecord> ledgerRecords;

            if (!getRbtSearchText().isSelected()) {
                Account accountSelectedItem = cbxModelAccount.getSelectedItem();
                if (accountSelectedItem == null) {
                    return null;
                }
                ledgerRecords = ledgerRecordRepository.findByDateRangeAndAccount(accountSelectedItem,
                        spnModelEndPeriod.getValue());
            } else {
                if (getTxtId().getText().isBlank() || getTxtId().getText().length() > 5) {
                    showMessage("Inserte un número entre 1 y 5.");
                    return null;
                }
                ledgerRecords = ledgerRecordRepository.findByDateRangeAndAccountId(
                        Integer.parseInt(getTxtId().getText()),
                        spnModelEndPeriod.getValue());
            }

            List<LedgerRecord> lrInRange = ledgerRecords.stream()
                    .filter(record -> !record.getJournalEntry().getDate().isBefore(spnModelStartPeriod.getValue()))
                    .toList();
            // 🔹 Usar Stream para mapear, ordenar y calcular totales
            List<GeneralLedgerTableDTO> generalLedgers = lrInRange.stream()
                    .map(record -> new GeneralLedgerTableDTO(
                            record.getCreatedBy(),
                            record.getUpdatedBy(),
                            record.getCreatedAt(),
                            record.getUpdatedAt(),
                            record.getJournalEntry().getId(),
                            record.getJournalEntry().getDate(),
                            record.getJournalEntry().getId().getDocumentType(),
                            record.getAccount().getId(),
                            record.getAccount().getAccountSubtype().getAccountType(),
                            record.getJournalEntry().getId().getDocumentNumber(),
                            record.getReference(),
                            record.getDebit(),
                            record.getCredit(),
                            BigDecimal.ZERO))
                    .sorted(Comparator.comparing(GeneralLedgerTableDTO::getEntryDate)) // Ordenar por fecha
                    .toList();

            var debitSumAll = ledgerRecords.stream().map(record -> record.getDebit()).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            var creditSumAll = ledgerRecords.stream().map(record -> record.getCredit()).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 🔹 Calcular balances
            var balanceAll = BigDecimal.ZERO;
            for (LedgerRecord lr : ledgerRecords) {
                balanceAll = lr.getAccount().getAccountSubtype().getAccountType().getBalance(balanceAll, lr.getCredit(),
                        lr.getDebit());
            }

            // 🔹 Agregar total al final de la lista
            var totalDTO = new GeneralLedgerTableDTO("TOTAL", debitSumAll, creditSumAll, balanceAll);
            generalLedgers = new ArrayList<>(generalLedgers);
            generalLedgers.add(totalDTO);
            return generalLedgers;
        }
    }

    @Override
    protected void setElementSelected(@NotNull MouseEvent e) {
        int row = getTblData().rowAtPoint(e.getPoint());
        if (row != -1) {
            int selectedRow = getTblData().getSelectedRow();
            if (selectedRow >= 0 && selectedRow < getData().size() - 1) {
                setSelected(getData().get(selectedRow));
                setAuditoria();
                getBtnEdit().setEnabled(user.isAdmin());
                setJournalEntryId(getSelected().getEntryId());
            } else {
                getBtnEdit().setEnabled(false);
            }
        }
    }

    protected void toggleSearch(boolean isText) {
        getTxtId().setEnabled(isText);
        getBtnSearch().setEnabled(isText);
        getCbxAccount().setEnabled(!isText);
        getCbxAccountSubtype().setEnabled(!isText);
        getCbxAccountType().setEnabled(!isText);
    }

    @Override
    public GeneralLedgerView getView() {
        return (GeneralLedgerView) super.getView();
    }

    private JComboBox<AccountType> getCbxAccountType() {
        return getView().getCbxAccountType();
    }

    private JComboBox<AccountSubtype> getCbxAccountSubtype() {
        return getView().getCbxAccountSubtype();
    }

    private JComboBox<Account> getCbxAccount() {
        return getView().getCbxAccount();
    }

    private JRadioButton getRbtSearchText() {
        return getView().getRbtSearchText();
    }

    private JRadioButton getRbtSearchFilter() {
        return getView().getRbtSearchFilter();
    }

    private JTextField getTxtId() {
        return getView().getTxtId();
    }

    private JButton getBtnSearch() {
        return getView().getBtnSearch();
    }

    @Override
    public AccountRepository getRepository() {
        return (AccountRepository) super.getRepository();
    }
}
