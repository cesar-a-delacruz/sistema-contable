package com.nutrehogar.sistemacontable.ui.business;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.config.Util;
import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.JournalEntry;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.AccountingPeriodQuery_;
import com.nutrehogar.sistemacontable.query.BussinessQuery_;
import com.nutrehogar.sistemacontable.report.GeneralLedgerReport;
import com.nutrehogar.sistemacontable.report.JournalReport;
import com.nutrehogar.sistemacontable.report.dto.GeneralLedgerReportData;
import com.nutrehogar.sistemacontable.report.dto.GeneralLedgerReportRow;
import com.nutrehogar.sistemacontable.report.dto.JournalReportData;
import com.nutrehogar.sistemacontable.report.dto.JournalReportRow;
import com.nutrehogar.sistemacontable.ui.UIEntityInfo;
import com.nutrehogar.sistemacontable.ui_2.component.ReportResponseDialog;
import com.nutrehogar.sistemacontable.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.ui.Period;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableModel;

import com.nutrehogar.sistemacontable.worker.ReportWorker;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static com.nutrehogar.sistemacontable.config.Util.*;

@Getter
public class JournalView extends SimpleView<JournalData> implements BusinessView {
    @NotNull
    private final CustomComboBoxModel<Period> cbxModelPeriod;
    @NotNull
    private final SpinnerNumberModel spnModelMonth;

    public JournalView(@NotNull User user, @NotNull Consumer<Long> editJournal) {
        super(user, UIEntityInfo.JOURNAL);
        this.cbxModelPeriod = new CustomComboBoxModel<>();
        this.spnModelMonth = new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1);
        this.tblModel = new CustomTableModel<>("Fecha", "Doc", "Cuenta", "Referencia","Concepto", "Débito", "Crédito") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var dto = data.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> dto.date();
                    case 1 -> dto.type().getName() + "-" + JournalEntry.formatNumber(dto.number());
                    case 2 -> dto.account();
                    case 3 -> dto.reference();
                    case 4 -> dto.concept();
                    case 5 -> dto.debit();
                    case 6 -> dto.credit();
                    default -> "";
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> LocalDate.class;
                    case 2 -> Account.class;
                    case 5, 6 -> BigDecimal.class;
                    default -> String.class;
                };
            }
        };
        initComponents();
        load();
        btnEdit.setEnabled(false);
        tblData.setOnDeselected(() -> btnEdit.setEnabled(false));
        tblData.setOnSelected(_ -> btnEdit.setEnabled(true));
        btnEdit.addActionListener(_ -> tblData.getSelected().ifPresent(e -> editJournal.accept(e.journalId())));
        cbxPeriod.addActionListener(_->loadData());
        spnMonth.addChangeListener(_ -> loadData());
        btnFilter.addActionListener(_ -> loadData());
        btnGenerateReport.addActionListener(_->{
            if(cbxModelPeriod.getSelectedItem() == null) {
                showMessage("Seleccione un periodo");
                return;
            }
            btnGenerateReport.setEnabled(false);
            showLoadingCursor();
            var journalData = tblModel.getData();
            var date = LocalDate.of(cbxModelPeriod.getSelectedItem().year(), spnModelMonth.getNumber().intValue(), 1);
            new ReportWorker(
                    () -> JournalReport.generate(user, new JournalReportData<>(
                            date,
                            journalData
                                    .stream()
                                    .map(j ->
                                            new JournalReportRow(
                                                    j.date().format(SMALL_DATE_FORMATTER),
                                                    JournalEntry.formatNaturalId(j.type(), j.number()),
                                                    Account.getFormattedNumber(j.account().number()),
                                                    j.reference(),
                                                    j.concept(),
                                                    formatDecimalSafe(j.debit()),
                                                    formatDecimalSafe(j.credit())
                                            )
                                    )
                                    .toList()
                    )),
                    path -> {
                        hideLoadingCursor();
                        btnGenerateReport.setEnabled(true);
                        ReportResponseDialog.showMessage(this, path);
                    },
                    this::showError
            ).execute();
        });
    }
    @Override
    public void load(){
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
    }
    @Override
    public void loadData() {
        tblData.setEmpty();
        var period = cbxModelPeriod.getSelectedItem();
        if (period == null) {
            showWarning("El periodo seleccionado no es valido");
            return;
        }
        var month = getSpnModelMonth().getNumber().intValue();
        new FromTransactionWorker<>(
                session -> new BussinessQuery_(session).findJournalByPeriodIdAndMonth(period.id(), month),
                tblModel::setData,
                this::showError
        ).execute();
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if(aFlag){
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
        spnMonth = new javax.swing.JSpinner(spnModelMonth);
        lblEnd = new javax.swing.JLabel();
        lblStart = new javax.swing.JLabel();
        btnGenerateReport = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblData = new com.nutrehogar.sistemacontable.ui_2.component.CustomTable(tblModel);
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
        btnFilter.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        lblFilter.setLabelFor(btnFilter);
        lblFilter.setText("<html><p>Muestra los datos de registros que coincidan con el período contable</p></html>");
        lblFilter.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblFilter.setPreferredSize(new java.awt.Dimension(250, 40));

        btnEdit.setText("Editar");
        btnEdit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        lblEdit.setLabelFor(btnEdit);
        lblEdit.setText("<html><p>Editar registro seleccionado</p></html>");
        lblEdit.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblEdit.setPreferredSize(new java.awt.Dimension(250, 40));

        cbxPeriod.setModel(cbxModelPeriod);

        lblEnd.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEnd.setText("Mes del período:");

        lblStart.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStart.setText("Período:");

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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOperationsLayout.createSequentialGroup()
                        .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblEnd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlOperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spnMonth)
                            .addComponent(cbxPeriod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                    .addComponent(lblEnd)
                    .addComponent(spnMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
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
        btnGenerateReport.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

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
                .addContainerGap(422, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(tblData);

        lblTitle.setFont(lblTitle.getFont().deriveFont((float)30));
        lblTitle.setForeground(Theme.Palette.OFFICE_GREEN);
        lblTitle.setIcon(entityInfo.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(entityInfo.getPlural());

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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblEdit;
    private javax.swing.JLabel lblEnd;
    private javax.swing.JLabel lblFilter;
    private javax.swing.JLabel lblStart;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlOperations;
    private javax.swing.JSpinner spnMonth;
    private com.nutrehogar.sistemacontable.ui_2.component.CustomTable<JournalData> tblData;
    // End of variables declaration//GEN-END:variables
}