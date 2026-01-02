package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.exception.InvalidFieldException;
import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.query.*;
import com.nutrehogar.sistemacontable.service.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.service.worker.InTransactionWorker;
import com.nutrehogar.sistemacontable.ui.View;

import com.nutrehogar.sistemacontable.ui_2.builder.*;
import com.nutrehogar.sistemacontable.ui_2.component.AuditablePanel;
import com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner;
import com.nutrehogar.sistemacontable.ui_2.component.OperationPanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nutrehogar.sistemacontable.config.Util.AUDITABLE_DATE_FORMATTER;
import static com.nutrehogar.sistemacontable.config.Util.NA;
import static com.nutrehogar.sistemacontable.config.Util.toStringSafe;
import static java.math.MathContext.DECIMAL128;

@Slf4j
@Getter
public class AccountingEntryView extends View{
    @NotNull
    protected final RecordController recordController;
    /**
     * Si esta vacio es que eta en modo crear, si esta es modo editar
     */
    @NotNull
    protected Optional<JournalEntry> journalEntry = Optional.empty();

    @NotNull
    public final String entityName;

    @NotNull
    private final SpinnerNumberModel spnModelJDocNumber;
    @NotNull
    private final LocalDateSpinnerModel spnModelJDate;
    @NotNull
    private final CustomComboBoxModel<DocumentType> cbxModelJDocType;

    public AccountingEntryView(@NotNull User user, @NotNull Optional<Long> journalId) {
        super(user);
        this.entityName = "Entrada de Diario";
        this.spnModelJDate = new LocalDateSpinnerModel();
        this.cbxModelJDocType = new CustomComboBoxModel<>(DocumentType.values());
        this.spnModelJDocNumber = new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1);
        recordController = new RecordController();
        initComponents();
        recordController.init();
        txtJName.putClientProperty("JTextField.placeholderText", "Ventas S.A.");
        taJConcept.putClientProperty("JTextArea.placeholderText", "Cancelación de factura al crédito");
        txtJCheckNumber.putClientProperty("JTextField.placeholderText", "4987");
        cbxJDocType.addActionListener(_->new FromTransactionWorker<>(
                session -> new JournalEntryQuery_(session).findNextDocNumberByType(cbxModelJDocType.getSelectedItem()),
                spnModelJDocNumber::setValue,
                this::showError
        ).execute());
        cbxJDocType.setRenderer(new CustomListCellRenderer());
        txtRReference.addActionListener(_ -> btnRSave.doClick());
        btnJSave.addActionListener(_ -> save());
        btnJAdd.addActionListener(_ -> prepareToAdd());
        btnJDelete.addActionListener(_ -> delete());
        journalId.ifPresentOrElse(this::edit, this::prepareToAdd);
    }

    public @NotNull JournalFormData getDataFromForm() throws InvalidFieldException {
        var name = txtJName.getText();
        if (name == null || name.isBlank()) throw new InvalidFieldException("El nombre no puede estar vacío");
        var concept = taJConcept.getText();
        if (concept == null || concept.isBlank()) throw new InvalidFieldException("El concepto no puede estar vacío");
        var checkNumber = txtJCheckNumber.getText();
        if (checkNumber == null) throw new InvalidFieldException("El numero del documento no puede estar vacío");
        var type = cbxModelJDocType.getSelectedItem();
        if (type == null)
            throw new InvalidFieldException("El tipo de documento no puede estar vacío");

        return new JournalFormData(
                spnModelJDocNumber.getNumber().intValue(),
                type,
                name,
                concept,
                checkNumber,
                spnJDate.getValue(),
                user.getUsername()
        );
    }

    public void edit(@NotNull Long journalEntryId) {
        new FromTransactionWorker<>(
                session -> new JournalEntryQuery_(session).findAndPeriodById(journalEntryId),
                journal -> {
                    if(journal.isEmpty()){
                        showError(new ApplicationException("No se encontró el Documento"));
                        prepareToAdd();
                        return;
                    }
                    journalEntry = journal;
                    prepareToEdit(journal.get());
                },
                this::showError
        ).execute();
    }

    public void prepareToAdd() {
        journalEntry = Optional.empty();
        btnJSave.setEnabled(true);
        btnJUpdate.setEnabled(false);
        btnJDelete.setEnabled(false);
        taJConcept.setText("");
        txtJCheckNumber.setText("");
        txtJName.setText("");
        spnJDate.setValue(LocalDate.now());
        cbxJDocType.setSelectedItem(DocumentType.INCOME);

        lblCreateAt.setText(NA);
        lblUpdateAt.setText(NA);
        lblCreateBy.setText(NA);
        lblUpdateBy.setText(NA);
        lblVersion.setText(NA);
        recordController.clear();
    }

    public void prepareToEdit(@NotNull JournalEntry journalEntry) {
        btnJSave.setEnabled(false);
        btnJUpdate.setEnabled(true);
        btnJDelete.setEnabled(true);

        spnModelJDate.setValue(journalEntry.getDate());
        txtJName.setText(journalEntry.getName());
        txtJCheckNumber.setText(journalEntry.getCheckNumber());
        spnModelJDocNumber.setValue(journalEntry.getNumber());
        taJConcept.setText(journalEntry.getConcept());
        cbxModelJDocType.setSelectedItem(journalEntry.getType());

        lblCreateAt.setText(toStringSafe(journalEntry.getCreatedAt(),date->date.format(AUDITABLE_DATE_FORMATTER),NA));
        lblUpdateAt.setText(toStringSafe(journalEntry.getUpdatedAt(),date->date.format(AUDITABLE_DATE_FORMATTER),NA));
        lblCreateBy.setText(toStringSafe(journalEntry.getCreatedBy(),NA));
        lblUpdateBy.setText(toStringSafe(journalEntry.getUpdatedBy(),NA));
        lblVersion.setText(toStringSafe(journalEntry.getVersion(),NA));

        recordController.clear();
    }

    public void delete() {
        if (journalEntry.isEmpty()) {
            showWarning("Debe de estar editando un documento para poder eliminarlo");
            return;
        }
        var id = journalEntry.get().getId();
        new InTransactionWorker(
                session -> new JournalEntryQuery_(session).findById(id).ifPresent(session::remove),
                this::prepareToAdd,
                this::showError
        ).execute();
    }


    public void save() {
        if (journalEntry.isPresent()) {
            showError(new ApplicationException("No se pudo guardar el documento cuando se esta editando"));
            return;
        }
        if (recordController
                .formRecords.isEmpty() || recordController.formRecords.size() <3) {
            showWarning("El documento no tiene suficientes registros, al menos debe tener dos");
            return;
        }
        if (recordController.isBalanced) {
            showWarning("El documento no esta balanceado");
            return;
        }
        var records = recordController
                .formRecords
                .stream()
                .map(r ->
                        new LedgerRecord(
                                r.reference(),
                                r.account(),
                                r.debit(),
                                r.credit(),
                                user.getUsername()
                        ))
                .collect(Collectors.toSet());
        try {
            var dto = getDataFromForm();
            new InTransactionWorker(
                    session -> {
                        var queries = new JournalEntryQuery_(session);
                        var period = new AccountingPeriodQuery_(session).findByNumber(1);

                        if (period.isEmpty()) throw new InvalidFieldException("El periodo no existe");

                        if (queries.existByDocNumAndType(dto.type(), dto.number()))
                            throw new InvalidFieldException("El documento: " + dto.type().getName() + " No." + dto.number() + ", ya existe");

                        var entry = new JournalEntry(
                                dto.number(),
                                dto.type(),
                                dto.name(),
                                dto.concept(),
                                dto.checkNumber(),
                                dto.date(),
                                dto.user()
                        );

                        for (var ledgerRecord : records)
                            ledgerRecord.setEntry(entry);

                        entry.setRecords(records);
                        entry.setPeriod(period.get());

                        session.persist(entry);
                    },
                    ()->{
                        showMessage("Documento " + dto.type().getName() + " No. " + dto.number() + ", creado exitosamente");
                        prepareToAdd();
                    },
                    this::showError
            ).execute();
        } catch (InvalidFieldException e) {
            showWarning(e);
        }
    }


    public void update() {

    }

    private final class RecordController {
        @NotNull
        public final CustomTableModel<RecordTableRow> tblModelRecord;
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
        public boolean isBalanced;

        public RecordController() {
            this.entityName = "Registro";
            this.cbxModelRAccount = new CustomComboBoxModel<>();
            this.accounts = new ArrayList<>();
            this.formRecords = new ArrayList<>();
            this.isBalanced = false;
            this.spnModelRAccountNumber = new SpinnerNumberModel(0, 0, 99999, 1);
            this.spnModelRAmount = new SpinnerNumberModel(1.0d, 0.0d, Integer.MAX_VALUE, 1.0d);
            this.tblModelRecord = new CustomTableModel<>("Referencia", "Cuenta", "Débito", "Crédito", "Subtotal") {
                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return switch (data.get(rowIndex)) {
                        case RecordTableData r -> switch (columnIndex) {
                            case 0 -> r.reference();
                            case 1 -> r.account();
                            case 2 -> r.debit();
                            case 3 -> r.credit();
                            case 4 -> r.total();
                            default -> "que haces?";
                        };
                        case RecordTableTotal(var total) -> switch (columnIndex) {
                            case 0 -> "Total";
                            case 4 -> total;
                            default -> "";
                        };
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
        public void clear(){
            formRecords.clear();
            loadData();
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
                            this::setDataToTable,
                            AccountingEntryView.this::showError
                    ).execute(),
                    () -> setDataToTable(List.of())
            );
        }

        public void setDataToTable(@NotNull List<LedgerRecord> records) {
            var debitSum = BigDecimal.ZERO;
            var creditSum = BigDecimal.ZERO;
            var totalSum = BigDecimal.ZERO;
            var dtos = new ArrayList<RecordTableRow>(records.size() + formRecords.size() + 1);
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
                        new RecordTableEntity(
                                record,
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
                        new RecordTableFormData(
                                record,
                                totalSum
                        )
                );
            }
            dtos.add(new RecordTableTotal(totalSum));
            isBalanced = totalSum.equals(BigDecimal.ZERO);
            tblModelRecord.setData(dtos);
        }

        public @NotNull RecordFormData getDataFromForm() throws InvalidFieldException {
            var reference = txtRReference.getText();
            if( reference == null || reference.isBlank() ) throw new InvalidFieldException("La referencia no puede estar vacía");
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
            if(tblRecord.getSelected().isEmpty() || tblRecord.getSelected().get() instanceof RecordTableTotal){
                showWarning("Seleccione un elemento de la tabla");
                return;
            }
            btnRSave.setEnabled(false);
            btnRUpdate.setEnabled(true);
        }


        public void onSelected(@NotNull RecordTableRow row) {
            if (row instanceof RecordTableTotal) {
                tblRecord.setEmpty();
                return;
            }
            if (row instanceof RecordTableEntity entity) {
                ApRecord.setAuditableFields(entity.entity());
            }
            OpRecord.getBtnDelete().setEnabled(true);
            OpRecord.getBtnPrepareToEdit().setEnabled(true);
        }


        public void onDeselected() {
            ApRecord.clear();
            OpRecord.getBtnDelete().setEnabled(false);
            OpRecord.getBtnPrepareToEdit().setEnabled(false);
        }


        public void delete() {
            if(tblRecord.getSelected().isEmpty()){
                showWarning("Seleccione un elemento de la tabla");
                return;
            }
            switch (tblRecord.getSelected().get()) {
                case RecordTableEntity(var entity, var _) ->
                        new InTransactionWorker(
                                session -> session.remove(session.merge(entity)),
                                this::loadData,
                                AccountingEntryView.this::showError
                        ).execute();
                case RecordTableFormData(var formData, var _) -> {
                    formRecords.removeIf(formData::equals);
                    loadData();
                }
                case RecordTableTotal _ -> showWarning("Seleccione un elemento de la tabla");
            }
        }

        public void save() {
            try {
                var dto = getDataFromForm();
                if (journalEntry.isPresent()) {
                    new InTransactionWorker(session -> session.persist(new LedgerRecord(journalEntry.get(), dto.reference(), dto.account(), dto.debit(), dto.credit(), user.getUsername())), this::loadData, AccountingEntryView.this::showError).execute();
                    return;
                }
                formRecords.add(new RecordFormData(dto.reference(), dto.account(), dto.debit(), dto.credit(), user.getUsername()));
                loadData();
            } catch (InvalidFieldException e) {
                showWarning(e);
            }
        }


        public void update() {
            if(tblRecord.getSelected().isEmpty()){
                showWarning("Seleccione un elemento de la tabla");
                return;
            }
            try {
                var dto = getDataFromForm();
                switch (tblRecord.getSelected().get()) {
                    case RecordTableEntity(var entity, var _) ->
                            new InTransactionWorker(
                                    session -> {
                                        var tomerge = session.merge(entity);
                                        tomerge.setUpdatedBy(dto.username());
                                        tomerge.setReference(dto.reference());
                                        tomerge.setAccount(dto.account());
                                        tomerge.setDebit(dto.debit());
                                        tomerge.setCredit(dto.credit());
                                    },
                                    this::loadData,
                                    AccountingEntryView.this::showError
                            ).execute();
                    case RecordTableFormData(var formData, var _) -> {
                        formRecords.removeIf(formData::equals);
                        formRecords.add(new RecordFormData(dto.reference(), dto.account(), dto.debit(), dto.credit(), user.getUsername()));
                        loadData();
                    }
                    case RecordTableTotal _ -> showWarning("Seleccione un elemento de la tabla");
                }
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

        bgRecordType = new ButtonGroup();
        pnlAside = new JPanel();
        pnlRecordForm = new JPanel();
        lblRecordAmount = new JLabel();
        lblRecordAccount = new JLabel();
        btnRSave = new JButton();
        cbxRAccount = new JComboBox<>();
        btnRUpdate = new JButton();
        lblSave = new JLabel();
        lblUpdate = new JLabel();
        txtRReference = new JTextField();
        lblRecordReference = new JLabel();
        lblRecordType = new JLabel();
        rbtRDebit = new JRadioButton();
        rbtRCredit = new JRadioButton();
        sepaSection1 = new JSeparator();
        labelSection1 = new JLabel();
        spnRAmount = new JSpinner(recordController.spnModelRAmount);
        spnRAccountNumber = new JSpinner(recordController.spnModelRAccountNumber);
        ApRecord = new AuditablePanel();
        OpRecord = new OperationPanel(recordController.entityName);
        pnlEntryForm = new JPanel();
        lblEntryName = new JLabel();
        txtJName = new JTextField();
        lblConcept = new JLabel();
        jScrollPane2 = new JScrollPane();
        taJConcept = new JTextArea();
        lblEntryDocumentNumber = new JLabel();
        lblEntryDate = new JLabel();
        txtJCheckNumber = new JTextField();
        lblEntryCeckNumber = new JLabel();
        spnJDate = new LocalDateSpinner(spnModelJDate);
        btnJSave = new JButton();
        btnJUpdate = new JButton();
        btnJDelete = new JButton();
        btnJAdd = new JButton();
        cbxJDocType = new JComboBox<>(cbxModelJDocType);
        spnJDocNumber = new JSpinner(spnModelJDocNumber);
        pnlSourceDocuments = new JPanel();
        btnGeneratePaymentVoucher = new JButton();
        btnGenerateRegistrationForm = new JButton();
        lblCreateBy = new JLabel();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        lblCreateAt = new JLabel();
        jLabel4 = new JLabel();
        jLabel3 = new JLabel();
        lblUpdateBy = new JLabel();
        lblUpdateAt = new JLabel();
        jLabel5 = new JLabel();
        lblVersion = new JLabel();
        jScrollPane3 = new JScrollPane();
        tblRecord = new CustomTable(recordController.tblModelRecord);
        lblTitle = new JLabel();

        setOpaque(false);

        pnlAside.setOpaque(false);

        pnlRecordForm.setBorder(BorderFactory.createTitledBorder("Formulario de Registros"));
        pnlRecordForm.setOpaque(false);

        lblRecordAmount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblRecordAmount.setText("Monto:");
        lblRecordAmount.setName(""); // NOI18N

        lblRecordAccount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblRecordAccount.setLabelFor(cbxRAccount);
        lblRecordAccount.setText("Cuenta:");

        btnRSave.setText("Guardar");

        cbxRAccount.setModel(recordController.cbxModelRAccount);

        btnRUpdate.setText("Actualizar");

        lblSave.setLabelFor(btnRSave);
        lblSave.setText("<html><p>Agrega el nuevo registro a la entrada</p></html>");
        lblSave.setVerticalAlignment(SwingConstants.TOP);
        lblSave.setPreferredSize(new Dimension(250, 40));

        lblUpdate.setLabelFor(btnRUpdate);
        lblUpdate.setText("<html><p>Actualiza los datos del registro editado</p></html>");
        lblUpdate.setVerticalAlignment(SwingConstants.TOP);
        lblUpdate.setPreferredSize(new Dimension(250, 40));

        lblRecordReference.setHorizontalAlignment(SwingConstants.RIGHT);
        lblRecordReference.setLabelFor(txtRReference);
        lblRecordReference.setText("Referencia:");

        lblRecordType.setHorizontalAlignment(SwingConstants.RIGHT);
        lblRecordType.setLabelFor(rbtRDebit);
        lblRecordType.setText("Tipo de registro:");

        bgRecordType.add(rbtRDebit);
        rbtRDebit.setSelected(true);
        rbtRDebit.setText("Débito");

        bgRecordType.add(rbtRCredit);
        rbtRCredit.setText("Crédito");

        labelSection1.setText("Operaciones");

        GroupLayout pnlRecordFormLayout = new GroupLayout(pnlRecordForm);
        pnlRecordForm.setLayout(pnlRecordFormLayout);
        pnlRecordFormLayout.setHorizontalGroup(
            pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecordFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRecordFormLayout.createSequentialGroup()
                        .addComponent(lblUpdate, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRUpdate, GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.TRAILING, pnlRecordFormLayout.createSequentialGroup()
                        .addComponent(lblSave, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRSave, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlRecordFormLayout.createSequentialGroup()
                        .addComponent(labelSection1)
                        .addGap(18, 18, 18)
                        .addComponent(sepaSection1))
                    .addGroup(pnlRecordFormLayout.createSequentialGroup()
                        .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(lblRecordAmount, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRecordAccount, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRecordReference, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRecordType, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(pnlRecordFormLayout.createSequentialGroup()
                                .addComponent(rbtRDebit)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbtRCredit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(123, 123, 123))
                            .addGroup(pnlRecordFormLayout.createSequentialGroup()
                                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtRReference)
                                    .addGroup(pnlRecordFormLayout.createSequentialGroup()
                                        .addComponent(spnRAccountNumber, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbxRAccount, GroupLayout.PREFERRED_SIZE, 143, GroupLayout.PREFERRED_SIZE))
                                    .addComponent(spnRAmount))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        pnlRecordFormLayout.setVerticalGroup(
            pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecordFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRReference, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRecordReference))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecordAccount)
                    .addComponent(cbxRAccount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnRAccountNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecordType)
                    .addComponent(rbtRDebit)
                    .addComponent(rbtRCredit))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecordAmount)
                    .addComponent(spnRAmount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(labelSection1)
                    .addComponent(sepaSection1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRSave)
                    .addComponent(lblSave, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRecordFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRUpdate))
                .addContainerGap())
        );

        ApRecord.setBorder(BorderFactory.createTitledBorder("Auditoría"));
        ApRecord.setToolTipText("");
        ApRecord.setName(""); // NOI18N

        GroupLayout pnlAsideLayout = new GroupLayout(pnlAside);
        pnlAside.setLayout(pnlAsideLayout);
        pnlAsideLayout.setHorizontalGroup(
            pnlAsideLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAsideLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(pnlRecordForm, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ApRecord, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(OpRecord, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAsideLayout.setVerticalGroup(
            pnlAsideLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlAsideLayout.createSequentialGroup()
                .addComponent(OpRecord, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlRecordForm, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ApRecord, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlEntryForm.setBorder(BorderFactory.createTitledBorder("Formulario de Entrada"));
        pnlEntryForm.setOpaque(false);

        lblEntryName.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEntryName.setLabelFor(txtJName);
        lblEntryName.setText("Nombre:");

        txtJName.setMaximumSize(new Dimension(500, 500));

        lblConcept.setHorizontalAlignment(SwingConstants.RIGHT);
        lblConcept.setLabelFor(taJConcept);
        lblConcept.setText("Concepto:");

        taJConcept.setColumns(20);
        taJConcept.setLineWrap(true);
        taJConcept.setRows(3);
        taJConcept.setWrapStyleWord(true);
        taJConcept.setMaximumSize(new Dimension(400, 400));
        jScrollPane2.setViewportView(taJConcept);

        lblEntryDocumentNumber.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEntryDocumentNumber.setText("No Documento:");

        lblEntryDate.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEntryDate.setLabelFor(spnJDate);
        lblEntryDate.setText("Fecha:");

        lblEntryCeckNumber.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEntryCeckNumber.setLabelFor(txtJCheckNumber);
        lblEntryCeckNumber.setText("No Cheque:");

        btnJSave.setText("Guardar");
        btnJSave.setToolTipText("");
        btnJSave.setMaximumSize(new Dimension(82, 23));
        btnJSave.setMinimumSize(new Dimension(82, 23));
        btnJSave.setPreferredSize(new Dimension(82, 23));

        btnJUpdate.setText("Actualizar");
        btnJUpdate.setMaximumSize(new Dimension(73, 23));
        btnJUpdate.setMinimumSize(new Dimension(73, 23));
        btnJUpdate.setPreferredSize(new Dimension(73, 23));

        btnJDelete.setText("Eliminar");

        btnJAdd.setText("Nuevo");

        cbxJDocType.setToolTipText("Tipo de Documento");
        cbxJDocType.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        GroupLayout pnlEntryFormLayout = new GroupLayout(pnlEntryForm);
        pnlEntryForm.setLayout(pnlEntryFormLayout);
        pnlEntryFormLayout.setHorizontalGroup(
            pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlEntryFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblEntryCeckNumber)
                    .addComponent(lblConcept, GroupLayout.Alignment.TRAILING)
                    .addComponent(lblEntryName, GroupLayout.Alignment.TRAILING))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(btnJAdd, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnJSave, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(GroupLayout.Alignment.TRAILING, pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(txtJName, GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEntryDocumentNumber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnJDocNumber, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxJDocType, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnJDelete, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(txtJCheckNumber)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEntryDate)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnJDate, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnJUpdate, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlEntryFormLayout.setVerticalGroup(
            pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlEntryFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(btnJDelete)
                    .addGroup(pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEntryName)
                        .addComponent(txtJName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblEntryDocumentNumber)
                        .addComponent(cbxJDocType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(spnJDocNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEntryCeckNumber, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtJCheckNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEntryDate)
                    .addComponent(spnJDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(btnJAdd)
                        .addGap(13, 13, 13)
                        .addComponent(btnJSave, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblConcept))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cbxJDocType.getAccessibleContext().setAccessibleName("");

        pnlSourceDocuments.setBorder(BorderFactory.createTitledBorder("Documentos Exportables"));
        pnlSourceDocuments.setOpaque(false);

        btnGeneratePaymentVoucher.setText("Comprobante");

        btnGenerateRegistrationForm.setText("Formulario");

        lblCreateBy.setText("N/A");

        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setText("Creado por: ");

        jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel2.setText("Creacion: ");

        lblCreateAt.setText("N/A");

        jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel4.setText("Actualizado por: ");

        jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel3.setText("Actualizacion:");

        lblUpdateBy.setText("N/A");

        lblUpdateAt.setText("N/A");

        jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel5.setText("Version: ");

        lblVersion.setText("N/A");

        GroupLayout pnlSourceDocumentsLayout = new GroupLayout(pnlSourceDocuments);
        pnlSourceDocuments.setLayout(pnlSourceDocumentsLayout);
        pnlSourceDocumentsLayout.setHorizontalGroup(
            pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlSourceDocumentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnGeneratePaymentVoucher, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGenerateRegistrationForm, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblCreateBy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCreateAt, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblUpdateBy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUpdateAt, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblVersion, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(157, 157, 157))
        );
        pnlSourceDocumentsLayout.setVerticalGroup(
            pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(pnlSourceDocumentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGeneratePaymentVoucher)
                    .addComponent(jLabel1)
                    .addComponent(lblCreateBy)
                    .addComponent(jLabel4)
                    .addComponent(lblUpdateBy)
                    .addComponent(jLabel5)
                    .addComponent(lblVersion))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGenerateRegistrationForm)
                    .addComponent(jLabel2)
                    .addComponent(lblCreateAt)
                    .addComponent(jLabel3)
                    .addComponent(lblUpdateAt))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane3.setViewportView(tblRecord);

        lblTitle.setFont(lblTitle.getFont().deriveFont((float)30));
        lblTitle.setForeground(Theme.Palette.OFFICE_GREEN);
        lblTitle.setIcon(Theme.SVGs.FORM.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(LabelBuilder.build("Subtipos de Cuentas"));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(pnlEntryForm, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSourceDocuments, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3)
                    .addComponent(lblTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAside, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAside, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlEntryForm, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSourceDocuments, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private AuditablePanel ApRecord;
    private OperationPanel OpRecord;
    private ButtonGroup bgRecordType;
    private JButton btnGeneratePaymentVoucher;
    private JButton btnGenerateRegistrationForm;
    private JButton btnJAdd;
    private JButton btnJDelete;
    private JButton btnJSave;
    private JButton btnJUpdate;
    private JButton btnRSave;
    private JButton btnRUpdate;
    private JComboBox<DocumentType> cbxJDocType;
    private JComboBox<Account> cbxRAccount;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JLabel labelSection1;
    private JLabel lblConcept;
    private JLabel lblCreateAt;
    private JLabel lblCreateBy;
    private JLabel lblEntryCeckNumber;
    private JLabel lblEntryDate;
    private JLabel lblEntryDocumentNumber;
    private JLabel lblEntryName;
    private JLabel lblRecordAccount;
    private JLabel lblRecordAmount;
    private JLabel lblRecordReference;
    private JLabel lblRecordType;
    private JLabel lblSave;
    private JLabel lblTitle;
    private JLabel lblUpdate;
    private JLabel lblUpdateAt;
    private JLabel lblUpdateBy;
    private JLabel lblVersion;
    private JPanel pnlAside;
    private JPanel pnlEntryForm;
    private JPanel pnlRecordForm;
    private JPanel pnlSourceDocuments;
    private JRadioButton rbtRCredit;
    private JRadioButton rbtRDebit;
    private JSeparator sepaSection1;
    private LocalDateSpinner spnJDate;
    private JSpinner spnJDocNumber;
    private JSpinner spnRAccountNumber;
    private JSpinner spnRAmount;
    private JTextArea taJConcept;
    private CustomTable<RecordTableRow> tblRecord;
    private JTextField txtJCheckNumber;
    private JTextField txtJName;
    private JTextField txtRReference;
    // End of variables declaration//GEN-END:variables

}