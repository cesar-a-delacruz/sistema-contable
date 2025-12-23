package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "records")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "journal_entry")
public class JournalEntry extends AuditableEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "document_number", nullable = false)
    @Basic(optional = false)
    @NotNull
    Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    @NotNull
    DocumentType type;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull
    String name;

    @Column(nullable = false, length = 600)
    @Basic(optional = false)
    @NotNull
    String concept;

    @Column(name = "check_number", nullable = false)
    @Basic(optional = false)
    @NotNull
    String checkNumber;

    @Column(nullable = false)
    @Basic(optional = false)
    @NotNull
    LocalDate date;

    @NotNull
    @OneToMany(mappedBy = LedgerRecord_.ENTRY, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<LedgerRecord> records = new HashSet<>();

}
