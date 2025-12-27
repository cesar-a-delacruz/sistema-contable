package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

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
    Long id;

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
}
