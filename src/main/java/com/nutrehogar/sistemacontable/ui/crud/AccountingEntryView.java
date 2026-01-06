package com.nutrehogar.sistemacontable.ui.crud;

import com.nutrehogar.sistemacontable.config.LabelBuilder;
import com.nutrehogar.sistemacontable.config.Theme;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.exception.InvalidFieldException;
import com.nutrehogar.sistemacontable.model.*;
import com.nutrehogar.sistemacontable.query.*;
import com.nutrehogar.sistemacontable.report.EntryFormReport;
import com.nutrehogar.sistemacontable.report.EntryFormReportType;
import com.nutrehogar.sistemacontable.report.PaymentVoucherReport;
import com.nutrehogar.sistemacontable.report.dto.JournalEntryReportData;
import com.nutrehogar.sistemacontable.report.dto.LedgerRecordReportRow;
import com.nutrehogar.sistemacontable.ui_2.component.ReportResponseDialog;
import com.nutrehogar.sistemacontable.worker.FromTransactionWorker;
import com.nutrehogar.sistemacontable.worker.InTransactionWorker;
import com.nutrehogar.sistemacontable.ui.View;

import com.nutrehogar.sistemacontable.ui_2.builder.*;
import com.nutrehogar.sistemacontable.worker.ReportWorker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nutrehogar.sistemacontable.config.Util.*;
import com.nutrehogar.sistemacontable.ui.UIEntityInfo;
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
    @NotNull
    private final CustomComboBoxModel<AccountingPeriod> cbxModelPeriod;

    public AccountingEntryView(@NotNull User user) {
        super(user);
        this.entityName = "Entrada de Diario";
        this.spnModelJDate = new LocalDateSpinnerModel();
        this.cbxModelPeriod = new CustomComboBoxModel<>();
        this.cbxModelJDocType = new CustomComboBoxModel<>(DocumentType.values());
        this.spnModelJDocNumber = new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1);
        recordController = new RecordController();
        initComponents();
        recordController.init();
        txtJName.putClientProperty("JTextField.placeholderText", "Ventas S.A.");
        taJConcept.putClientProperty("JTextArea.placeholderText", "Cancelación de factura al crédito");
        txtJCheckNumber.putClientProperty("JTextField.placeholderText", "4987");
        cbxPeriod.addActionListener(_ -> {
            var period = cbxModelPeriod.getSelectedItem();
            var date = spnModelJDate.getValue();
            if(period == null){
                return;
            }
            spnModelJDate.setValue(LocalDate.of(period.getYear(), date.getMonth(), date.getDayOfMonth()));
            spnModelJDate.setMinDate(period.getStartDate());
            spnModelJDate.setMaxDate(period.getEndDate());
            findNexNumber();
        });
        cbxJDocType.addActionListener(_-> findNexNumber());
        cbxJDocType.setRenderer(new CustomListCellRenderer());
        txtRReference.addActionListener(_ -> btnRSave.doClick());
        btnJSave.addActionListener(_ -> save());
        btnJAdd.addActionListener(_ -> {
            IO.println(user.getUsername());
            prepareToAdd();
        });
        btnJDelete.addActionListener(_ -> delete());
        btnGeneratePaymentVoucher.addActionListener(_ -> generateReport(EntryFormReportType.PAYMENT_VOUCHER));
        btnGenerateRegistrationForm.addActionListener(_ -> generateReport(EntryFormReportType.REGISTRATION_FORM));
        prepareToAdd();
    }
    private void generateReport(@NotNull EntryFormReportType type) {
        {
            if(journalEntry.isEmpty())
                return;

            btnGeneratePaymentVoucher.setEnabled(false);
            btnGenerateRegistrationForm.setEnabled(false);
            showLoadingCursor();

            var jr = journalEntry.get();
            List<RecordTableEntity> tableEntities = new ArrayList<>();
            for(var r : recordController.tblModelRecord.getData())
                if(r instanceof RecordTableEntity re)
                    tableEntities.add(re);

            var amount = recordController.tblModelRecord.getData().getLast().debit();
            new ReportWorker(
                    () -> {
                        StringBuilder checkNumber = new StringBuilder();
                        for (var check : jr.getCheckNumber().split(";")) {
                            checkNumber.append(check.trim());
                            checkNumber.append("\n");
                        }

                        var dto = new JournalEntryReportData(
                                jr.getFormattedNaturalId(),
                                checkNumber.toString(),
                                jr.getDate(),
                                jr.getName(),
                                jr.getConcept(),
                                formatDecimalSafe(amount),
                                tableEntities.stream()
                                        .map(r ->
                                                new LedgerRecordReportRow(
                                                        r.account().getName(),
                                                        r.reference(),
                                                        formatDecimalSafe(r.debit()),
                                                        formatDecimalSafe(r.credit())
                                                )
                                        )
                                        .toList()
                        );
                        return type.generate(user, dto);
                    },
                    path -> {
                        hideLoadingCursor();
                        btnGeneratePaymentVoucher.setEnabled(true);
                        btnGenerateRegistrationForm.setEnabled(true);
                        ReportResponseDialog.showMessage(this, path);
                    },
                    this::showError
            ).execute();
        }
    }

    public void findNexNumber() {
        var type = cbxModelJDocType.getSelectedItem();
        var period = cbxModelPeriod.getSelectedItem();
        new FromTransactionWorker<>(
                session -> new JournalEntryQuery_(session).findNextNumByTypeAndPeriod(type, period),
                spnModelJDocNumber::setValue,
                this::showError
        ).execute();
    }
    public void findPeriods(){
        new FromTransactionWorker<>(
                session -> new AccountingPeriodQuery_(session).findAllOpen(),
                periods -> {
                    if (periods.isEmpty()) {
                        showWarning("No hay periodos disponibles, antes de continuar debe crear al menos uno");
                        return;
                    }
                    cbxModelPeriod.setData(periods);
                    var thisYear = LocalDate.now().getYear();

                    for(var p: periods)
                        if(p.getYear() == thisYear)
                            cbxModelPeriod.setSelectedItem(p);
                },
                this::showError
        ).execute();
    }
    public @NotNull JournalFormData getDataFromForm() throws InvalidFieldException {
        var name = txtJName.getText();
        if (name == null || name.isBlank())
            throw new InvalidFieldException("El nombre no puede estar vacío");

        var concept = taJConcept.getText();
        if (concept == null || concept.isBlank())
            throw new InvalidFieldException("El concepto no puede estar vacío");

        var checkNumber = txtJCheckNumber.getText();
        if (checkNumber == null)
            throw new InvalidFieldException("El numero del documento no puede estar vacío");

        var type = cbxModelJDocType.getSelectedItem();
        if (type == null)
            throw new InvalidFieldException("El tipo de documento no puede estar vacío");

        var period = cbxModelPeriod.getSelectedItem();
        if (period == null)

            throw new InvalidFieldException("El periodo es requerido");
        return new JournalFormData(
                spnModelJDocNumber.getNumber().intValue(),
                type,
                name,
                concept,
                checkNumber,
                spnJDate.getValue(),
                period,
                user.getUsername()
        );
    }
    public void edit(Long journalEntryId) {
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
        findPeriods();
        recordController.clear();
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
        if (journalEntry.isEmpty() || journalEntry.get().getId() == null) {
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
                .formRecords.isEmpty() || recordController.formRecords.size() <2) {
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
                .collect(Collectors.toList());
        try {
            var dto = getDataFromForm();
            new InTransactionWorker(
                    session -> {
                        var queries = new JournalEntryQuery_(session);
                        var period = session.merge(dto.period());

                        if (queries.existByNumAndTypeAndPeriod(dto.type(), dto.number(), period))
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
                        entry.setPeriod(period);

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
            this.tblModelRecord = new CustomTableModel<>("Referencia", "Cuenta", "Débito", "Crédito") {
                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return switch (data.get(rowIndex)) {
                        case RecordTableData r -> switch (columnIndex) {
                            case 0 -> r.reference();
                            case 1 -> r.account();
                            case 2 -> r.debit();
                            case 3 -> r.credit();
                            default -> "que haces?";
                        };
                        case RecordTableTotal(var debit, var credit) -> switch (columnIndex) {
                            case 0 -> "Total";
                            case 2 -> debit;
                            case 3 -> credit;
                            default -> "";
                        };
                    };
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return switch (columnIndex) {
                        case 1 -> Account.class;
                        case 2, 3 -> BigDecimal.class;
                        default -> String.class;
                    };
                }
            };
        }
        public void clear(){
            loadAccounts();
            formRecords.clear();
            loadData();
        }

        public void init() {
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
            clear();
        }

        public void loadAccounts(){
            new FromTransactionWorker<>(
                    session -> new AccountQuery_(session).findAll(),
                    a -> {
                        accounts.clear();
                        accounts.addAll(a);
                        filterAccounts();
                    },
                    AccountingEntryView.this::showError
            ).execute();
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
            var dtos = new ArrayList<RecordTableRow>(records.size() + formRecords.size() + 1);
            for (var record : records) {
                var account = record.getAccount();
                if (account == null) {
                    records.remove(record);
                    continue;
                }
                debitSum = debitSum.add(record.getDebit(), DECIMAL128);
                creditSum = creditSum.add(record.getCredit(), DECIMAL128);
                dtos.add(new RecordTableEntity(record));
            }
            for (var record : formRecords) {
                debitSum = debitSum.add(record.debit(), DECIMAL128);
                creditSum = creditSum.add(record.credit(), DECIMAL128);
                dtos.add(new RecordTableFormData(record));
            }
            dtos.add(new RecordTableTotal(debitSum, creditSum));
            isBalanced = debitSum.equals(creditSum);
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
                case RecordTableEntity(var entity) ->
                        new InTransactionWorker(
                                session -> session.remove(session.merge(entity)),
                                this::loadData,
                                AccountingEntryView.this::showError
                        ).execute();
                case RecordTableFormData(var formData) -> {
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
                    case RecordTableEntity(var entity) ->
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
                    case RecordTableFormData(var formData) -> {
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
        OpRecord = new com.nutrehogar.sistemacontable.ui_2.component.OperationPanel("Registro", false);
        pnlEntryForm = new javax.swing.JPanel();
        lblEntryName = new javax.swing.JLabel();
        txtJName = new javax.swing.JTextField();
        lblConcept = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taJConcept = new javax.swing.JTextArea();
        lblEntryDocumentNumber = new javax.swing.JLabel();
        lblEntryDate = new javax.swing.JLabel();
        txtJCheckNumber = new javax.swing.JTextField();
        lblEntryCeckNumber = new javax.swing.JLabel();
        spnJDate = new com.nutrehogar.sistemacontable.ui_2.component.LocalDateSpinner(spnModelJDate);
        btnJSave = new javax.swing.JButton();
        btnJUpdate = new javax.swing.JButton();
        btnJDelete = new javax.swing.JButton();
        btnJAdd = new javax.swing.JButton();
        cbxJDocType = new javax.swing.JComboBox<>(cbxModelJDocType);
        spnJDocNumber = new javax.swing.JSpinner(spnModelJDocNumber);
        lblStart = new javax.swing.JLabel();
        cbxPeriod = new com.nutrehogar.sistemacontable.ui_2.component.CustomComboBox<>(cbxModelPeriod);
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
        tblRecord = new com.nutrehogar.sistemacontable.ui_2.component.CustomTable(recordController.tblModelRecord);
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
        btnRSave.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        cbxRAccount.setModel(recordController.cbxModelRAccount);

        btnRUpdate.setText("Actualizar");
        btnRUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

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
        lblEntryDocumentNumber.setText("Doc:");

        lblEntryDate.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEntryDate.setLabelFor(spnJDate);
        lblEntryDate.setText("Fecha:");

        lblEntryCeckNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEntryCeckNumber.setLabelFor(txtJCheckNumber);
        lblEntryCeckNumber.setText("No Cheque:");

        btnJSave.setText("Guardar");
        btnJSave.setToolTipText("");
        btnJSave.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnJSave.setMaximumSize(new java.awt.Dimension(82, 23));
        btnJSave.setMinimumSize(new java.awt.Dimension(82, 23));
        btnJSave.setPreferredSize(new java.awt.Dimension(82, 23));

        btnJUpdate.setText("Actualizar");
        btnJUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnJUpdate.setMaximumSize(new java.awt.Dimension(73, 23));
        btnJUpdate.setMinimumSize(new java.awt.Dimension(73, 23));
        btnJUpdate.setPreferredSize(new java.awt.Dimension(73, 23));

        btnJDelete.setText("Eliminar");
        btnJDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnJAdd.setText("Nuevo");
        btnJAdd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        cbxJDocType.setToolTipText("Tipo de Documento");
        cbxJDocType.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        lblStart.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStart.setText("Período:");

        cbxPeriod.setModel(cbxModelPeriod);

        javax.swing.GroupLayout pnlEntryFormLayout = new javax.swing.GroupLayout(pnlEntryForm);
        pnlEntryForm.setLayout(pnlEntryFormLayout);
        pnlEntryFormLayout.setHorizontalGroup(
            pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEntryFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEntryCeckNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblConcept, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblEntryName, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnJAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnJSave, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(cbxPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEntryDate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnJDate, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblEntryDocumentNumber)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnJDocNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxJDocType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 113, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEntryFormLayout.createSequentialGroup()
                        .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtJCheckNumber)
                            .addComponent(txtJName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnJUpdate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnJDelete, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        pnlEntryFormLayout.setVerticalGroup(
            pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEntryFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnJDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEntryName)
                        .addComponent(txtJName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEntryCeckNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtJCheckNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnJUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEntryDocumentNumber)
                        .addComponent(spnJDocNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbxJDocType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEntryDate)
                        .addComponent(spnJDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblStart)
                        .addComponent(cbxPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEntryFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEntryFormLayout.createSequentialGroup()
                        .addComponent(btnJAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnJSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(7, 7, 7))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblConcept))
                .addGap(3, 3, 3))
        );

        cbxJDocType.getAccessibleContext().setAccessibleName("");

        pnlSourceDocuments.setBorder(javax.swing.BorderFactory.createTitledBorder("Documentos Exportables"));
        pnlSourceDocuments.setOpaque(false);

        btnGeneratePaymentVoucher.setText("Comprobante");
        btnGeneratePaymentVoucher.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnGenerateRegistrationForm.setText("Formulario");
        btnGenerateRegistrationForm.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        lblCreateBy.setText("N/A");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Creado por: ");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Creacion: ");

        lblCreateAt.setText("N/A");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Actualizado por: ");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Actualizacion:");

        lblUpdateBy.setText("N/A");

        lblUpdateAt.setText("N/A");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCreateBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCreateAt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUpdateBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUpdateAt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(157, 157, 157))
        );
        pnlSourceDocumentsLayout.setVerticalGroup(
            pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSourceDocumentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSourceDocumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGeneratePaymentVoucher)
                    .addComponent(jLabel1)
                    .addComponent(lblCreateBy)
                    .addComponent(jLabel4)
                    .addComponent(lblUpdateBy)
                    .addComponent(jLabel5)
                    .addComponent(lblVersion))
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
        lblTitle.setIcon(UIEntityInfo.ENTRY_FORM.getIcon().derive(Theme.ICON_MD, Theme.ICON_MD));
        lblTitle.setText(UIEntityInfo.ENTRY_FORM.getPlural());

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
    private javax.swing.JComboBox<DocumentType> cbxJDocType;
    private com.nutrehogar.sistemacontable.ui_2.component.CustomComboBox<AccountingPeriod> cbxPeriod;
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
    private javax.swing.JLabel lblStart;
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
    private javax.swing.JSpinner spnJDocNumber;
    private javax.swing.JSpinner spnRAccountNumber;
    private javax.swing.JSpinner spnRAmount;
    private javax.swing.JTextArea taJConcept;
    private com.nutrehogar.sistemacontable.ui_2.component.CustomTable<RecordTableRow> tblRecord;
    private javax.swing.JTextField txtJCheckNumber;
    private javax.swing.JTextField txtJName;
    private javax.swing.JTextField txtRReference;
    // End of variables declaration//GEN-END:variables

}