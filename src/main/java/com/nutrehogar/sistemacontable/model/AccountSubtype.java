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

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "accounts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "account_subtypes")
public class AccountSubtype extends AuditableEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
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

    @OneToMany(mappedBy = Account_.SUBTYPE, fetch = FetchType.LAZY, cascade = {PERSIST})
    @NotNull
    @OnDelete(action = OnDeleteAction.SET_NULL)
    Set<Account> accounts = new HashSet<>();

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

    public void setNumber(@NotNull String subNumber, @NotNull AccountType type) {
        this.number = AccountNumber.generateNumber(subNumber, type);
    }

    public String getSubNumber() {
        return AccountNumber.getSubNumber(number);
    }

    public String getFormattedNumber() {
        return AccountNumber.getFormattedNumber(number);
    }

//    public static class Comparator implements java.util.Comparator<AccountSubtype> {
//        @Override
//        public int compare(AccountSubtype o1, AccountSubtype o2) {
//            int mainPart1 = o1.getType().getId();
//            int mainPart2 = o2.getType().getId();
//
//            // Comparar la parte entera primero
//            if (mainPart1 != mainPart2) {
//                return Integer.compare(mainPart1, mainPart2);
//            }
//
//            Integer subPart1 = ajustarDecimal(o1.getCanonicalId());
//            Integer subPart2 = ajustarDecimal(o2.getCanonicalId());
//
//            return Integer.compare(subPart1, subPart2);
//        }

//        private Integer ajustarDecimal(String decimal) {
//            // Normalizar la longitud a 3 dígitos rellenando a la derecha
//            if (decimal.length() == 1) {
//                return Integer.parseInt(decimal) * 100; // "6" → "600"
//            } else if (decimal.length() == 2) {
//                return Integer.parseInt(decimal) * 10; // "61" → "610"
//            } else {
//                return Integer.parseInt(decimal); // "999" → "999"
//            }
//        }
//    }

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
