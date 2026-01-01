package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"entry", "account"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "ledger_records")
public class LedgerRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    @Nullable Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @Nullable
    @OnDelete(action = OnDeleteAction.CASCADE)
    JournalEntry entry;

    @Column(nullable = false, length = 600)
    @Basic(optional = false)
    @NotNull String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Nullable
    @OnDelete(action = OnDeleteAction.RESTRICT)
    Account account;

    @NotNull
    @Basic(optional = false)
    @Column(precision = 15, scale = 2, nullable = false)
    BigDecimal debit;

    @NotNull
    @Basic(optional = false)
    @Column(precision = 15, scale = 2, nullable = false)
    BigDecimal credit;

    public LedgerRecord(@NotNull String reference, @NotNull Account account, @NotNull BigDecimal debit, @NotNull BigDecimal credit, @NotNull String updatedBy) {
        super(updatedBy);
        this.reference = reference;
        this.account = account;
        this.debit = debit;
        this.credit = credit;
    }

    public LedgerRecord(@NotNull JournalEntry entry, @NotNull String reference, @NotNull Account account, @NotNull BigDecimal debit, @NotNull BigDecimal credit, @NotNull String updatedBy) {
        super(updatedBy);
        this.entry = entry;
        this.reference = reference;
        this.account = account;
        this.debit = debit;
        this.credit = credit;
    }
}
