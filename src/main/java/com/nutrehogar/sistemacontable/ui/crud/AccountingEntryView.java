package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.exception.InvalidFieldException;
import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.query.AccountQuery_;
import com.nutrehogar.sistemacontable.query.LedgerRecordQuery_;
import com.nutrehogar.sistemacontable.service.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.ui.View;

import com.nutrehogar.sistemacontable.ui_2.builder.CustomComboBoxModel;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomListCellRenderer;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.MathContext.DECIMAL128;

@Slf4j
@Getter
public class AccountingEntryView extends View implements CRUDView<JournalEntry, JournalFormData> {
    @NotNull
    protected final RecordController recordController;
    /**
     * Si esta vacio es que eta en modo crear, si esta es modo editar
     */
    @NotNull
    protected Optional<JournalEntry> journalEntry = Optional.empty();

    @NotNull
    public final String entityName;

    public AccountingEntryView(@NotNull User user) {
        super(user);
        this.entityName = "Entrada de Diario";
        recordController = new RecordController();
        initComponents();
        recordController.init();
        txtJName.putClientProperty("JTextField.placeholderText", "Ventas S.A.");
        taJConcept.putClientProperty("JTextArea.placeholderText", "Cancelación de factura al crédito");
        txtJDoctNumber.putClientProperty("JTextField.placeholderText", "120");
        txtJCheckNumber.putClientProperty("JTextField.placeholderText", "4987");
        txtRReference.addActionListener(_->btnRSave.doClick());
    }

    @Override
    public void loadData() {

    }

    @Override
    public @NotNull JournalFormData getDataFromForm() throws InvalidFieldException {
        return null;
    }

    @Override
    public void setEntityDataInForm(@NotNull JournalEntry journalEntry) {

    }

    @Override
    public void prepareToAdd() {

    }

    @Override
    public void prepareToEdit() {

    }

    @Override
    public void onSelected(@NotNull JournalEntry journalEntry) {

    }

    @Override
    public void onDeselected() {

    }

    @Override
    public void delete() {

    }

    @Override
    public void save() {

    }

    @Override
    public void update() {

    }

    private final class RecordController {
        @NotNull
        public final CustomTableModel<RecordTableData> tblModelRecord;
        @NotNull
        public final CustomComboBoxModel<Account> cbxModelRAccount;
        @NotNull
        public final SpinnerNumberModel spnModelRAmount;
        @NotNull
        private final SpinnerNumberModel spnModelRAccountNumber;

        @NotNull
        public final String entityName;
        @NotNull
        public final ArrayList<Account> accounts;
        @NotNull
        public final ArrayList<RecordFormData> formRecords;
        @NotNull
        public final ArrayList<LedgerRecord> records;

        public RecordController() {
            this.entityName = "Registro";
            this.cbxModelRAccount = new CustomComboBoxModel<>();
            this.accounts = new ArrayList<>();
            this.records = new ArrayList<>();
            this.formRecords = new ArrayList<>();
            this.spnModelRAccountNumber = new SpinnerNumberModel(0, 0, 9999, 1);
            this.spnModelRAmount = new SpinnerNumberModel(1.0d, 0.0d, Integer.MAX_VALUE, 1.0d);
            this.tblModelRecord = new CustomTableModel<>("Referencia", "Cuenta", "Débito", "Crédito", "Subtotal") {

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    var record = data.get(rowIndex);
                    return switch (columnIndex) {
                        case 0 -> record.reference();
                        case 1 -> record.account();
                        case 2 -> record.debit();
                        case 3 -> record.credit();
                        case 4 -> record.total();
                        default -> "que haces?";
                    };
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return switch (columnIndex) {
                        case 1 -> Account.class;
                        case 2, 3, 4 -> BigDecimal.class;
                        default -> String.class;
                    };
                }
            };
        }

        public void init() {
            loadData();
            new FromTransactionWorker<>(
                    session -> new AccountQuery_(session).findAll(),
                    a -> {
                        accounts.addAll(a);
                        filterAccounts();
                    },
                    AccountingEntryView.this::showError
            ).execute();
            spnRAccountNumber.setEditor(new JSpinner.NumberEditor(spnRAccountNumber, "#"));
            cbxRAccount.setRenderer(new CustomListCellRenderer());
            txtRReference.putClientProperty("JTextField.placeholderText", "Pago de factura");
            tblRecord.setOnDeselected(this::onDeselected);
            tblRecord.setOnSelected(this::onSelected);
            OpRecord.getBtnPrepareToAdd().addActionListener(_ -> prepareToAdd());
            OpRecord.getBtnPrepareToEdit().addActionListener(_ -> prepareToEdit());
            btnRSave.addActionListener(_ -> save());
            btnRUpdate.addActionListener(_ -> update());
            OpRecord.getBtnDelete().addActionListener(_ -> delete());
            spnRAccountNumber.addChangeListener(_ -> filterAccounts());
        }

        public void filterAccounts() {
            var value = spnModelRAccountNumber.getNumber().intValue();
            if (value == 0) {
                cbxModelRAccount.setData(accounts);
                return;
            }
            cbxModelRAccount
                    .setData(accounts.stream()
                            .filter(a -> a
                                    .getNumber()
                                    .toString()
                                    .contains(String.valueOf(value)))
                            .toList());
        }

        public void loadData() {
            tblRecord.setEmpty();
            prepareToAdd();
            journalEntry.ifPresentOrElse(journalEntry ->
                    new FromTransactionWorker<>(
                            session -> new LedgerRecordQuery_(session).findAllAndAccountsByJournal(journalEntry),
                            RecordController.this::setDataToTable,
                            AccountingEntryView.this::showError
                    ).execute(),
                    () -> setDataToTable(List.of())
            );
        }

        public void setDataToTable(@NotNull List<LedgerRecord> records) {
            var debitSum = BigDecimal.ZERO;
            var creditSum = BigDecimal.ZERO;
            var totalSum = BigDecimal.ZERO;
            var dtos = new ArrayList<RecordTableData>();
            for (var record : records) {
                var account = record.getAccount();
                if (account == null) {
                    records.remove(record);
                    continue;
                }
                debitSum = debitSum.add(record.getDebit(), DECIMAL128);
                creditSum = creditSum.add(record.getCredit(), DECIMAL128);
                totalSum = account.getType().getBalance(totalSum, record.getCredit(), record.getDebit());
                dtos.add(
                        new RecordTableData(
                                record,
                                record.getReference(),
                                account,
                                record.getDebit(),
                                record.getCredit(),
                                totalSum
                        )
                );
            }
            for (var record : formRecords) {
                var account = record.account();
                debitSum = debitSum.add(record.debit(), DECIMAL128);
                creditSum = creditSum.add(record.credit(), DECIMAL128);
                totalSum = account.getType().getBalance(totalSum, record.credit(), record.debit());
                dtos.add(
                        new RecordTableData(
                                null,
                                record.reference(),
                                account,
                                record.debit(),
                                record.credit(),
                                totalSum
                        )
                );
            }
            dtos.add(new RecordTableData(
                    "Total",
                    debitSum,
                    creditSum,
                    totalSum
            ));
            tblModelRecord.setData(dtos);
        }


        public @NotNull RecordFormData getDataFromForm() throws InvalidFieldException {
            var reference = txtRReference.getText();
            var account = cbxModelRAccount.getSelectedItem();
            if (account == null) throw new InvalidFieldException("La cuenta no puede estar vacía");
            var amount = BigDecimal.valueOf(spnModelRAmount.getNumber().doubleValue());
            var isDebit = rbtRDebit.isSelected();
            return new RecordFormData(
                    reference,
                    account,
                    isDebit ? amount : BigDecimal.ZERO,
                    !isDebit ? amount : BigDecimal.ZERO,
                    user.getUsername()
            );
        }


        public void setEntityDataInForm(@NotNull RecordTableData record) {
            cbxModelRAccount.setSelectedItem(record.account());
            txtRReference.setText(record.reference());
            var isDebit = record.credit().equals(BigDecimal.ZERO);
            rbtRDebit.setSelected(isDebit);
            rbtRCredit.setSelected(!isDebit);
            spnModelRAmount.setValue(isDebit ? record.debit() : record.credit());
        }


        public void prepareToAdd() {
            btnRSave.setEnabled(true);
            btnRUpdate.setEnabled(false);
        }


        public void prepareToEdit() {
            tblRecord
                    .getSelected()
                    .ifPresentOrElse(
                            this::setEntityDataInForm,
                            () -> showWarning("Seleccione un elemento de la tabla")
                    );
            btnRSave.setEnabled(false);
            btnRUpdate.setEnabled(true);
        }


        public void onSelected(@NotNull RecordTableData record) {
            if(record.reference().equals("Total") && record.account() == null){
                tblRecord.setEmpty();
                return;
            }
            journalEntry.ifPresent(journalEntry -> {
                if(journalEntry.getRecords() != null && !journalEntry.getRecords().isEmpty()){
                    journalEntry
                            .getRecords()
                            .stream()
                            .filter(r->r.getId().equals(record.id()))
                            .findFirst()
                            .ifPresent(r-> ApRecord.setAuditableFields(r));
                }
            });
            OpRecord.getBtnDelete().setEnabled(true);
            OpRecord.getBtnPrepareToEdit().setEnabled(true);
        }


        public void onDeselected() {
            OpRecord.getBtnDelete().setEnabled(false);
            OpRecord.getBtnPrepareToEdit().setEnabled(false);
        }


        public void delete() {
            try {
                tblRecord.getSelected()
                        .ifPresentOrElse(
                                entity -> journalEntry.ifPresentOrElse(_ -> {
//                                        new InTransactionWorker(
//                                                session -> session.remove(session.merge(entity)),
//                                                this::loadData,
//                                                AccountingEntryView.this::showError
//                                        ).execute(),
                                }, () -> {
                                    records.removeIf(r -> r.getReference().equals(entity.reference()));
                                    loadData();
                                }),
                                () -> {
                                    throw new InvalidFieldException("Seleccione un elemento de la tabla");
                                });
            } catch (InvalidFieldException e) {
                showWarning(e);
            }
        }


        public void save() {
            try {
                journalEntry.ifPresentOrElse(_ -> {
                    //                new InTransactionWorker(
//                        session -> session.persist(new AccountSubtype(dto.number(), dto.name(), dto.type(), dto.username())),
//                        this::loadData,
//                        AccountingEntryView.this::showError
//                ).execute();
                }, () -> {
                    var dto = getDataFromForm();
                    if (journalEntry.isEmpty()) {
                        records.add(new LedgerRecord(dto.reference(), dto.account(), dto.debit(), dto.credit(), user.getUsername()));
                    }
                    loadData();
                });
            } catch (InvalidFieldException e) {
                showWarning(e);
            }
        }


        public void update() {
            try {
                var dto = getDataFromForm();
                tblRecord.getSelected()
                        .ifPresentOrElse(
                                accountSubtype ->
                                        journalEntry.ifPresentOrElse(_->{
//                                        new InTransactionWorker(
//                                                session -> {
//                                                    var entity = session.merge(accountSubtype);
//                                                    entity.setUpdatedBy(dto.username());
//                                                    entity.setNumber(dto.number());
//                                                    entity.setName(dto.name());
//                                                    entity.setType(dto.type());
//                                                },
//                                                this::loadData,
//                                                AccountingEntryView.this::showError
//                                        ).execute()
                                        },()->{
                                            records.removeIf(r -> r.getReference().equals(accountSubtype.reference()));
                                            records.add(new LedgerRecord(dto.reference(), dto.account(), dto.debit(), dto.credit(), user.getUsername()));
                                            loadData();
                                        }),
                                () -> {
                                    throw new InvalidFieldException("Seleccione un elemento de la tabla");
                                }
                        );
            } catch (InvalidFieldException e) {
                showWarning(e);
            }
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
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgRecordType = new javax.swing.ButtonGroup();
        pnlAside = new javax.swing.JPanel();
        pnlRecordForm = new javax.swing.JPanel();
        lblRecordAmount = new javax.swing.JLabel();
        lblRecordAccount = new javax.swing.JLabel();
        btnRSave = new javax.swing.JButton();
        cbxRAccount = new javax.swing.JComboBox<>();
        btnRUpdate = new javax.swing.JButton();
        lblSave = new javax.swing.JLabel();
        lblUpdate = new javax.swing.JLabel();
        txtRReference = new javax.swing.JTextField();
        lblRecordReference = new javax.swing.JLabel();
        lblRecordType = new javax.swing.JLabel();
        rbtRDebit = new javax.swing.JRadioButton();
        rbtRCredit = new javax.swing.JRadioButton();
        sepaSection1 = new javax.swing.JSeparator();
        labelSection1 = new javax.swing.JLabel();
        spnRAmount = new javax.swing.JSpinner(recordController.spnModelRAmount);
        spnRAccountNumber = new javax.swing.JSpinner(recordController.spnModelRAccountNumber);
        ApRecord = new com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel();
        OpRecord = new com.nutrehogar.sistemacontable.ui_2.component.OperationPanel();
        pnlEntryForm = new javax.swing.JPanel();
        lblEntryName = new javax.swing.JLabel();
        txtJName = new javax.swing.JTextField();
        lblConcept = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taJConcept = new javax.swing.JTextArea();
        lblEntryDocumentNumber = new javax.swing.JLabel();
        txtJDoctNumber = new javax.swing.JTextField();
        lblEntryDate = new javax.swing.JLabel();
        txtJCheckNumber = new javax.swing.JTextField();
        lblEntryCeckNumber = new javax.swing.JLabel();
        spnJDate = new com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner();
        btnJSave = new javax.swing.JButton();
        btnJUpdate = new javax.swing.JButton();
        btnJDelete = new javax.swing.JButton();
        btnJAdd = new javax.swing.JButton();
        cbxJDoctType = new javax.swing.JComboBox<>();
        pnlSourceDocuments = new javax.swing.JPanel();
        btnGeneratePaymentVoucher = new javax.swing.JButton();
        btnGenerateRegistrationForm = new javax.swing.JButton();
        lblCreateBy = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblCreateAt = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblUpdateBy = new javax.swing.JLabel();
        lblUpdateAt = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblVersion = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblRecord = new com.nutrehogar.sistemacontable.ui_2.builder.CustomTable(recordController.tblModelRecord);
        lblTitle = new javax.swing.JLabel();

        setOpaque(false);

        pnlAside.setOpaque(false);

        pnlRecordForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Formulario de Registros"));
        pnlRecordForm.setOpaque(false);

        lblRecordAmount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRecordAmount.setText("Monto:");
        lblRecordAmount.setName(""); // NOI18N

        lblRecordAccount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRecordAccount.setLabelFor(cbxRAccount);
        lblRecordAccount.setText("Cuenta:");

        btnRSave.setText("Guardar");

        cbxRAccount.setModel(recordController.cbxModelRAccount);

        btnRUpdate.setText("Actualizar");

        lblSave.setLabelFor(btnRSave);
        lblSave.setText("<html><p>Agrega el nuevo registro a la entrada</p></html>");
        lblSave.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblSave.setPreferredSize(new java.awt.Dimension(250, 40));

        lblUpdate.setLabelFor(btnRUpdate);
        lblUpdate.setText("<html><p>Actualiza los datos del registro editado</p></html>");
        lblUpdate.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblUpdate.setPreferredSize(new java.awt.Dimension(250, 40));

        lblRecordReference.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRecordReference.setLabelFor(txtRReference);
        lblRecordReference.setText("Referencia:");

        lblRecordType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRecordType.setLabelFor(rbtRDebit);
        lblRecordType.setText("Tipo de registro:");

        bgRecordType.add(rbtRDebit);
        rbtRDebit.setSelected(true);
        rbtRDebit.setText("Débito");

        bgRecordType.add(rbtRCredit);
        rbtRCredit.setText("Crédito");

        labelSection1.setText("Operaciones");

        javax.swing.GroupLayout pnlRecordFormLayout = new javax.swing.GroupLayout(pnlRecordForm);
        pnlRecordForm.setLayout(pnlRecordFormLayout);
        pnlRecordFormLayout.setHorizontalGroup(
            pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecordFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRecordFormLayout.createSequentialGroup()
                        .addComponent(lblUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRecordFormLayout.createSequentialGroup()
                        .addComponent(lblSave, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlRecordFormLayout.createSequentialGroup()
                        .addComponent(labelSection1)
                        .addGap(18, 18, 18)
                        .addComponent(sepaSection1))
                    .addGroup(pnlRecordFormLayout.createSequentialGroup()
                        .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblRecordAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRecordAccount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRecordReference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRecordType, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlRecordFormLayout.createSequentialGroup()
                                .addComponent(rbtRDebit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbtRCredit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(123, 123, 123))
                            .addGroup(pnlRecordFormLayout.createSequentialGroup()
                                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtRReference)
                                    .addGroup(pnlRecordFormLayout.createSequentialGroup()
                                        .addComponent(spnRAccountNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbxRAccount, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(spnRAmount))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        pnlRecordFormLayout.setVerticalGroup(
            pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecordFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRecordReference))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecordAccount)
                    .addComponent(cbxRAccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnRAccountNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecordType)
                    .addComponent(rbtRDebit)
                    .addComponent(rbtRCredit))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecordAmount)
                    .addComponent(spnRAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelSection1)
                    .addComponent(sepaSection1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRSave)
                    .addComponent(lblSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRUpdate))
                .addContainerGap())
        );

        ApRecord.setBorder(javax.swing.BorderFactory.createTitledBorder("Auditoría"));
        ApRecord.setToolTipText("");
        ApRecord.setName(""); // NOI18N

        javax.swing.GroupLayout pnlAsideLayout = new javax.swing.GroupLayout(pnlAside);
        pnlAside.setLayout(pnlAsideLayout);
        pnlAsideLayout.setHorizontalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlRecordForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ApRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(OpRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAsideLayout.setVerticalGroup(
            pnlAsideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addComponent(OpRecord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlRecordForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ApRecord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlEntryForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Formulario de Entrada"));
        pnlEntryForm.setOpaque(false);

        lblEntryName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEntryName.setLabelFor(txtJName);
        lblEntryName.setText("Nombre:");

        txtJName.setMaximumSize(new java.awt.Dimension(500, 500));

        lblConcept.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblConcept.setLabelFor(taJConcept);
        lblConcept.setText("Concepto:");

        taJConcept.setColumns(20);
        taJConcept.setLineWrap(true);
        taJConcept.setRows(3);
        taJConcept.setWrapStyleWord(true);
        taJConcept.setMaximumSize(new java.awt.Dimension(400, 400));
        jScrollPane2.setViewportView(taJConcept);

        lblEntryDocumentNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEntryDocumentNumber.setLabelFor(txtJDoctNumber);
        lblEntryDocumentNumber.setText("No Documento:");

        lblEntryDate.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEntryDate.setLabelFor(spnJDate);
        lblEntryDate.setText("Fecha:");

        lblEntryCeckNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEntryCeckNumber.setLabelFor(txtJCheckNumber);
        lblEntryCeckNumber.setText("No Cheque:");

        btnJSave.setText("Guardar");
        btnJSave.setToolTipText("");
        btnJSave.setMaximumSize(new java.awt.Dimension(73, 23));
        btnJSave.setMinimumSize(new java.awt.Dimension(73, 23));
        btnJSave.setPreferredSize(new java.awt.Dimension(73, 23));

        btnJUpdate.setText("Actualizar");
        btnJUpdate.setMaximumSize(new java.awt.Dimension(73, 23));
        btnJUpdate.setMinimumSize(new java.awt.Dimension(73, 23));
        btnJUpdate.setPreferredSize(new java.awt.Dimension(73, 23));

        btnJDelete.setText("Eliminar");

        btnJAdd.setText("Nuevo");

        cbxJDoctType.setModel(new javax.swing.DefaultComboBoxModel<>());
        cbxJDoctType.setToolTipText("Tipo de Documento");
        cbxJDoctType.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout pnlEntryFormLayout = new javax.swing.GroupLayout(pnlEntryForm);
        pnlEntryForm.setLayout(pnlEntryFormLayout);
        pnlEntryFormLayout.setHorizontalGroup(
            pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEntryFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEntryName, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEntryCeckNumber)
                    .addComponent(lblConcept, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlEntryFormLayout.createSequentialGroup()
                                .addComponent(jScrollPane2)
                                .addGap(12, 12, 12))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEntryFormLayout.createSequentialGroup()
                                .addComponent(lblEntryDate, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnJDate, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnJUpdate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnJSave, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnJAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(txtJName, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEntryDocumentNumber)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtJDoctNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxJDoctType, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnJDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(txtJCheckNumber)
                        .addGap(333, 333, 333)))
                .addContainerGap())
        );
        pnlEntryFormLayout.setVerticalGroup(
            pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEntryFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblEntryName)
                            .addComponent(txtJName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblEntryCeckNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtJCheckNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEntryDate)
                            .addComponent(spnJDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnJDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblEntryDocumentNumber)
                                .addComponent(txtJDoctNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cbxJDoctType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnJUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(btnJAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnJSave, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblConcept)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cbxJDoctType.getAccessibleContext().setAccessibleName("Tipo de Documento");

        pnlSourceDocuments.setBorder(javax.swing.BorderFactory.createTitledBorder("Documentos Exportables"));
        pnlSourceDocuments.setOpaque(false);

        btnGeneratePaymentVoucher.setText("Comprobante");

        btnGenerateRegistrationForm.setText("Formulario");

        lblCreateBy.setText("N/A");

        jLabel1.setText("Creado por: ");

        jLabel2.setText("Creacion: ");

        lblCreateAt.setText("N/A");

        jLabel4.setText("Actualizado por: ");

        jLabel3.setText("Actualizacion:");

        lblUpdateBy.setText("N/A");

        lblUpdateAt.setText("N/A");

        jLabel5.setText("Version: ");

        lblVersion.setText("N/A");

        javax.swing.GroupLayout pnlSourceDocumentsLayout = new javax.swing.GroupLayout(pnlSourceDocuments);
        pnlSourceDocuments.setLayout(pnlSourceDocumentsLayout);
        pnlSourceDocumentsLayout.setHorizontalGroup(
            pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSourceDocumentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnGeneratePaymentVoucher, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGenerateRegistrationForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCreateBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCreateAt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUpdateAt)
                    .addGroup(pnlSourceDocumentsLayout.createSequentialGroup()
                        .addComponent(lblUpdateBy)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblVersion)))
                .addContainerGap(87, Short.MAX_VALUE))
        );
        pnlSourceDocumentsLayout.setVerticalGroup(
            pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSourceDocumentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(lblVersion))
                    .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnGeneratePaymentVoucher)
                        .addComponent(jLabel1)
                        .addComponent(lblCreateBy)
                        .addComponent(jLabel4)
                        .addComponent(lblUpdateBy)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGenerateRegistrationForm)
                    .addComponent(jLabel2)
                    .addComponent(lblCreateAt)
                    .addComponent(jLabel3)
                    .addComponent(lblUpdateAt))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane3.setViewportView(tblRecord);

        lblTitle.setFont(lblTitle.getFont().deriveFont((float)30));
        lblTitle.setForeground(Theme.Palette.OFFICE_GREEN);
        lblTitle.setIcon(Theme.SVGs.FORM.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(LabelBuilder.build("Subtipos de Cuentas"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlEntryForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSourceDocuments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAside, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlEntryForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSourceDocuments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel ApRecord;
    private com.nutrehogar.sistemacontable.ui_2.component.OperationPanel OpRecord;
    private javax.swing.ButtonGroup bgRecordType;
    private javax.swing.JButton btnGeneratePaymentVoucher;
    private javax.swing.JButton btnGenerateRegistrationForm;
    private javax.swing.JButton btnJAdd;
    private javax.swing.JButton btnJDelete;
    private javax.swing.JButton btnJSave;
    private javax.swing.JButton btnJUpdate;
    private javax.swing.JButton btnRSave;
    private javax.swing.JButton btnRUpdate;
    private javax.swing.JComboBox<DocumentType> cbxJDoctType;
    private javax.swing.JComboBox<Account> cbxRAccount;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel labelSection1;
    private javax.swing.JLabel lblConcept;
    private javax.swing.JLabel lblCreateAt;
    private javax.swing.JLabel lblCreateBy;
    private javax.swing.JLabel lblEntryCeckNumber;
    private javax.swing.JLabel lblEntryDate;
    private javax.swing.JLabel lblEntryDocumentNumber;
    private javax.swing.JLabel lblEntryName;
    private javax.swing.JLabel lblRecordAccount;
    private javax.swing.JLabel lblRecordAmount;
    private javax.swing.JLabel lblRecordReference;
    private javax.swing.JLabel lblRecordType;
    private javax.swing.JLabel lblSave;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUpdate;
    private javax.swing.JLabel lblUpdateAt;
    private javax.swing.JLabel lblUpdateBy;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel pnlAside;
    private javax.swing.JPanel pnlEntryForm;
    private javax.swing.JPanel pnlRecordForm;
    private javax.swing.JPanel pnlSourceDocuments;
    private javax.swing.JRadioButton rbtRCredit;
    private javax.swing.JRadioButton rbtRDebit;
    private javax.swing.JSeparator sepaSection1;
    private com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner spnJDate;
    private javax.swing.JSpinner spnRAccountNumber;
    private javax.swing.JSpinner spnRAmount;
    private javax.swing.JTextArea taJConcept;
    private com.nutrehogar.sistemacontable.ui_2.builder.CustomTable<RecordTableData> tblRecord;
    private javax.swing.JTextField txtJCheckNumber;
    private javax.swing.JTextField txtJDoctNumber;
    private javax.swing.JTextField txtJName;
    private javax.swing.JTextField txtRReference;
    // End of variables declaration//GEN-END:variables
}