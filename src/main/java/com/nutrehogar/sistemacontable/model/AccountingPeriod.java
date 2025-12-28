package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString()
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "accounting_period")
public class AccountingPeriod extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    Integer id;

    @Column(nullable = false, unique = true)
    @Basic(optional = false)
    @NotNull
    Integer year;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull
    Integer periodNumber;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull
    LocalDate startDate;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull
    LocalDate endDate;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull
    Boolean closed;

    @NotNull
    @OneToMany(mappedBy = JournalEntry_.PERIOD, cascade = {CascadeType.PERSIST}, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    Set<JournalEntry> entries = new HashSet<>();

    public AccountingPeriod(
            @NotNull Integer year,
            @NotNull Integer periodNumber,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotNull Boolean closed,
            @NotNull String updatedBy
    ) {
        super(updatedBy);
        this.year = year;
        this.periodNumber = periodNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.closed = closed;
    }
}
