package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
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
@ToString(exclude = "entries")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "accounting_period")
public class AccountingPeriod extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    @Nullable Integer id;

    @Column(nullable = false, unique = true)
    @Basic(optional = false)
    @NaturalId(mutable = true)
    @NotNull Integer year;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull LocalDate startDate;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull LocalDate endDate;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull Boolean closed;

    @OneToMany(mappedBy = JournalEntry_.PERIOD, fetch = FetchType.LAZY)
    @NotNull List<JournalEntry> entries = new ArrayList<>();

    public AccountingPeriod(@NotNull Integer year, @NotNull LocalDate startDate, @NotNull LocalDate endDate, @NotNull Boolean closed, @NotNull String updatedBy) {
        super(updatedBy);
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
        this.closed = closed;
    }
}
