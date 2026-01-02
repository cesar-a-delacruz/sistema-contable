package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.model.DocumentType;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.BussinessQuery_;
import com.nutrehogar.sistemacontable.service.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui.crud.RecordTableData;
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
public class TrialBalanceView extends SimpleView<TrialBalanceRow> {
    @NotNull
    private final LocalDateSpinnerModel spnModelStartDate;
    @NotNull
    private final LocalDateSpinnerModel spnModelEndDate;
    public TrialBalanceView(@NotNull User user, @NotNull Consumer<Long> editJournal) {
        super(user, "Libro Diario");
        this.spnModelStartDate = new LocalDateSpinnerModel();
        this.spnModelEndDate = new LocalDateSpinnerModel();
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
        loadData();
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
        btnFilter.addActionListener(_->loadData());

    }

    public void loadData() {
        tblData.setEmpty();
        var end = spnModelEndDate.getValue();
        var start = spnModelStartDate.getValue();
        new FromTransactionWorker<>(
                session -> {
                    var journal = new BussinessQuery_(session).findJournalByDateRange(start, end);
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

        jPanel1 = new JPanel();
        pnlAside = new JPanel();
        pnlOperations = new JPanel();
        btnFilter = new JButton();
        lblFilter = new JLabel();
        spnStartDate = new LocalDateSpinner(spnModelStartDate);
        lblStart = new JLabel();
        btnResetStartDate = new JButton();
        btnResetEndDate = new JButton();
        spnEndDate = new LocalDateSpinner(spnModelEndDate);
        lblEnd = new JLabel();
        btnEdit = new JButton();
        lblEdit = new JLabel();
        btnGenerateReport = new JButton();
        jScrollPane2 = new JScrollPane();
        tblData = new CustomTable(tblModel);
        lblTitle = new JLabel();

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 591, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 479, Short.MAX_VALUE)
        );

        setOpaque(false);
        setPreferredSize(new Dimension(524, 664));

        pnlAside.setOpaque(false);

        pnlOperations.setBorder(BorderFactory.createTitledBorder("Operaciones"));
        pnlOperations.setOpaque(false);

        btnFilter.setText("Aplicar");

        lblFilter.setLabelFor(btnFilter);
        lblFilter.setText("<html><p>Muestra los datos de registros que coincidan con el período contable</p></html>");
        lblFilter.setVerticalAlignment(SwingConstants.TOP);
        lblFilter.setPreferredSize(new Dimension(250, 40));

        lblStart.setHorizontalAlignment(SwingConstants.RIGHT);
        lblStart.setLabelFor(spnStartDate);
        lblStart.setText("Inicio de período:");

        btnResetStartDate.setText("Restablecer");

        btnResetEndDate.setText("Restablecer");

        lblEnd.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEnd.setLabelFor(spnEndDate);
        lblEnd.setText("Final de período:");

        btnEdit.setText("Editar");

        lblEdit.setLabelFor(btnEdit);
        lblEdit.setText("<html><p>Editar registro seleccionado</p></html>");
        lblEdit.setVerticalAlignment(SwingConstants.TOP);
        lblEdit.setPreferredSize(new Dimension(250, 40));

        GroupLayout pnlOperationsLayout = new GroupLayout(pnlOperations);
        pnlOperations.setLayout(pnlOperationsLayout);
        pnlOperationsLayout.setHorizontalGroup(
            pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlOperationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblFilter, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(GroupLayout.Alignment.TRAILING, pnlOperationsLayout.createSequentialGroup()
                        .addGroup(pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(lblStart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblEnd, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(spnEndDate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spnStartDate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(lblEdit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(btnResetEndDate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnFilter, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnResetStartDate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEdit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlOperationsLayout.setVerticalGroup(
            pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, pnlOperationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(spnStartDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStart)
                    .addComponent(btnResetStartDate))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(spnEndDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEnd)
                    .addComponent(btnResetEndDate))
                .addGap(18, 18, 18)
                .addGroup(pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFilter)
                    .addComponent(lblFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlOperationsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOperationsLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(lblEdit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnEdit)))
        );

        btnGenerateReport.setText("Generar Reporte");

        GroupLayout pnlAsideLayout = new GroupLayout(pnlAside);
        pnlAside.setLayout(pnlAsideLayout);
        pnlAsideLayout.setHorizontalGroup(
            pnlAsideLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlOperations, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(GroupLayout.Alignment.TRAILING, pnlAsideLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGenerateReport)
                .addGap(15, 15, 15))
        );
        pnlAsideLayout.setVerticalGroup(
            pnlAsideLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addComponent(pnlOperations, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGenerateReport)
                .addContainerGap(420, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(tblData);

        lblTitle.setFont(lblTitle.getFont().deriveFont((float)30));
        lblTitle.setForeground(Theme.Palette.OFFICE_GREEN);
        lblTitle.setIcon(Theme.SVGs.TRIAL_BALANCE.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(LabelBuilder.build("Balance de comprobacion"));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAside, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2))
                    .addComponent(pnlAside, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnEdit;
    private JButton btnFilter;
    private JButton btnGenerateReport;
    private JButton btnResetEndDate;
    private JButton btnResetStartDate;
    private JPanel jPanel1;
    private JScrollPane jScrollPane2;
    private JLabel lblEdit;
    private JLabel lblEnd;
    private JLabel lblFilter;
    private JLabel lblStart;
    private JLabel lblTitle;
    private JPanel pnlAside;
    private JPanel pnlOperations;
    private LocalDateSpinner spnEndDate;
    private LocalDateSpinner spnStartDate;
    private CustomTable<TrialBalanceRow> tblData;
    // End of variables declaration//GEN-END:variables
}
