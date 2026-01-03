package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.exception.InvalidFieldException;
import com.nutrehogar.sistemacontable.model.AccountEntity;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;

import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.AccountSubtypeQuery_;
import com.nutrehogar.sistemacontable.service.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.service.worker.InTransactionWorker;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomListCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class AccountSubtypeView extends SimpleView<AccountSubtype> implements CRUDView<AccountSubtype, AccountSubtypeFormData> {

    private final CustomComboBoxModel<AccountType> cbxModelAccountType;
    private final SpinnerNumberModel spnModelNumber;
    public AccountSubtypeView(User user) {
        super(user,"Subtipo de Cuenta");
        this.cbxModelAccountType = new CustomComboBoxModel<>(AccountType.values());
        this.spnModelNumber = new SpinnerNumberModel(0, 0,9999,1);
        this.tblModel = new CustomTableModel<>("Numero de Cuenta", "Nombre", "Tipo de Cuenta") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var dto = data.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> dto.getFormattedNumber();
                    case 1 -> dto.getName();
                    case 2 -> dto.getType().getName();
                    default -> "que haces?";
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? AccountType.class : String.class;
            }
        };
        initComponents();
        loadData();
        cbxType.setRenderer(new CustomListCellRenderer());
        txtName.putClientProperty("JTextField.placeholderText", "Activos Corrientes");
        spnNumber.setEditor(new JSpinner.NumberEditor(spnNumber, "#"));
        tblData.setOnDeselected(this::onDeselected);
        tblData.setOnSelected(this::onSelected);
        operationPanel.getBtnPrepareToAdd().addActionListener(_ -> prepareToAdd());
        operationPanel.getBtnPrepareToEdit().addActionListener(_ -> prepareToEdit());
        btnSave.addActionListener(_ -> save());
        btnUpdate.addActionListener(_ -> update());
        operationPanel.getBtnDelete().addActionListener(_ -> delete());
        cbxType.addActionListener(_ -> lblAccountTypeId.setText((cbxModelAccountType.getSelectedItem()).getId() + "."));
    }

    public void loadData() {
        tblData.setEmpty();
        prepareToAdd();
        new FromTransactionWorker<>(
                session -> new AccountSubtypeQuery_(session).findAll(),
                tblModel::setData,
                this::showError
        ).execute();
    }
    @Override
    public @NotNull AccountSubtypeFormData getDataFromForm() throws InvalidFieldException {
        var type = cbxModelAccountType.getSelectedItem();
        if (txtName.getText().isBlank())
            throw new InvalidFieldException("El nombre no puede estar vacío");

        if (type == null)
            throw new InvalidFieldException("El tipo de cuenta no puede estar vacío");

        return new AccountSubtypeFormData(txtName.getText(), AccountEntity.generateNumber(spnModelNumber.getNumber().intValue(), type), type, user.getUsername());
    }

    @Override
    public void setEntityDataInForm(@NotNull AccountSubtype entity) {
        txtName.setText(entity.getName());
        spnModelNumber.setValue(entity.getSubNumber());
        cbxType.setSelectedItem(entity.getType());
    }

    @Override
    public void prepareToAdd() {
        txtName.setText("");
        spnModelNumber.setValue(0);
        cbxType.setSelectedItem(AccountType.ASSETS);
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
    }

    @Override
    public void prepareToEdit() {
        tblData
                .getSelected()
                .ifPresentOrElse(
                        this::setEntityDataInForm,
                        () -> showWarning("Seleccione un elemento de la tabla")
                );
        btnSave.setEnabled(false);
        btnUpdate.setEnabled(true);
    }

    @Override
    public void onSelected(@NotNull AccountSubtype entity) {
        auditablePanel.setAuditableFields(entity);
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
        try {
            tblData
                    .getSelected()
                    .ifPresentOrElse(
                            entity ->
                                    new InTransactionWorker(
                                            session -> session.remove(session.merge(entity)),
                                            this::loadData,
                                            this::showError
                                    ).execute(),
                            () -> {
                                throw new InvalidFieldException("Seleccione un elemento de la tabla");
                            });
        } catch (InvalidFieldException e) {
            showWarning(e);
        }
    }

    @Override
    public void save() {
        try {
            var dto = getDataFromForm();
            new InTransactionWorker(
                    session -> session.persist(new AccountSubtype(dto.number(), dto.name(), dto.type(), dto.username())),
                    this::loadData,
                    this::showError
            ).execute();
        } catch (InvalidFieldException e) {
            showWarning(e);
        }
    }

    @Override
    public void update() {
        try {
            var dto = getDataFromForm();
            tblData.getSelected()
                    .ifPresentOrElse(
                            accountSubtype ->
                                    new InTransactionWorker(
                                            session -> {
                                                var entity = session.merge(accountSubtype);
                                                entity.setUpdatedBy(dto.username());
                                                entity.setNumber(dto.number());
                                                entity.setName(dto.name());
                                                entity.setType(dto.type());
                                            },
                                            this::loadData,
                                            this::showError
                                    ).execute(),
                            () -> {
                                throw new InvalidFieldException("Seleccione un elemento de la tabla");
                            }
                    );
        } catch (InvalidFieldException e) {
            showWarning(e);
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
        pnlForm = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblNumber = new javax.swing.JLabel();
        lblAccountTypeId = new javax.swing.JLabel();
        lblType = new javax.swing.JLabel();
        cbxType = new javax.swing.JComboBox<>();
        btnSave = new javax.swing.JButton();
        labelSection1 = new javax.swing.JLabel();
        sepaSection1 = new javax.swing.JSeparator();
        btnUpdate = new javax.swing.JButton();
        lblSave = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        spnNumber = new javax.swing.JSpinner();
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

        lblName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblName.setLabelFor(txtName);
        lblName.setText("Nombre:");

        txtName.setMaximumSize(new java.awt.Dimension(200, 200));

        lblNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNumber.setText("Numero:");

        lblAccountTypeId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountTypeId.setText("1.");

        lblType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblType.setLabelFor(cbxType);
        lblType.setText("Tipo:");

        cbxType.setModel(cbxModelAccountType);

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

        spnNumber.setModel(spnModelNumber);

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbxType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(lblAccountTypeId, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFormLayout.createSequentialGroup()
                        .addComponent(labelSection1)
                        .addGap(18, 18, 18)
                        .addComponent(sepaSection1))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNumber)
                    .addComponent(lblAccountTypeId)
                    .addComponent(spnNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(cbxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
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
                    .addComponent(auditablePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        lblTitle.setIcon(Theme.SVGs.ACCOUNT_SUBTYPE.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(LabelBuilder.build("Subtipos de Cuentas"));

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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2))
                    .addComponent(pnlAside, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel auditablePanel;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<AccountType> cbxType;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelSection1;
    private javax.swing.JLabel lblAccountTypeId;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNumber;
    private javax.swing.JLabel lblSave;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lblUpdate;
    private com.nutrehogar.sistemacontable.ui_2.component.OperationPanel operationPanel;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JSeparator sepaSection1;
    private javax.swing.JSpinner spnNumber;
    private com.nutrehogar.sistemacontable.ui_2.builder.CustomTable<AccountSubtype> tblData;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables

}
