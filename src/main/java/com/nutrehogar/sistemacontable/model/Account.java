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
@ToString(exclude = "records")
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
    @NotNull
    String name;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @NotNull
    @Column(name = "account_type", nullable = false)
    AccountType type;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @Nullable
    AccountSubtype subtype;

    @OneToMany(mappedBy = LedgerRecord_.ACCOUNT, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @NotNull
    Set<LedgerRecord> records = new HashSet<>();

    public Account(@NotNull String updatedBy) {
        super(updatedBy);
    }

    public Account(@NotNull String name, @NotNull String updatedBy, @Nullable AccountSubtype subtype) {
        super(updatedBy);
        this.subtype = subtype;
        this.name = name;
    }


    public void setNumber(@NotNull String subNumber, @NotNull AccountType type) {
        int remaining = 4 - subNumber.length();
        this.number = Integer.valueOf(type.getId() + (remaining == 0 ? subNumber : subNumber + "0".repeat(remaining)));
    }

    public String getSubNumber() {
        return number.toString().substring(1);
    }


    public String getFormattedNumber() {
        var subNumber = number.toString().substring(1);
        return type.getId() + "." + subNumber;
    }


    public static @NotNull String getCellRenderer(Integer id) {
        if (id == null)
            return "";
        return getCellRenderer(id.toString());
    }

    public static @NotNull String getCellRenderer(String id) {
        if (id == null)
            return "";
        return id.charAt(0) + "." + id.substring(1);
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
