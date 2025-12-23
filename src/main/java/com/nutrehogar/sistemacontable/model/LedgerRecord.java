package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "entry")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "ledger_records")
public class LedgerRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    JournalEntry entry;

    @Column(nullable = false, length = 600)
    @Basic(optional = false)
    @NotNull
    String reference;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    Account account;

    @NotNull
    @Basic(optional = false)
    @Column(precision = 15, scale = 2, nullable = false)
    BigDecimal debit;

    @NotNull
    @Basic(optional = false)
    @Column(precision = 15, scale = 2, nullable = false)
    BigDecimal credit;
}
