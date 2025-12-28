package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.PasswordHasher;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.model.Permission;

import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.UserQuery_;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomListCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;
import com.nutrehogar.sistemacontable.ui_2.component.OperationPanel;
import lombok.Getter;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class UserView extends CRUDView<User,UserFormData> {

    private final CustomComboBoxModel<Permission> cbxModelPermision;
    public UserView(User user) {
        super(user,"Subtipo de Cuenta");
        this.cbxModelPermision = new CustomComboBoxModel<>(Permission.values());
        this.tblModel = new CustomTableModel("Nombre", "Contraseña", "Habilitado", "Permiso") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var user = data.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> user.getUsername();
                    case 1 -> "************";
                    case 2 -> user.getEnabled();
                    case 3 -> user.getPermissions();
                    default -> "que haces?";
                };
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 2 -> Boolean.class;
                    case 3 -> Permission.class;
                    default -> String.class;
                };
            }
        };
        initComponents();
        txtName.putClientProperty("JTextField.placeholderText", "Lic. Ema Perez");
        txtPassword.putClientProperty("JTextField.placeholderText", "20010");
        configureTable(tblData);
        cbxPermissions.setRenderer(new CustomListCellRenderer());
        addListenersToOperationPanel();
        loadData();
        btnSave.addActionListener(_ -> save());
        btnUpdate.addActionListener(_ -> update());
        lblTitle.setFont(Theme.Typography.FONT_BASE.deriveFont(Font.PLAIN,30));
    }



    @Override
    protected void delete() {
        if (selected.isEmpty()) {
            showMessage("Seleccione un elemento de la tabla");
            return;
        }
        new DeleteUserWorker(selected.get()).execute();
    }
    @Override
    protected void save() {
        var dto = getDataFromForm();
        if(dto.password().isEmpty()){
            showMessage(LabelBuilder.build("La contraseña es requerida"));
            return;
        }
        new SaveUserWorker(dto).execute();
    }
    @Override
    protected void update() {
        if (selected.isEmpty() || selected.get().getId() == null) {
            showMessage("Seleccione un elemento de la tabla");
            return;
        }
        var dto = getDataFromForm();
        new EditUserWorker(selected.get(), dto).execute();
    }
    private final class EditUserWorker extends EditWorker {

        public EditUserWorker(@NotNull User entity, @NotNull UserFormData dto) {
            super(entity, dto);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            var entity = session.merge(this.entity);
            entity.setUpdatedBy(dto.updatedBy());
            entity.setEnabled(dto.isEnable());
            dto.password().ifPresent(entity::setPassword);
            entity.setPermissions(dto.permission());
        }
    }
    private final class SaveUserWorker extends SaveWorker {

        public SaveUserWorker(@NotNull UserFormData dto) {
            super(dto);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            if(dto.password().isEmpty()){
                throw new ApplicationException(LabelBuilder.build("La contraseña es requerida"));
            }
            session.persist(new User(dto.password().get(), dto.username(), dto.isEnable(),dto.permission(),dto.updatedBy()));
        }
    }
    private final class DeleteUserWorker extends DeleteWorker {
        public DeleteUserWorker(@NotNull User entity) {
            super(entity);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            session.remove(session.merge(this.entity));
        }
    }

    @Override
    protected UserFormData getDataFromForm() {
        Optional<String> pass = txtPassword.getText().isBlank()
                ? Optional.empty()
                : Optional.of(PasswordHasher.hashPassword(getTxtPassword().getText()));

        return new UserFormData(
                txtName.getText(),
                chkIsEnable.isSelected(),
                (Permission) cbxPermissions.getSelectedItem(),
                pass,
                user.getUsername()
        );
    }

    @Override
    protected void resetForm() {
        txtName.setText("");
        txtPassword.setText("");
        chkIsEnable.setSelected(true);
        cbxPermissions.setSelectedItem(Permission.ADMIN);
    }
    @Override
    protected void setEntityDataInForm(@NotNull User entity) {
        txtName.setText(entity.getUsername());
        chkIsEnable.setSelected(entity.getEnabled());
        txtPassword.setText("");
        cbxPermissions.setSelectedItem(entity.getPermissions());
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
    protected List<User> findEntities() {
        AtomicReference<List<User>> list = new AtomicReference<>(List.of());
            HibernateUtil
                    .getSessionFactory()
                    .inTransaction(session -> list.set(new UserQuery_(session).findAll()));
        return list.get();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();
        pnlAside = new javax.swing.JPanel();
        pnlForm = new javax.swing.JPanel();
        lblUsername = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        labelSection1 = new javax.swing.JLabel();
        sepaSection1 = new javax.swing.JSeparator();
        btnUpdate = new javax.swing.JButton();
        lblSave = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        lblUserPasswod = new javax.swing.JLabel();
        txtPassword = new javax.swing.JTextField();
        chkIsEnable = new javax.swing.JCheckBox();
        cbxPermissions = new javax.swing.JComboBox<>();
        lblUserPasswod1 = new javax.swing.JLabel();
        auditablePanel = new com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel();
        operationPanel1 = new com.nutrehogar.sistemacontable.ui_2.component.OperationPanel(entityName);
        lblTitle = new javax.swing.JLabel();

        setOpaque(false);

        tblData.setModel(tblModel);
        jScrollPane1.setViewportView(tblData);

        pnlAside.setOpaque(false);

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Formulario"));
        pnlForm.setOpaque(false);

        lblUsername.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUsername.setLabelFor(txtName);
        lblUsername.setText("Nombre de Usuario:");

        btnSave.setText("Guardar");

        labelSection1.setText("Operaciones");

        btnUpdate.setText("Actualizar");

        lblSave.setLabelFor(btnSave);
        lblSave.setText("<html><p>Guarda el nuevo usuario registrado en la base de datos</p></html>");
        lblSave.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblSave.setPreferredSize(new java.awt.Dimension(250, 40));

        lblUpdate.setLabelFor(btnUpdate);
        lblUpdate.setText("<html><p>Actualiza los datos del usuario seleccionado con los datos del formulario</p></html>");
        lblUpdate.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblUpdate.setPreferredSize(new java.awt.Dimension(250, 40));

        lblUserPasswod.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUserPasswod.setLabelFor(txtName);
        lblUserPasswod.setText("Contraseña:");

        chkIsEnable.setText("Habilitado");

        cbxPermissions.setModel(cbxModelPermision);

        lblUserPasswod1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUserPasswod1.setLabelFor(txtName);
        lblUserPasswod1.setText("Permiso:");

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFormLayout.createSequentialGroup()
                        .addComponent(lblSave, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addComponent(lblUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblUserPasswod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelSection1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(lblUserPasswod1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtName)
                            .addComponent(sepaSection1)
                            .addComponent(txtPassword)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(chkIsEnable)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(cbxPermissions, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUserPasswod)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkIsEnable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxPermissions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUserPasswod1))
                .addGap(23, 23, 23)
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
                    .addComponent(auditablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addContainerGap(35, Short.MAX_VALUE))
        );

        lblTitle.setText("Usuarios");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1))
                    .addComponent(pnlAside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel auditablePanel;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<Permission> cbxPermissions;
    private javax.swing.JCheckBox chkIsEnable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelSection1;
    private javax.swing.JLabel lblSave;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUpdate;
    private javax.swing.JLabel lblUserPasswod;
    private javax.swing.JLabel lblUserPasswod1;
    private javax.swing.JLabel lblUsername;
    private com.nutrehogar.sistemacontable.ui_2.component.OperationPanel operationPanel1;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JSeparator sepaSection1;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPassword;
    // End of variables declaration//GEN-END:variables
}
