package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@ToString(exclude = {"records", "subtype"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account extends AccountEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @Nullable AccountSubtype subtype;

    @OneToMany(mappedBy = LedgerRecord_.ACCOUNT, fetch = FetchType.LAZY)
    @NotNull List<LedgerRecord> records = new ArrayList<>();

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
