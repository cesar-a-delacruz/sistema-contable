package com.nutrehogar.sistemacontable.controller.business.dto;

import com.nutrehogar.sistemacontable.domain.model.JournalEntryPK;
import com.nutrehogar.sistemacontable.domain.type.AccountType;
import com.nutrehogar.sistemacontable.domain.type.DocumentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GeneralLedgerTableDTO extends AuditableDTO {
    JournalEntryPK entryId;
    LocalDate entryDate;
    DocumentType documentType;
    Integer accountId;
    AccountType accountType;
    Integer voucher;
    String reference;
    BigDecimal debit;
    BigDecimal credit;
    @Setter
    BigDecimal balance;

    public GeneralLedgerTableDTO(String createdBy, String updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt,
            JournalEntryPK entryId, LocalDate entryDate, DocumentType documentType, Integer accountId,
            AccountType accountType, Integer voucher, String reference, BigDecimal debit, BigDecimal credit,
            BigDecimal balance) {
        super(createdBy, updatedBy, createdAt, updatedAt);
        this.entryId = entryId;
        this.entryDate = entryDate;
        this.documentType = documentType;
        this.accountId = accountId;
        this.accountType = accountType;
        this.voucher = voucher;
        this.reference = reference;
        this.debit = debit;
        this.credit = credit;
        this.balance = balance;
    }

    public GeneralLedgerTableDTO(String reference, BigDecimal debit, BigDecimal credit, BigDecimal balance) {
        this.reference = reference;
        this.debit = debit;
        this.credit = credit;
        this.balance = balance;
    }
}