package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.application.config.Theme;
import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.AccountQuery_;
import com.nutrehogar.sistemacontable.query.AccountSubtypeQuery_;
import com.nutrehogar.sistemacontable.ui.crud.CRUDView;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;

import javax.swing.JButton;

import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomListCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;
import com.nutrehogar.sistemacontable.ui_2.component.OperationPanel;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import org.hibernate.HibernateException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class AccountView extends CRUDView<Account> {

    private final CustomComboBoxModel<AccountType> cbxModelAccountType;
    public AccountView(User user) {
        super(user,"Cuenta");
        this.cbxModelAccountType = new CustomComboBoxModel<>(AccountType.values());
        this.tblModel = new CustomTableModel("Numero", "Nombre", "Tipo de Cuenta", "Subtipo de Cuenta") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var dto = data.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> dto.getFormattedNumber();
                    case 1 -> dto.getName();
                    case 2 -> dto.getType();
                    case 3 -> dto.getSubtype() == null ? null : dto.getSubtype().getName();
                    default -> "que haces?";
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? AccountType.class : String.class;
            }
        };
        initComponents();
        txtName.putClientProperty("JTextField.placeholderText", "Caja Menuda");
        txtNumber.putClientProperty("JTextField.placeholderText", "11");
        configureTable(tblData);
        cbxType.setRenderer(new CustomListCellRenderer());
        addListenersToOperationPanel();
        loadData();
        btnSave.addActionListener(_ -> save());
        btnUpdate.addActionListener(_ -> update());
        lblTitle.setFont(Theme.Typography.FONT_BASE.deriveFont(Font.PLAIN,30));
    }



    @Override
    protected void delete() {
        selected.ifPresentOrElse(entity-> HibernateUtil.getSessionFactory().inStatelessTransaction(statelessSession -> statelessSession.delete(entity)),()->showMessage("Seleccione un elemento de la tabla"));
        loadData();
    }

    @Override
    protected void save() {
        try{
            HibernateUtil
                    .getSessionFactory()
                    .inTransaction(session -> session.persist(setEntityDataFromForm(new Account(user.getUsername()))));
        } catch (ConstraintViolationException cve) {
            // mostrar advertencia de validación al usuario
            showError("Error al guardar los datos", cve);
        } catch (HibernateException he) {
            // error general de persistencia
            showError("Error al guardar los datos", he);
        } catch (Exception e) {
            // fallback general
            showError("Error al actualizar los datos", e);
        }
        loadData();
    }

    @Override
    protected void update() {
        if (selected.isEmpty() || selected.get().getId() == null) {
            showMessage("Seleccione un elemento de la tabla");
            return;
        }
        try{
            HibernateUtil.getSessionFactory()
                    .inTransaction(session -> {
                        var repo = new AccountQuery_(session);
                        repo.findById(selected.get().getId()).ifPresent(this::setEntityDataFromForm);
                    });
        } catch (ConstraintViolationException cve) {
            // mostrar advertencia de validación al usuario
            showError("Error al actualizar los datos", cve);
        } catch (HibernateException he) {
            // error general de persistencia
            showError("Error al actualizar los datos", he);
        } catch (Exception e) {
            // fallback general
            showError("Error al actualizar los datos", e);
        }
        loadData();
    }

    @Override
    protected @NotNull Account setEntityDataFromForm(@NotNull Account entity) {
        entity.setName(txtName.getText());
        entity.setNumber(txtNumber.getText(), (AccountType) cbxType.getSelectedItem());
        entity.setType((AccountType) cbxType.getSelectedItem());
        entity.setUpdatedBy(user.getUsername());
        return entity;
    }

    @Override
    protected void resetForm() {
        txtName.setText("");
        txtNumber.setText("");
        cbxType.setSelectedItem(AccountType.ASSETS);
    }
    @Override
    protected @NotNull Account setEntityDataInForm(@NotNull Account entity) {
        txtName.setText(entity.getName());
        txtNumber.setText(entity.getSubNumber());
        cbxType.setSelectedItem(entity.getType());
        return entity;
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
    protected java.util.List<Account> findEntities() {
        AtomicReference<java.util.List<Account>> list = new AtomicReference<>(List.of());
        HibernateUtil.getSessionFactory().inStatelessTransaction(statelessSession -> list.set(statelessSession.createQuery("select a from Account a", Account.class).list()));
        return list.get();
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

        jScrollPane1 = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();
        pnlAside = new javax.swing.JPanel();
        pnlForm = new javax.swing.JPanel();
        lblAccountName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblAccountId = new javax.swing.JLabel();
        lblAccountTypeId = new javax.swing.JLabel();
        txtNumber = new javax.swing.JTextField();
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
        auditablePanel = new com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel();
        operationPanel1 = new com.nutrehogar.sistemacontable.ui_2.component.OperationPanel(entityName);
        lblTitle = new javax.swing.JLabel();

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(524, 664));

        tblData.setModel(tblModel);
        jScrollPane1.setViewportView(tblData);

        pnlAside.setOpaque(false);
        pnlAside.setPreferredSize(new java.awt.Dimension(419, 652));

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Formulario"));
        pnlForm.setOpaque(false);

        lblAccountName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountName.setLabelFor(txtName);
        lblAccountName.setText("Nombre de cuenta:");
        lblAccountName.setToolTipText("");

        lblAccountId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountId.setLabelFor(txtNumber);
        lblAccountId.setText("Código de cuenta:");
        lblAccountId.setToolTipText("");

        lblAccountTypeId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountTypeId.setText("1.");

        lblAccountType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountType.setLabelFor(cbxType);
        lblAccountType.setText("Tipo de cuenta:");

        cbxType.setModel(cbxModelAccountType);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setLabelFor(cbxSubtype);
        jLabel1.setText("Subtipo de cuenta:");

        btnSave.setText("Guardar");

        cbxSubtype.setModel(new javax.swing.DefaultComboBoxModel<>());

        labelSection1.setText("Operaciones");

        btnUpdate.setText("Actualizar");

        lblSave.setLabelFor(btnSave);
        lblSave.setText("<html><p>Guarda la nueva "+entityName+"</p></html>");
        lblSave.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblSave.setPreferredSize(new java.awt.Dimension(250, 40));

        lblUpdate.setLabelFor(btnUpdate);
        lblUpdate.setText("<html><p>Actualiza los datos de la "+entityName+" seleccionada con los datos del formulario</p></html>");
        lblUpdate.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblUpdate.setPreferredSize(new java.awt.Dimension(250, 40));

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
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(lblAccountTypeId)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(cbxSubtype, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sepaSection1)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFormLayout.createSequentialGroup()
                        .addComponent(lblSave, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addComponent(lblUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)))
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
                    .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccountType)
                    .addComponent(cbxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        operationPanel1.setOpaque(false);

        javax.swing.GroupLayout pnlAsideLayout = new javax.swing.GroupLayout(pnlAside);
        pnlAside.setLayout(pnlAsideLayout);
        pnlAsideLayout.setHorizontalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlForm, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(auditablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                    .addComponent(operationPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAsideLayout.setVerticalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(operationPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(auditablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblTitle.setText("Cuentas");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAside, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1))))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel auditablePanel;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<AccountSubtype> cbxSubtype;
    private javax.swing.JComboBox<AccountType> cbxType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelSection1;
    private javax.swing.JLabel lblAccountId;
    private javax.swing.JLabel lblAccountName;
    private javax.swing.JLabel lblAccountType;
    private javax.swing.JLabel lblAccountTypeId;
    private javax.swing.JLabel lblSave;
    private javax.swing.JLabel lblTitle;
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
