package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.exception.InvalidFieldException;
import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.query.AccountQuery_;
import com.nutrehogar.sistemacontable.query.AccountSubtypeQuery_;

import javax.swing.*;

import com.nutrehogar.sistemacontable.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.worker.InTransactionWorker;
import com.nutrehogar.sistemacontable.ui.SimpleView;
import com.nutrehogar.sistemacontable.ui.UIEntityInfo;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomListCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableModel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Getter
public class AccountView extends SimpleView<AccountData> implements CRUDView<AccountData, AccountFormData> {

    private final CustomComboBoxModel<AccountType> cbxModelAccountType;
    private final CustomComboBoxModel<AccountSubtypeMinData> cbxModelAccountSubtype;
    private Optional<Consumer<List<AccountSubtypeMinData>>> onFindSubtypes = Optional.empty();
    private final SpinnerNumberModel spnModelNumber;

    public AccountView(User user) {
        super(user,  UIEntityInfo.ACCOUNT);
        this.cbxModelAccountType = new CustomComboBoxModel<>(AccountType.values());
        this.cbxModelAccountSubtype = new CustomComboBoxModel<>(List.of());
        this.spnModelNumber = new SpinnerNumberModel(0, 0,9999,1);
        this.tblModel = new CustomTableModel<>("Numero", "Nombre", "Tipo de Cuenta", "Subtipo de Cuenta") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var dto = data.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> AccountEntity.getFormattedNumber(dto.number());
                    case 1 -> dto.name();
                    case 2 -> dto.type();
                    case 3 -> dto.subtypeName() == null ? null : dto.subtypeName();
                    default -> "que haces?";
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? AccountType.class : String.class;
            }
        };
        initComponents();
        cbxType.setRenderer(new CustomListCellRenderer());
        cbxSubtype.setRenderer(new CustomListCellRenderer());
        spnNumber.setEditor(new JSpinner.NumberEditor(spnNumber, "#"));
        txtName.putClientProperty("JTextField.placeholderText", "Caja Menuda");

        tblData.setOnDeselected(this::onDeselected);
        tblData.setOnSelected(this::onSelected);
        operationPanel.getBtnPrepareToAdd().addActionListener(_ -> prepareToAdd());
        operationPanel.getBtnPrepareToEdit().addActionListener(_ -> prepareToEdit());
        btnSave.addActionListener(_ -> save());
        btnUpdate.addActionListener(_ -> update());
        operationPanel.getBtnDelete().addActionListener(_ -> delete());


        cbxType.addActionListener(_ -> lblAccountTypeId.setText((cbxModelAccountType.getSelectedItem()).getId() + "."));
        cbxType.addActionListener(_ -> {
            var type = cbxModelAccountType.getSelectedItem();
            new FromTransactionWorker<>(
                    session -> new AccountSubtypeQuery_(session).findMinDataByType(type),
                    subtypes-> {
                        cbxModelAccountSubtype.setData(subtypes);
                        onFindSubtypes.ifPresent(e->e.accept(subtypes));
                        onFindSubtypes = Optional.empty();
                    },
                    this::showError
            ).execute();
        });
        rbAddSubtype.addActionListener(_ -> cbxSubtype.setEnabled(rbAddSubtype.isSelected()));

        cbxType.setSelectedItem(AccountType.ASSETS);
    }


    public void loadData() {
        tblData.setEmpty();
        prepareToAdd();
        new FromTransactionWorker<>(
                session -> new AccountQuery_(session).findAllDataAndSubtypes(),
                tblModel::setData,
                this::showError
        ).execute();
    }

    @Override
    public @NotNull AccountFormData getDataFromForm() throws InvalidFieldException {
        var type = cbxModelAccountType.getSelectedItem();
        var subtype = cbxModelAccountSubtype.getSelectedItem();

        if (txtName.getText().isBlank())
            throw new InvalidFieldException("El nombre no puede estar vacío");

        if (type == null)
            throw new InvalidFieldException("El tipo de cuenta no puede estar vacío");

        if (subtype == null)
            throw new InvalidFieldException("El subtipo no puede estar vacío");

        Optional<Integer> subtypeId = Optional.ofNullable(rbAddSubtype.isSelected() ? subtype.id() : null);

        return new AccountFormData(
                txtName.getText(),
                AccountEntity.generateNumber(spnModelNumber.getNumber().intValue(), type),
                type,
                subtypeId,
                user.getUsername()
        );
    }

    @Override
    public void setEntityDataInForm(@NotNull AccountData entity) {
        txtName.setText(entity.name());
        spnModelNumber.setValue(AccountEntity.getSubNumber(entity.number()));
        cbxType.setSelectedItem(entity.type());

        if (entity.subtypeId() == null) {
            rbAddSubtype.setSelected(false);
            cbxSubtype.setEnabled(false);
            btnUpdate.setEnabled(true);
            return;
        }

        rbAddSubtype.setSelected(true);
        cbxSubtype.setEnabled(false);

        onFindSubtypes = Optional.of((list) -> {
            list.stream()
                    .filter(e -> e.id().equals(entity.subtypeId()))
                    .findFirst()
                    .ifPresent(cbxModelAccountSubtype::setSelectedItem);
            cbxSubtype.setEnabled(true);
            btnUpdate.setEnabled(true);
        });
    }

    @Override
    public void prepareToAdd() {
        txtName.setText("");
        spnModelNumber.setValue(0);
        rbAddSubtype.setSelected(false);
        cbxSubtype.setEnabled(false);
        cbxType.setSelectedItem(AccountType.ASSETS);
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
    }

    @Override
    public void prepareToEdit() {
        tblData.getSelected()
                .ifPresentOrElse(this::setEntityDataInForm,
                        () -> showWarning("Seleccione un elemento de la tabla"));
        btnSave.setEnabled(false);
        btnUpdate.setEnabled(true);
    }

    @Override
    public void onSelected(@NotNull AccountData entity) {
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
        try{
            tblData
                    .getSelected()
                    .ifPresentOrElse(
                            accountData ->
                                    new InTransactionWorker(
                                            session -> {
                                                var queries = new AccountQuery_(session);
                                                var response = queries.findById(accountData.id());

                                                if (response.isEmpty())
                                                    throw new InvalidFieldException("La cuenta no pudo ser encontrada");

                                                var account = response.get();

                                                if (queries.isUsed(account))
                                                    throw new InvalidFieldException(
                                                            LabelBuilder.of("Existen Documentos que usan esta cuenta")
                                                                    .p("Para eliminarla debe eliminar primero esos registros, puede hacerlo desde la pantalla de Mayor General")
                                                                    .build()
                                                    );

                                                session.remove(account);
                                            },
                                            this::loadData,
                                            this::showError
                                    ).execute(),
                            () -> {
                                throw new InvalidFieldException("Seleccione un elemento de la tabla");
                            }
                    );
        }catch (InvalidFieldException e){
            showWarning(e);
        }
    }

    @Override
    public void save() {
        try {
            var dto = getDataFromForm();
            new InTransactionWorker(
                    session -> {
                        var account = new Account(dto.number(), dto.name(), dto.type(), dto.username());
                        if (dto.subtypeId().isPresent()) {
                            var subtype = new AccountSubtypeQuery_(session).findById(dto.subtypeId().get());
                            if (subtype.isEmpty()) {
                                throw new InvalidFieldException(
                                        LabelBuilder.of("El subtipo de cuenta no fue encontrado.")
                                                .p("Si no quiere agregar un subtipo, desmarque el check.")
                                                .p("Si es un error debe")
                                                .build());
                            }
                            account.setSubtype(subtype.get());
                        }
                        session.persist(account);
                    },
                    this::loadData,
                    this::showError
            ).execute();
        }catch (InvalidFieldException e) {
            showWarning(e);
        }
    }

    @Override
    public void update() {
        var dto = getDataFromForm();
        tblData.getSelected()
                .ifPresentOrElse(
                        AccountData ->
                                new InTransactionWorker(
                                        session -> {
                                            var response = new AccountQuery_(session).findById(AccountData.id());

                                            if (response.isEmpty()) {
                                                throw new ApplicationException(LabelBuilder.build("Cunta no ncontrada"));
                                            }
                                            var entity = response.get();

                                            entity.setUpdatedBy(dto.username());
                                            entity.setNumber(dto.number());
                                            entity.setName(dto.name());
                                            entity.setType(dto.type());

                                            if (dto.subtypeId().isEmpty()) {
                                                entity.setSubtype(null);
                                                return;
                                            }

                                            if (entity.getSubtype() != null
                                                    && entity.getSubtype().getId() != null
                                                    && entity.getSubtype().getId().equals(dto.subtypeId().get()))
                                                return;

                                            var subtype = new AccountSubtypeQuery_(session).findById(dto.subtypeId().get());


                                            if (subtype.isEmpty()) {
                                                throw new ApplicationException(
                                                        LabelBuilder.of("El subtipo de cuenta no fue encontrado.")
                                                                .p("Si no quiere agregar un subtipo, desmarque el check.")
                                                                .p("Si es un error debe")
                                                                .build());
                                            }
                                            entity.setSubtype(subtype.get());
                                        },
                                        this::loadData,
                                        this::showError
                                ).execute(),
                        () -> {
                            throw new InvalidFieldException("Seleccione un elemento de la tabla");
                        });
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
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAside = new javax.swing.JPanel();
        pnlForm = new javax.swing.JPanel();
        lblAccountName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblAccountId = new javax.swing.JLabel();
        lblAccountTypeId = new javax.swing.JLabel();
        lblAccountType = new javax.swing.JLabel();
        cbxType = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        cbxSubtype = new javax.swing.JComboBox<>();
        labelSection1 = new javax.swing.JLabel();
        sepaSection1 = new javax.swing.JSeparator();
        btnUpdate = new javax.swing.JButton();
        lblSave = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        rbAddSubtype = new javax.swing.JRadioButton();
        spnNumber = new javax.swing.JSpinner();
        auditablePanel = new com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel();
        operationPanel = new com.nutrehogar.sistemacontable.ui_2.component.OperationPanel(entityInfo);
        lblTitle = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblData = new com.nutrehogar.sistemacontable.ui_2.component.CustomTable(tblModel);

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(524, 664));

        pnlAside.setOpaque(false);
        pnlAside.setPreferredSize(new java.awt.Dimension(419, 652));

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Formulario"));
        pnlForm.setOpaque(false);

        lblAccountName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountName.setLabelFor(txtName);
        lblAccountName.setText("Nombre:");
        lblAccountName.setToolTipText("");

        lblAccountId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountId.setText("Numero:");
        lblAccountId.setToolTipText("");

        lblAccountTypeId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountTypeId.setText("1.");

        lblAccountType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountType.setLabelFor(cbxType);
        lblAccountType.setText("Tipo:");

        cbxType.setModel(cbxModelAccountType);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setLabelFor(cbxSubtype);
        jLabel1.setText("Subtipo de cuenta:");

        btnSave.setText("Guardar");
        btnSave.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        cbxSubtype.setModel(cbxModelAccountSubtype);

        labelSection1.setText("Operaciones");

        btnUpdate.setText("Actualizar");
        btnUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        lblSave.setLabelFor(btnSave);
        lblSave.setText("<html><p>Guarda la nueva "+entityName+"</p></html>");
        lblSave.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblSave.setPreferredSize(new java.awt.Dimension(250, 40));

        lblUpdate.setLabelFor(btnUpdate);
        lblUpdate.setText("<html><p>Actualiza los datos de la "+entityName+" seleccionada con los datos del formulario</p></html>");
        lblUpdate.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblUpdate.setPreferredSize(new java.awt.Dimension(250, 40));

        rbAddSubtype.setText("Agregar subtipo");

        spnNumber.setModel(spnModelNumber);

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(labelSection1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblAccountType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblAccountId, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblAccountName, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtName)
                            .addComponent(cbxType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbxSubtype, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sepaSection1)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rbAddSubtype)
                                    .addGroup(pnlFormLayout.createSequentialGroup()
                                        .addComponent(lblAccountTypeId)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spnNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                            .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccountName)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccountId)
                    .addComponent(lblAccountTypeId)
                    .addComponent(spnNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccountType)
                    .addComponent(cbxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbAddSubtype)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbxSubtype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(btnUpdate)))
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
                    .addComponent(auditablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                    .addComponent(operationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAsideLayout.setVerticalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(operationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(auditablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblTitle.setFont(lblTitle.getFont().deriveFont((float)30));
        lblTitle.setForeground(Theme.Palette.OFFICE_GREEN);
        lblTitle.setIcon(entityInfo.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(entityInfo.getPlural());

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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAside, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
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
    private javax.swing.JComboBox<AccountSubtypeMinData> cbxSubtype;
    private javax.swing.JComboBox<AccountType> cbxType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelSection1;
    private javax.swing.JLabel lblAccountId;
    private javax.swing.JLabel lblAccountName;
    private javax.swing.JLabel lblAccountType;
    private javax.swing.JLabel lblAccountTypeId;
    private javax.swing.JLabel lblSave;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUpdate;
    private com.nutrehogar.sistemacontable.ui_2.component.OperationPanel operationPanel;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JRadioButton rbAddSubtype;
    private javax.swing.JSeparator sepaSection1;
    private javax.swing.JSpinner spnNumber;
    private com.nutrehogar.sistemacontable.ui_2.component.CustomTable<AccountData> tblData;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables

}
