```java

private static final class JournalController {
        public JournalController() {

        }
    }

    private static final class RecordController {
        public CustomTableModel<LedgerRecord> tblModelRecord;

        public RecordController() {
            this.tblModelRecord = new CustomTableModel<>("Referencia", "Cuenta", "Débito", "Crédito") {

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    var record = data.get(rowIndex);
                    return switch (columnIndex) {
                        case 0 -> record.getReference();
                        case 1 -> record.getAccount().getId();
                        case 2 -> record.getDebit();
                        case 3 -> record.getCredit();
                        default -> "que haces?";
                    };
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }
            };
        }
    }
```