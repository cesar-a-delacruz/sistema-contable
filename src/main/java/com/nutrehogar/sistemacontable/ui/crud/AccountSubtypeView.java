package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.model.AccountNumber;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;

import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.AccountSubtypeQuery_;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomListCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;
import com.nutrehogar.sistemacontable.ui_2.component.OperationPanel;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;


import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class AccountSubtypeView extends CRUDView<AccountSubtype, AccountSubtypeFormData> {

    private final CustomComboBoxModel<AccountType> cbxModelAccountType;

    public AccountSubtypeView(User user) {
        super(user, "Subtipo de Cuenta");
        this.cbxModelAccountType = new CustomComboBoxModel<>(AccountType.values());
        this.tblModel = new CustomTableModel("Numero de Cuenta", "Nombre", "Tipo de Cuenta") {
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
        txtName.putClientProperty("JTextField.placeholderText", "Activos Corrientes");
        txtNumber.putClientProperty("JTextField.placeholderText", "11");
        configureTable(tblData);
        cbxType.setRenderer(new CustomListCellRenderer());
        addListenersToOperationPanel();
        loadData();
        btnSave.addActionListener(_ -> save());
        btnUpdate.addActionListener(_ -> update());
        cbxType.addActionListener(_ -> lblAccountTypeId.setText(((AccountType) cbxType.getSelectedItem()).getId() + "."));
        lblTitle.setFont(Theme.Typography.FONT_BASE.deriveFont(Font.PLAIN, 30));
    }

    @Override
    protected void delete() {
        if (selected.isEmpty()) {
            showMessage("Seleccione un elemento de la tabla");
            return;
        }
        new DeleteAccountSubtypeWorker(selected.get()).execute();
    }
    @Override
    protected void save() {
        var dto = getDataFromForm();
        new SaveAccountSubtypeWorker(dto).execute();
    }
    @Override
    protected void update() {
        if (selected.isEmpty() || selected.get().getId() == null) {
            showMessage("Seleccione un elemento de la tabla");
            return;
        }
        var dto = getDataFromForm();
        new EditAccountSubtypeWorker(selected.get(), dto).execute();
    }
    private final class EditAccountSubtypeWorker extends EditWorker {

        public EditAccountSubtypeWorker(@NotNull AccountSubtype entity, @NotNull AccountSubtypeFormData dto) {
            super(entity, dto);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            var entity = session.merge(this.entity);
            entity.setUpdatedBy(dto.username());
            entity.setNumber(dto.number());
            entity.setName(dto.name());
            entity.setType(dto.type());
        }
    }
    private final class SaveAccountSubtypeWorker extends SaveWorker {

        public SaveAccountSubtypeWorker(@NotNull AccountSubtypeFormData dto) {
            super(dto);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            session.persist(new AccountSubtype(dto.number(), dto.name(), dto.type(), dto.username()));
        }
    }
    private final class DeleteAccountSubtypeWorker extends DeleteWorker {
        public DeleteAccountSubtypeWorker(@NotNull AccountSubtype entity) {
            super(entity);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            session.remove(session.merge(this.entity));
        }
    }

    @Override
    protected @NotNull AccountSubtypeFormData getDataFromForm() {
        var type = (AccountType) cbxType.getSelectedItem();
        return new AccountSubtypeFormData(
                txtName.getText(),
                AccountNumber.generateNumber(txtNumber.getText(), type),
                type,
                user.getUsername()
        );
    }

    @Override
    protected void resetForm() {
        txtName.setText("");
        txtNumber.setText("");
        cbxType.setSelectedItem(AccountType.ASSETS);
    }

    @Override
    protected void setEntityDataInForm(@NotNull AccountSubtype entity) {
        txtName.setText(entity.getName());
        txtNumber.setText(entity.getSubNumber());
        cbxType.setSelectedItem(entity.getType());
    }

    @Override
    protected void prepareToAdd() {
        super.prepareToAdd();
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
    }

    @Override
    protected void prepareToEdit() {
        super.prepareToEdit();
        btnSave.setEnabled(false);
        btnUpdate.setEnabled(true);
    }

    @Override
    protected AuditablePanel getAuditablePanel() {
        return this.auditablePanel;
    }

    @Override
    protected OperationPanel getOperationPanel() {
        return this.operationPanel1;
    }

    @Override
    protected List<AccountSubtype> findEntities() {
        AtomicReference<List<AccountSubtype>> list = new AtomicReference<>(List.of());
        HibernateUtil
                .getSessionFactory()
                .inTransaction(session ->
                        list.set(new AccountSubtypeQuery_(session).findAll()));
        return list.get();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();
        pnlAside = new javax.swing.JPanel();
        pnlForm = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblNumber = new javax.swing.JLabel();
        lblAccountTypeId = new javax.swing.JLabel();
        txtNumber = new javax.swing.JTextField();
        lblType = new javax.swing.JLabel();
        cbxType = new javax.swing.JComboBox<>();
        btnSave = new javax.swing.JButton();
        labelSection1 = new javax.swing.JLabel();
        sepaSection1 = new javax.swing.JSeparator();
        btnUpdate = new javax.swing.JButton();
        lblSave = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        auditablePanel = new com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel();
        operationPanel1 = new com.nutrehogar.sistemacontable.ui_2.component.OperationPanel(entityName);
        lblTitle = new javax.swing.JLabel();

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

        tblData.setModel(tblModel);
        jScrollPane1.setViewportView(tblData);

        pnlAside.setOpaque(false);

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Formulario"));
        pnlForm.setOpaque(false);

        lblName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblName.setLabelFor(txtName);
        lblName.setText("Nombre:");

        txtName.setMaximumSize(new java.awt.Dimension(200, 200));

        lblNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNumber.setLabelFor(txtNumber);
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
        lblSave.setText("<html><p>Guarda el nuevo " + entityName + "</p></html>");
        lblSave.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblSave.setPreferredSize(new java.awt.Dimension(250, 40));

        lblUpdate.setLabelFor(btnUpdate);
        lblUpdate.setText("<html><p>Actualiza los datos del " + entityName + " seleccionado con los datos del formulario</p></html>");
        lblUpdate.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblUpdate.setPreferredSize(new java.awt.Dimension(250, 40));

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
                                                                .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                        .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                                        .addComponent(operationPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        pnlAsideLayout.setVerticalGroup(
                pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlAsideLayout.createSequentialGroup()
                                .addComponent(operationPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(auditablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(23, Short.MAX_VALUE))
        );

        lblTitle.setText("Subtipos de Cuentas");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                                        .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                                                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane1)))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel auditablePanel;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<AccountType> cbxType;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelSection1;
    private javax.swing.JLabel lblAccountTypeId;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNumber;
    private javax.swing.JLabel lblSave;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lblUpdate;
    private com.nutrehogar.sistemacontable.ui_2.component.OperationPanel operationPanel1;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JSeparator sepaSection1;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtNumber;
    // End of variables declaration//GEN-END:variables

}
