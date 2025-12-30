package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.model.*;

import com.nutrehogar.sistemacontable.query.AccountSubtypeQuery_;
import com.nutrehogar.sistemacontable.query.AccountingPeriodQuery_;
import com.nutrehogar.sistemacontable.service.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.service.worker.InTransactionWorker;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableModel;
import com.nutrehogar.sistemacontable.ui_2.builder.LocalDateSpinnerModel;
import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;
import com.nutrehogar.sistemacontable.ui_2.component.OperationPanel;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class AccountingPeriodView extends SimpleView<AccountingPeriod> implements CRUDView<AccountingPeriod, AccountingPeriodFormData> {
    private final LocalDateSpinnerModel spnModelStartPeriod;
    private final LocalDateSpinnerModel spnModelEndPeriod;
    private final SpinnerNumberModel spnModelYear;

    public AccountingPeriodView(User user) {
        super(user, "Periodo Contable");
        var currentYear = LocalDate.now().getYear();
        this.spnModelStartPeriod = new LocalDateSpinnerModel(LocalDate.of(currentYear, 1, 1));
        this.spnModelEndPeriod = new LocalDateSpinnerModel(LocalDate.of(currentYear, 12, 1));
        this.spnModelYear = new SpinnerNumberModel(currentYear, 1500, null, 1);
        this.tblModel = new CustomTableModel<>("Numero", "Año", "Cerrado", "Fecha de inicio", "Fecha de cierre") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var dto = data.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> dto.getPeriodNumber();
                    case 1 -> dto.getYear();
                    case 2 -> dto.getClosed();
                    case 3 -> dto.getStartDate();
                    case 4 -> dto.getEndDate();
                    default -> "que haces?";
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 1 -> Integer.class;
                    case 2 -> Boolean.class;
                    case 3, 4 -> LocalDate.class;
                    default -> String.class;
                };
            }
        };
        initComponents();
        loadData();
        spnYear.setEditor(new JSpinner.NumberEditor(spnYear, "#"));
        txtNumber.putClientProperty("JTextField.placeholderText", "11");
        tblData.setOnDeselected(this::onDeselected);
        tblData.setOnSelected(this::onSelected);
        operationPanel.getBtnPrepareToAdd().addActionListener(_ -> prepareToAdd());
        operationPanel.getBtnPrepareToEdit().addActionListener(_ -> prepareToEdit());
        btnSave.addActionListener(_ -> save());
        btnUpdate.addActionListener(_ -> update());
        operationPanel.getBtnDelete().addActionListener(_ -> delete());
    }
    public void loadData() {
        tblData.setEmpty();
        prepareToAdd();
        new FromTransactionWorker<>(
                session -> new AccountingPeriodQuery_(session).findAll(),
                tblModel::setData,
                this::showError
        ).execute();
    }
    @Override
    public @NotNull AccountingPeriodFormData getDataFromForm() {
        return new AccountingPeriodFormData(
                spnModelYear.getNumber().intValue(),
                Integer.valueOf(txtNumber.getText()),
                spnModelStartPeriod.getValue(),
                spnModelEndPeriod.getValue(),
                chkClosed.isSelected(),
                user.getUsername()
        );
    }

    @Override
    public void setEntityDataInForm(@NotNull AccountingPeriod entity) {
        txtNumber.setText(entity.getPeriodNumber().toString());
        spnModelYear.setValue(entity.getYear());
        spnModelStartPeriod.setValue(entity.getStartDate());
        spnModelEndPeriod.setValue(entity.getEndDate());
        chkClosed.setSelected(entity.getClosed());
    }

    @Override
    public void prepareToAdd() {
        txtNumber.setText("");
        chkClosed.setSelected(false);
        var currentYear = LocalDate.now().getYear();
        spnModelYear.setValue(currentYear);
        spnModelStartPeriod.setValue(LocalDate.of(currentYear, 1, 1));
        spnModelEndPeriod.setValue(LocalDate.of(currentYear, 12, 1));
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
    }
    @Override
    public void prepareToEdit() {
        tblData.getSelected()
                .ifPresentOrElse(this::setEntityDataInForm,
                        () -> showMessage("Seleccione un elemento de la tabla"));
        btnSave.setEnabled(false);
        btnUpdate.setEnabled(true);
    }

    @Override
    public void onSelected(AccountingPeriod accountingPeriod) {
        auditablePanel.setAuditableFields(accountingPeriod);
        operationPanel.getBtnDelete().setEnabled(true);
        operationPanel.getBtnPrepareToEdit().setEnabled(true);
    }

    @Override
    public void onDeselected() {
        operationPanel.getBtnDelete().setEnabled(false);
        operationPanel.getBtnPrepareToEdit().setEnabled(false);
    }

    @Override
    public void delete() {
        tblData
                .getSelected()
                .ifPresentOrElse(
                        entity ->
                                new InTransactionWorker(
                                        session -> session.remove(session.merge(entity)),
                                        this::loadData,
                                        this::showError
                                ).execute(),
                        () -> showMessage("Seleccione un elemento de la tabla")
                );
    }

    @Override
    public void save() {
        var dto = getDataFromForm();
        new InTransactionWorker(
                session -> session.persist(new AccountingPeriod(dto.year(),dto.periodNumber(),dto.startDate(),dto.endDate(),dto.closed(),dto.username())),
                this::loadData,
                this::showError
        ).execute();    }

    @Override
    public void update() {
        var dto = getDataFromForm();
        tblData.getSelected()
                .ifPresentOrElse(
                        accountingPeriod ->
                                new InTransactionWorker(
                                        session -> {
                                            var entity = session.merge(accountingPeriod);
                                            entity.setUpdatedBy(dto.username());
                                            entity.setClosed(dto.closed());
                                            entity.setStartDate(dto.startDate());
                                            entity.setEndDate(dto.endDate());
                                            entity.setPeriodNumber(dto.periodNumber());
                                            entity.setYear(dto.year());
                                        },
                                        this::loadData,
                                        this::showError
                                ).execute(),
                        () -> showMessage("Seleccione un elemento de la tabla")
                );
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
        pnlForm = new javax.swing.JPanel();
        lblNumber = new javax.swing.JLabel();
        txtNumber = new javax.swing.JTextField();
        lblType = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        labelSection1 = new javax.swing.JLabel();
        sepaSection1 = new javax.swing.JSeparator();
        btnUpdate = new javax.swing.JButton();
        lblSave = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        spnStart = new com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner(spnModelStartPeriod);
        lblStart = new javax.swing.JLabel();
        lblEnd = new javax.swing.JLabel();
        spnEnd = new com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner(spnModelEndPeriod);
        spnYear = new javax.swing.JSpinner();
        chkClosed = new javax.swing.JCheckBox();
        auditablePanel = new com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel();
        operationPanel = new com.nutrehogar.sistemacontable.ui_2.component.OperationPanel(entityName);
        lblTitle = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblData = new com.nutrehogar.sistemacontable.ui_2.builder.CustomTable(tblModel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 591, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 473, Short.MAX_VALUE)
        );

        setOpaque(false);

        pnlAside.setOpaque(false);

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Formulario"));
        pnlForm.setOpaque(false);

        lblNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNumber.setLabelFor(txtNumber);
        lblNumber.setText("Numero:");

        lblType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblType.setText("Año:");

        btnSave.setText("Guardar");

        labelSection1.setText("Operaciones");

        btnUpdate.setText("Actualizar");

        lblSave.setLabelFor(btnSave);
        lblSave.setText("<html><p>Guarda el nuevo "+entityName+"</p></html>");
        lblSave.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblSave.setPreferredSize(new java.awt.Dimension(250, 40));

        lblUpdate.setLabelFor(btnUpdate);
        lblUpdate.setText("<html><p>Actualiza los datos del "+entityName+" seleccionado con los datos del formulario</p></html>");
        lblUpdate.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblUpdate.setPreferredSize(new java.awt.Dimension(250, 40));

        lblStart.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStart.setText("Inicio de período:");

        lblEnd.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEnd.setText("Final de período:");

        spnYear.setModel(spnModelYear);

        chkClosed.setText("Cerrado");

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFormLayout.createSequentialGroup()
                        .addComponent(labelSection1)
                        .addGap(18, 18, 18)
                        .addComponent(sepaSection1))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSave, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                            .addComponent(lblUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                            .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
                                .addGap(6, 6, 6)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spnYear)
                                    .addComponent(txtNumber)))
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblStart, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkClosed)
                                    .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(spnEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(spnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNumber)
                    .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(spnYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStart)
                    .addComponent(spnStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEnd)
                    .addComponent(spnEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkClosed)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelSection1)
                    .addComponent(sepaSection1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(lblSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate))
                .addContainerGap())
        );

        auditablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Auditoría"));

        javax.swing.GroupLayout pnlAsideLayout = new javax.swing.GroupLayout(pnlAside);
        pnlAside.setLayout(pnlAsideLayout);
        pnlAsideLayout.setHorizontalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlForm, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(auditablePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                    .addComponent(operationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAsideLayout.setVerticalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addComponent(operationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(auditablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblTitle.setFont(lblTitle.getFont().deriveFont((float)30));
        lblTitle.setForeground(Theme.Palette.OFFICE_GREEN);
        lblTitle.setIcon(Theme.SVGs.ACCOUNTING_PERIOD.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(LabelBuilder.build("Periodos Contables"));

        jScrollPane2.setViewportView(tblData);

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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAside, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel auditablePanel;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox chkClosed;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelSection1;
    private javax.swing.JLabel lblEnd;
    private javax.swing.JLabel lblNumber;
    private javax.swing.JLabel lblSave;
    private javax.swing.JLabel lblStart;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lblUpdate;
    private com.nutrehogar.sistemacontable.ui_2.component.OperationPanel operationPanel;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JSeparator sepaSection1;
    private com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner spnEnd;
    private com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner spnStart;
    private javax.swing.JSpinner spnYear;
    private com.nutrehogar.sistemacontable.ui_2.builder.CustomTable<AccountingPeriod> tblData;
    private javax.swing.JTextField txtNumber;
    // End of variables declaration//GEN-END:variables

}
