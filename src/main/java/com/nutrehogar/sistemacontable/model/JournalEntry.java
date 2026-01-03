package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"records", "period"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "journal_entry")
public class JournalEntry extends AuditableEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable Long id;

    @Column(name = "document_number", nullable = false)
    @Basic(optional = false)
    @NotNull Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    @NotNull DocumentType type;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull String name;

    @Column(nullable = false, length = 600)
    @Basic(optional = false)
    @NotNull String concept;

    @Column(name = "check_number", nullable = false)
    @Basic(optional = false)
    @NotNull String checkNumber;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull LocalDate date;

    @OneToMany(mappedBy = LedgerRecord_.ENTRY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @NotNull List<LedgerRecord> records = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @Nullable AccountingPeriod period;

    public JournalEntry(@NotNull Integer number, @NotNull DocumentType type, @NotNull String name, @NotNull String concept, @NotNull String checkNumber, @NotNull LocalDate date, @NotNull String updatedBy) {
        super(updatedBy);
        this.number = number;
        this.type = type;
        this.name = name;
        this.concept = concept;
        this.checkNumber = checkNumber;
        this.date = date;
    }
}
