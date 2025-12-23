package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
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
    @NaturalId
    String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    AccountSubtype subtype;

    @OneToMany(mappedBy = LedgerRecord_.ACCOUNT, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @NotNull
    Set<LedgerRecord> records = new HashSet<>();

    public Account(@NotNull String updatedBy, @NotNull AccountSubtype subtype, @NotNull String name) {
        super(updatedBy);
        this.subtype = subtype;
        this.name = name;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Account account)) return false;
        return account.getName().equals(this.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getCanonicalId() {
        int subtypeIdLength = subtype.getId().toString().length();
        return id.toString().substring(subtypeIdLength);
    }

    public String getFormattedId() {
        return subtype.getFormattedId() + getCanonicalId();
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

}
