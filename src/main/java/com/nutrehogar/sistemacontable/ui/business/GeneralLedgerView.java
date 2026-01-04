package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.DocumentType;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.AccountQuery_;
import com.nutrehogar.sistemacontable.query.AccountingPeriodQuery_;
import com.nutrehogar.sistemacontable.query.BussinessQuery_;
import com.nutrehogar.sistemacontable.service.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.ui.Period;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui_2.builder.*;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static java.math.MathContext.DECIMAL128;
import static java.util.Comparator.comparing;

@Getter
public class GeneralLedgerView extends SimpleView<GeneralLedgerRow> implements BusinessView {
    @NotNull
    private final CustomComboBoxModel<Period> cbxModelPeriod;
    @NotNull
    public final CustomComboBoxModel<Account> cbxModelAccount;
    @NotNull
    private final SpinnerNumberModel spnModelAccountNumber;
    @NotNull
    private final List<Account> accounts;

    public GeneralLedgerView(@NotNull User user, @NotNull Consumer<Long> editJournal) {
        super(user, "Libro Diario");
        this.cbxModelPeriod = new CustomComboBoxModel<>();
        this.cbxModelAccount = new CustomComboBoxModel<>();
        this.accounts = new ArrayList<>();
        this.spnModelAccountNumber = new SpinnerNumberModel(0, 0, 99999, 1);
        this.tblModel = new CustomTableModel<>("Fecha", "Comprobante", "Tipo", "Referencia", "Débito", "Crédito", "Saldo") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return switch (data.get(rowIndex)) {
                    case RowTotal(var debit, var credit,var total) -> switch (columnIndex) {
                        case 3 -> "Total";
                        case 4 -> debit;
                        case 5 -> credit;
                        case 6 -> total;
                        default -> "";
                    };
                    case GeneralLedgerPreviousPeriods(var total, var debit, var credit) -> switch (columnIndex) {
                        case 3 ->
                                total.setScale(0, RoundingMode.HALF_UP).equals(BigDecimal.ZERO) ? "No hay saldo de periodos anteriores" : "Saldo de periodos anteriores";
                        case 4 -> debit;
                        case 5 -> credit;
                        case 6 -> total;
                        default -> "";
                    };
                    case GeneralLedgerEntity dto -> switch (columnIndex) {
                        case 0 -> dto.date();
                        case 1 -> dto.number();
                        case 2 -> dto.type();
                        case 3 -> dto.reference();
                        case 4 -> dto.debit();
                        case 5 -> dto.credit();
                        case 6 -> dto.total();
                        default -> "Element not found";
                    };
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> LocalDate.class;
                    case 1 -> Integer.class;
                    case 2 -> DocumentType.class;
                    case 4, 5, 6 -> BigDecimal.class;
                    default -> String.class;
                };
            }
        };
        initComponents();
        load();
        cbxAccount.setRenderer(new CustomListCellRenderer());
        spnAccountNumber.setEditor(new JSpinner.NumberEditor(spnAccountNumber, "#"));
        btnEdit.setEnabled(false);
        tblData.setOnDeselected(() -> btnEdit.setEnabled(false));
        tblData.setOnSelected(e -> {
            if (e instanceof GeneralLedgerEntity) {
                btnEdit.setEnabled(true);
                return;
            }
            tblData.setEmpty();
        });
        btnEdit.addActionListener(_ -> tblData.getSelected().ifPresent(e -> {
            if (e instanceof GeneralLedgerEntity t) {
                editJournal.accept(t.journalId());
                return;
            }
            tblData.setEmpty();
        }));
        cbxPeriod.addActionListener(_ -> loadData());
        btnFilter.addActionListener(_ -> loadData());
        cbxAccount.addActionListener(_ -> loadData());
        spnAccountNumber.addChangeListener(_ -> filterAccounts());
    }

    @Override
    public void load() {
        new FromTransactionWorker<>(
                session -> new AccountingPeriodQuery_(session).findAllMinData(),
                periods -> {
                    if (periods.isEmpty()) {
                        showWarning("No hay periodos disponibles, antes de continuar debe crear al menos uno");
                        return;
                    }
                    cbxModelPeriod.setData(periods);

                    var thisYear = LocalDate.now().getYear();

                    for (var period : periods)
                        if (period.year() == thisYear)
                            cbxModelPeriod.setSelectedItem(period);

                },
                this::showError
        ).execute();
        new FromTransactionWorker<>(
                session -> new AccountQuery_(session).findAll(),
                list -> {
                    accounts.clear();
                    accounts.addAll(list);
                    filterAccounts();
                },
                this::showError
        ).execute();
    }

    public void filterAccounts() {
        var value = spnModelAccountNumber.getNumber().intValue();
        if (value == 0) {
            cbxModelAccount.setData(accounts);
            return;
        }
        cbxModelAccount.setData(
                accounts
                        .stream()
                        .filter(a -> a
                                .getNumber()
                                .toString()
                                .contains(String.valueOf(value)))
                        .toList()
        );
    }

    @Override
    public void loadData() {
        tblData.setEmpty();
        var period = cbxModelPeriod.getSelectedItem();
        if (period == null) {
            return;
        }
        var account = cbxModelAccount.getSelectedItem();
        if (account == null) {
            return;
        }
        var type = account.getType();
        new FromTransactionWorker<>(
                session -> {
                    var queries = new BussinessQuery_(session);
                    var journal = queries.findJournalByPeriodIdAndAccount(period.id(), account);
                    var arraySize = journal.size() + 1;
                    var debitSum = BigDecimal.ZERO;
                    var creditSum = BigDecimal.ZERO;

                    if (type.isCumulative()) {
                        arraySize++;
                        var preTotal = queries.findAccountPreTotal(period.year(), account);
                        if (preTotal.isPresent()) {
                            debitSum = preTotal.get().debit();
                            creditSum = preTotal.get().credit();
                        }
                    }
                    var trialBalance = new ArrayList<GeneralLedgerRow>(arraySize);

                    var totalSum = type.getBalance(BigDecimal.ZERO, creditSum, debitSum);

                    if (type.isCumulative())
                        trialBalance.add(new GeneralLedgerPreviousPeriods(totalSum, debitSum, creditSum));

                    for (var record : journal) {
                        debitSum = debitSum.add(record.debit(), DECIMAL128);
                        creditSum = creditSum.add(record.credit(), DECIMAL128);
                        totalSum = type.getBalance(totalSum, record.credit(), record.debit());
                        trialBalance.add(
                                new GeneralLedgerEntity(
                                        record.journalId(),
                                        record.date(),
                                        record.number(),
                                        record.type(),
                                        record.reference(),
                                        record.debit(),
                                        record.credit(),
                                        totalSum
                                )
                        );
                    }
                    trialBalance.add(new RowTotal(debitSum, creditSum, totalSum));
                    return trialBalance;
                },
                tblModel::setData,
                this::showError
        ).execute();
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            loadData();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        pnlAside = new javax.swing.JPanel();
        pnlOperations = new javax.swing.JPanel();
        btnFilter = new javax.swing.JButton();
        lblFilter = new javax.swing.JLabel();
        btnEdit = new javax.swing.JButton();
        lblEdit = new javax.swing.JLabel();
        cbxPeriod = new com.nutrehogar.sistemacontable.ui_2.component.CustomComboBox<>(cbxModelPeriod);
        lblStart = new javax.swing.JLabel();
        cbxAccount = new javax.swing.JComboBox<>();
        spnAccountNumber = new javax.swing.JSpinner(spnModelAccountNumber);
        lblRecordAccount = new javax.swing.JLabel();
        btnGenerateReport = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblData = new com.nutrehogar.sistemacontable.ui_2.builder.CustomTable(tblModel);
        lblTitle = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 591, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 479, Short.MAX_VALUE)
        );

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(524, 664));

        pnlAside.setOpaque(false);

        pnlOperations.setBorder(javax.swing.BorderFactory.createTitledBorder("Operaciones"));
        pnlOperations.setOpaque(false);

        btnFilter.setText("Aplicar");

        lblFilter.setLabelFor(btnFilter);
        lblFilter.setText("<html><p>Muestra los datos de registros que coincidan con el período contable</p></html>");
        lblFilter.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblFilter.setPreferredSize(new java.awt.Dimension(250, 40));

        btnEdit.setText("Editar");

        lblEdit.setLabelFor(btnEdit);
        lblEdit.setText("<html><p>Editar registro seleccionado</p></html>");
        lblEdit.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblEdit.setPreferredSize(new java.awt.Dimension(250, 40));

        cbxPeriod.setModel(cbxModelPeriod);

        lblStart.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStart.setText("Período:");

        cbxAccount.setModel(cbxModelAccount);

        lblRecordAccount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRecordAccount.setText("Cuenta:");

        javax.swing.GroupLayout pnlOperationsLayout = new javax.swing.GroupLayout(pnlOperations);
        pnlOperations.setLayout(pnlOperationsLayout);
        pnlOperationsLayout.setHorizontalGroup(
            pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOperationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOperationsLayout.createSequentialGroup()
                        .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlOperationsLayout.createSequentialGroup()
                        .addComponent(lblStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxPeriod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOperationsLayout.createSequentialGroup()
                        .addComponent(lblRecordAccount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnAccountNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxAccount, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlOperationsLayout.setVerticalGroup(
            pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOperationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStart)
                    .addComponent(cbxPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecordAccount)
                    .addComponent(cbxAccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnAccountNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFilter)
                    .addComponent(lblFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOperationsLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(lblEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnEdit)))
        );

        btnGenerateReport.setText("Generar Reporte");

        javax.swing.GroupLayout pnlAsideLayout = new javax.swing.GroupLayout(pnlAside);
        pnlAside.setLayout(pnlAsideLayout);
        pnlAsideLayout.setHorizontalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlOperations, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAsideLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGenerateReport)
                .addGap(15, 15, 15))
        );
        pnlAsideLayout.setVerticalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addComponent(pnlOperations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenerateReport)
                .addContainerGap(428, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(tblData);

        lblTitle.setFont(lblTitle.getFont().deriveFont((float)30));
        lblTitle.setForeground(Theme.Palette.OFFICE_GREEN);
        lblTitle.setIcon(Theme.SVGs.GENERAL_LEDGER.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(LabelBuilder.build("Mayor general"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2))
                    .addComponent(pnlAside, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnGenerateReport;
    private javax.swing.JComboBox<Account> cbxAccount;
    private com.nutrehogar.sistemacontable.ui_2.component.CustomComboBox<Period> cbxPeriod;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblEdit;
    private javax.swing.JLabel lblFilter;
    private javax.swing.JLabel lblRecordAccount;
    private javax.swing.JLabel lblStart;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlOperations;
    private javax.swing.JSpinner spnAccountNumber;
    private com.nutrehogar.sistemacontable.ui_2.builder.CustomTable<GeneralLedgerRow> tblData;
    // End of variables declaration//GEN-END:variables

}