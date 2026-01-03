package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.model.DocumentType;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.AccountingPeriodQuery_;
import com.nutrehogar.sistemacontable.query.BussinessQuery_;
import com.nutrehogar.sistemacontable.service.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.ui.Period;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui.crud.RecordTableData;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTable;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableModel;
import com.nutrehogar.sistemacontable.ui_2.builder.LocalDateSpinnerModel;

import com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.svg.SVGAnimatedRect;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.math.MathContext.DECIMAL128;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

@Getter
public class GeneralLedgerView extends SimpleView<TrialBalanceRow> {
    @NotNull
    private final CustomComboBoxModel<Period> cbxModelPeriod;
    @NotNull
    public final CustomComboBoxModel<Account> cbxModelAccount;
    @NotNull
    private final SpinnerNumberModel spnModelAccountNumber;
    public GeneralLedgerView(@NotNull User user, @NotNull Consumer<Long> editJournal) {
        super(user, "Libro Diario");
        this.cbxModelPeriod = new CustomComboBoxModel<>();
        this.cbxModelAccount = new CustomComboBoxModel<>();
        this.spnModelAccountNumber = new SpinnerNumberModel(0, 0, 99999, 1);
        this.tblModel = new CustomTableModel<>("Fecha", "Comprobante", "Tipo", "Cuenta", "Referencia", "Débito", "Crédito", "Saldo") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return switch (data.get(rowIndex)) {
                    case TrialBalanceTotal(var total, var debit, var credit) -> switch (columnIndex) {
                        case 4 -> "Total";
                        case 5 -> debit;
                        case 6 -> credit;
                        case 7 -> total;
                        default -> "";
                    };
                    case TrialBalanceData dto -> switch (columnIndex) {
                        case 0 -> dto.date();
                        case 1 -> dto.number();
                        case 2 -> dto.type();
                        case 3 -> dto.account();
                        case 4 -> dto.reference();
                        case 5 -> dto.debit();
                        case 6 -> dto.credit();
                        case 7 -> dto.total();
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
                    case 3 -> Account.class;
                    case 5, 6, 7 -> BigDecimal.class;
                    default -> String.class;
                };
            }
        };
        initComponents();
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

                    loadData();
                },
                this::showError
        ).execute();
        btnEdit.setEnabled(false);
        tblData.setOnDeselected(() -> btnEdit.setEnabled(false));
        tblData.setOnSelected(e -> {
            if (e instanceof TrialBalanceData) {
                btnEdit.setEnabled(true);
                return;
            }
            tblData.setEmpty();
        });
        btnEdit.addActionListener(_ -> tblData.getSelected().ifPresent(e -> {
            if (e instanceof TrialBalanceData t) {
                editJournal.accept(t.journalId());
                return;
            }
            tblData.setEmpty();
        }));
        cbxPeriod.addActionListener(_->loadData());
        spnMonth.addChangeListener(_ -> loadData());
        btnFilter.addActionListener(_ -> loadData());
    }

    public void loadData() {
        tblData.setEmpty();
        var period = cbxModelPeriod.getSelectedItem();
        if (period == null) {
            showWarning("El periodo seleccionado no es valido");
            return;
        }
        var month = getSpnModelMonth().getNumber().intValue();
        new FromTransactionWorker<>(
                session -> {
                    var journal = new BussinessQuery_(session).findJournalByPeriodIdAndMonth(period.id(), month);
                    var trialBalance = new ArrayList<TrialBalanceRow>(journal.size());
                    var groupByAccountType = journal
                            .stream()
                            .sorted(
                                    comparing(JournalData::date)
                                            .thenComparing(JournalData::type)
                                            .thenComparing(JournalData::number)
                            ).collect(
                                    groupingBy(
                                            JournalData::account,
                                            TreeMap::new,
                                            toList()
                                    )
                            );
                    for (var entry : groupByAccountType.entrySet()) {
                        var accountMinData = entry.getKey();
                        var debitSum = BigDecimal.ZERO;
                        var creditSum = BigDecimal.ZERO;
                        var totalSum = BigDecimal.ZERO;
                        for (var record : entry.getValue()) {
                            debitSum = debitSum.add(record.debit(), DECIMAL128);
                            creditSum = creditSum.add(record.credit(), DECIMAL128);
                            totalSum = accountMinData.type().getBalance(totalSum, record.credit(), record.debit());
                            trialBalance.add(
                                    new TrialBalanceData(
                                            record.journalId(),
                                            record.date(),
                                            record.number(),
                                            record.type(),
                                            accountMinData,
                                            record.reference(),
                                            record.credit(),
                                            record.debit(),
                                            totalSum
                                    )
                            );
                        }
                        trialBalance.add(new TrialBalanceTotal(totalSum, creditSum, debitSum));
                    }
                    return trialBalance;
                },
                tblModel::setData,
                this::showError
        ).execute();
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
        cbxRAccount = new javax.swing.JComboBox<>();
        spnRAccountNumber = new javax.swing.JSpinner(recordController.spnModelRAccountNumber);
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

        cbxRAccount.setModel(recordController.cbxModelRAccount);

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
                        .addComponent(spnRAccountNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxRAccount, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                    .addComponent(cbxRAccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnRAccountNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        lblTitle.setIcon(Theme.SVGs.TRIAL_BALANCE.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(LabelBuilder.build("Balance de comprobacion"));

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
    private com.nutrehogar.sistemacontable.ui_2.component.CustomComboBox<Period> cbxPeriod;
    private javax.swing.JComboBox<Account> cbxRAccount;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblEdit;
    private javax.swing.JLabel lblFilter;
    private javax.swing.JLabel lblRecordAccount;
    private javax.swing.JLabel lblStart;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlOperations;
    private javax.swing.JSpinner spnRAccountNumber;
    private com.nutrehogar.sistemacontable.ui_2.builder.CustomTable<TrialBalanceRow> tblData;
    // End of variables declaration//GEN-END:variables
}
