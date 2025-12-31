package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"records", "subtype"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account extends AuditableEntity {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false, unique = true)
    @Basic(optional = false)
    @NotNull
    @NaturalId(mutable = true)
    Integer number;

    @Column(nullable = false, unique = true)
    @Basic(optional = false)
    @NotNull String name;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @NotNull
    @Column(name = "account_type", nullable = false)
    AccountType type;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @Nullable AccountSubtype subtype;

    @OneToMany(mappedBy = LedgerRecord_.ACCOUNT, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @NotNull Set<LedgerRecord> records = new HashSet<>();

    public Account(@NotNull String updatedBy) {
        super(updatedBy);
    }

    public Account(@NotNull Integer number, @NotNull String name, @NotNull AccountType type, @NotNull String updatedBy) {
        super(updatedBy);
        this.number = number;
        this.name = name;
        this.type = type;
    }

    public Account(@NotNull String name, @Nullable AccountSubtype subtype, @NotNull String updatedBy) {
        super(updatedBy);
        this.subtype = subtype;
        this.name = name;
    }

    public void setNumber(@NotNull String subNumber, @NotNull AccountType type) {
        this.number = AccountNumber.generateNumber(subNumber, type);
    }

    public Integer getSubNumber() {
        return AccountNumber.getSubNumber(number);
    }

    public String getFormattedNumber() {
        return AccountNumber.getFormattedNumber(number);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Account account)) return false;

        return Objects.equals(id, account.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
