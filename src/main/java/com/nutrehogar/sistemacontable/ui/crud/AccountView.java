package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.query.AccountQuery_;
import com.nutrehogar.sistemacontable.query.AccountSubtypeQuery_;

import javax.swing.*;

import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomListCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;
import com.nutrehogar.sistemacontable.ui_2.component.OperationPanel;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class AccountView extends CRUDView<AccountData, AccountFormData> {

    private final CustomComboBoxModel<AccountType> cbxModelAccountType;
    private List<AccountSubtypeMinData> accountSubtypeMinData;
    private final CustomComboBoxModel<AccountSubtypeMinData> cbxModelAccountSubtype;
    private Optional<Runnable> onFindSubtypes = Optional.empty();

    public AccountView(User user) {
        super(user, "Cuenta");
        this.cbxModelAccountType = new CustomComboBoxModel<>(AccountType.values());
        this.accountSubtypeMinData = List.of();
        this.cbxModelAccountSubtype = new CustomComboBoxModel<>(accountSubtypeMinData);
        this.tblModel = new CustomTableModel("Numero", "Nombre", "Tipo de Cuenta", "Subtipo de Cuenta") {
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                var dto = data.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> AccountNumber.getFormattedNumber(dto.number());
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
        txtName.putClientProperty("JTextField.placeholderText", "Caja Menuda");
        txtNumber.putClientProperty("JTextField.placeholderText", "11");
        configureTable(tblData);
        cbxType.setRenderer(new CustomListCellRenderer());
        cbxSubtype.setRenderer(new CustomListCellRenderer());
        addListenersToOperationPanel();
        loadData();
        btnSave.addActionListener(_ -> save());
        btnUpdate.addActionListener(_ -> update());
        cbxType.addActionListener(_ -> lblAccountTypeId.setText(((AccountType) cbxType.getSelectedItem()).getId() + "."));
        cbxType.addActionListener(_ -> new CbxModelAccountSubtypeDataLoader((AccountType) cbxType.getSelectedItem()).execute());
        rbAddSubtype.addActionListener(_ -> cbxSubtype.setEnabled(rbAddSubtype.isSelected()));
        lblTitle.setFont(Theme.Typography.FONT_BASE.deriveFont(Font.PLAIN, 30));
    }

    @Override
    protected void delete() {
        if (selected.isEmpty()) {
            showMessage("Seleccione un elemento de la tabla");
            return;
        }
        new DeleteAccountWorker(selected.get()).execute();
    }

    @Override
    protected void save() {
        var dto = getDataFromForm();
        new SaveAccountWorker(dto).execute();
    }

    @Override
    protected void update() {
        if (selected.isEmpty()) {
            showMessage("Seleccione un elemento de la tabla");
            return;
        }
        var dto = getDataFromForm();
        new EditAccountWorker(selected.get(), dto).execute();
    }

    private final class EditAccountWorker extends EditWorker {

        public EditAccountWorker(@NotNull AccountData entity, @NotNull AccountFormData dto) {
            super(entity, dto);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            IO.println("se busca");
            var response = new AccountQuery_(session).findById(this.entity.id());

            if(response.isEmpty()){
                throw new ApplicationException(LabelBuilder.build("Cunta no ncontrada"));
            }
            var entity = response.get();

            entity.setUpdatedBy(dto.username());
            entity.setNumber(dto.number());
            entity.setName(dto.name());
            entity.setType(dto.type());

            IO.println("is empty");
            if (dto.subtypeId().isEmpty()) return;
            IO.println("sis equals");

            if (entity.getSubtype() != null
                    && entity.getSubtype().getId() != null
                    && entity.getSubtype().getId().equals(dto.subtypeId().get()))
                return;
            IO.println("se busca 2");
            var subtype = new AccountSubtypeQuery_(session).findById(dto.subtypeId().get());

            IO.println("isempty");
            if (subtype.isEmpty()) {
                throw new ApplicationException(
                        LabelBuilder.of("El subtipo de cuenta no fue encontrado.")
                                .p("Si no quiere agregar un subtipo, desmarque el check.")
                                .p("Si es un error debe")
                                .build());
            }
            IO.println("se pone");
            entity.setSubtype(subtype.get());
        }
    }

    private final class SaveAccountWorker extends SaveWorker {

        public SaveAccountWorker(@NotNull AccountFormData dto) {
            super(dto);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            var account = new Account(dto.number(), dto.name(), dto.type(), dto.username());
            if (dto.subtypeId().isPresent()) {
                var subtype = new AccountSubtypeQuery_(session).findById(dto.subtypeId().get());
                if (subtype.isEmpty()) {
                    throw new ApplicationException(
                            LabelBuilder.of("El subtipo de cuenta no fue encontrado.")
                                    .p("Si no quiere agregar un subtipo, desmarque el check.")
                                    .p("Si es un error debe")
                                    .build());
                }
                account.setSubtype(subtype.get());
            }
            session.persist(account);
        }
    }

    private final class DeleteAccountWorker extends DeleteWorker {
        public DeleteAccountWorker(@NotNull AccountData entity) {
            super(entity);
        }

        @Override
        protected void inTransaction(@NotNull Session session) {
            new AccountQuery_(session).findById(this.entity.id()).ifPresent(session::remove);
        }
    }

    private final class CbxModelAccountSubtypeDataLoader extends SwingWorker<List<AccountSubtypeMinData>, Void> {
        @NotNull
        private final AccountType type;
        @Nullable
        private ApplicationException error;

        private CbxModelAccountSubtypeDataLoader(@NotNull AccountType type) {
            this.type = type;
        }

        @Override
        protected @NotNull List<AccountSubtypeMinData> doInBackground() {
            AtomicReference<List<AccountSubtypeMinData>> list = new AtomicReference<>(List.of());
            try {
                HibernateUtil
                        .getSessionFactory()
                        .inTransaction(session -> list.set(new AccountSubtypeQuery_(session).findMinDataByType(type)));
            } catch (ConstraintViolationException cve) {
                error = new ApplicationException(
                        LabelBuilder.of("Los datos ingresados son inválidos.")
                                .p("Por favor revise qe no alla cambos único que se repitan, ejm: Nombres, Números de Cuentas")
                                .build(),
                        cve);
            } catch (HibernateException he) {
                error = new ApplicationException(
                        LabelBuilder.of("Ocurrió un error en la base de datos, inténtelo nuevamente")
                                .p("Si el problema persiste valla al inicio y regrese")
                                .p("Si el problema persiste, cierre el programa y vuelva a intentarlo.")
                                .build(),
                        he);
            } catch (Exception e) {
                error = new ApplicationException(
                        LabelBuilder.of("Ocurrió un error inesperado, por favor inténtelo de nuevo.")
                                .p("Si el problema persiste, cierre el programa y vuelva a intentarlo.")
                                .build(),
                        e);
            }
            return list.get();
        }

        @Override
        protected void done() {
            accountSubtypeMinData = List.of();

            try {
                if (get() == null) {
                    error = new ApplicationException(LabelBuilder.build("Error al optener la lista de subtipos de cuentas"));
                }
                if (get().isEmpty() || get().getFirst() == null) {
                    error = new ApplicationException(LabelBuilder.of("La lista esta vacía.")
                            .p("Para agregar un subtipo de cuenta debe seleccionar un tipo de cuenta que tenga subtipos.")
                            .build()
                    );
                }
                accountSubtypeMinData = get();
            } catch (Exception e) {
                error = new ApplicationException(LabelBuilder.build("Error al optener la lista de subtipos de cuentas"), e);
            }

            cbxModelAccountSubtype.setData(accountSubtypeMinData);

            if (error != null) {
                showError(error.getMessage(), error);
                return;
            }

            onFindSubtypes.ifPresent(Runnable::run);
            onFindSubtypes = Optional.empty();
        }
    }


    @Override
    protected @NotNull AccountFormData getDataFromForm() {
        var type = (AccountType) cbxType.getSelectedItem();

        Optional<Integer> subtypeId = rbAddSubtype.isSelected()
                ? Optional.of(((AccountSubtypeMinData) cbxSubtype.getSelectedItem()).id())
                : Optional.empty();

        return new AccountFormData(
                txtName.getText(),
                AccountNumber.generateNumber(txtNumber.getText(), type),
                type,
                subtypeId,
                user.getUsername()
        );
    }

    @Override
    protected void resetForm() {
        txtName.setText("");
        txtNumber.setText("");
        rbAddSubtype.setSelected(false);
        cbxSubtype.setEnabled(false);
        cbxType.setSelectedItem(AccountType.ASSETS);
    }

    @Override
    protected void setEntityDataInForm(@NotNull AccountData entity) {
        txtName.setText(entity.name());
        txtNumber.setText(AccountNumber.getSubNumber(entity.number()));
        cbxType.setSelectedItem(entity.type());

        if (entity.subtypeId() == null) {
            rbAddSubtype.setSelected(false);
            cbxSubtype.setEnabled(false);
            btnUpdate.setEnabled(true);
            return;
        }

        rbAddSubtype.setSelected(true);
        cbxSubtype.setEnabled(false);

        onFindSubtypes = Optional.of(() -> {
            accountSubtypeMinData
                    .stream()
                    .filter(e -> e.id().equals(entity.subtypeId()))
                    .findFirst()
                    .ifPresent(cbxModelAccountSubtype::setSelectedItem);
            cbxSubtype.setEnabled(true);
            btnUpdate.setEnabled(true);
        });
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
    protected List<AccountData> findEntities() {
        AtomicReference<List<AccountData>> list = new AtomicReference<>(List.of());
        HibernateUtil.getSessionFactory().inTransaction(session -> list.set(new AccountQuery_(session).findAllDataAndSubtypes()));
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
        rbAddSubtype = new javax.swing.JRadioButton();
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
        lblAccountName.setText("Nombre:");
        lblAccountName.setToolTipText("");

        lblAccountId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAccountId.setLabelFor(txtNumber);
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

        cbxSubtype.setModel(cbxModelAccountSubtype);

        labelSection1.setText("Operaciones");

        btnUpdate.setText("Actualizar");

        lblSave.setLabelFor(btnSave);
        lblSave.setText("<html><p>Guarda la nueva " + entityName + "</p></html>");
        lblSave.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblSave.setPreferredSize(new java.awt.Dimension(250, 40));

        lblUpdate.setLabelFor(btnUpdate);
        lblUpdate.setText("<html><p>Actualiza los datos de la " + entityName + " seleccionada con los datos del formulario</p></html>");
        lblUpdate.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblUpdate.setPreferredSize(new java.awt.Dimension(250, 40));

        rbAddSubtype.setText("Agregar subtipo");

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
                                                                        .addGroup(pnlFormLayout.createSequentialGroup()
                                                                                .addComponent(lblAccountTypeId)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addComponent(rbAddSubtype))
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
                                        .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                                        .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlAside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(pnlAside, javax.swing.GroupLayout.PREFERRED_SIZE, 675, Short.MAX_VALUE)
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
    private javax.swing.JComboBox<AccountSubtypeMinData> cbxSubtype;
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
    private javax.swing.JRadioButton rbAddSubtype;
    private javax.swing.JSeparator sepaSection1;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtNumber;
    // End of variables declaration//GEN-END:variables

}
