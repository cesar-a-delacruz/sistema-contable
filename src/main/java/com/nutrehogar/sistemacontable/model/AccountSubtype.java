package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "accounts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "account_subtypes")
public class AccountSubtype extends AccountEntity {

    @OneToMany(mappedBy = Account_.SUBTYPE, fetch = FetchType.LAZY)
    @NotNull List<Account> accounts = new ArrayList<>();

    public AccountSubtype(@NotNull String updatedBy) {
        super(updatedBy);
    }

    public AccountSubtype(@NotNull Integer number, @NotNull String name, @NotNull AccountType type, @NotNull String updatedBy) {
        super(updatedBy);
        this.number = number;
        this.name = name;
        this.type = type;
    }

    public AccountSubtype(@NotNull String name, @NotNull AccountType type, @NotNull String updatedBy) {
        super(updatedBy);
        this.name = name;
        this.type = type;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof AccountSubtype that)) return false;
        return Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
