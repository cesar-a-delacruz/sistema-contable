package com.nutrehogar.sistemacontable.model;

import com.nutrehogar.sistemacontable.exception.InvalidFieldException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MappedSuperclass
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
@NoArgsConstructor
public abstract class AccountEntity extends AuditableEntity {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable Integer id;

    @Column(nullable = false, unique = true)
    @Basic(optional = false)
    @NaturalId(mutable = true)
    @NotNull Integer number;

    @Column(nullable = false, unique = true)
    @Basic(optional = false)
    @NotNull String name;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @Column(name = "account_type", nullable = false)
    @NotNull AccountType type;

    protected AccountEntity(@NotNull String updatedBy) {
        super(updatedBy);
    }

    public void setNumber(@NotNull String subNumber, @NotNull AccountType type) {
        this.number = AccountEntity.generateNumber(subNumber, type);
    }

    public Integer getSubNumber() {
        return AccountEntity.getSubNumber(number);
    }

    public String getFormattedNumber() {
        return AccountEntity.getFormattedNumber(number);
    }

    @NonNull
    public static Integer generateNumber(@NotNull String subNumber, @NotNull AccountType type) throws InvalidFieldException {
        int length = subNumber.length();
        if (length > 4 || length == 0)
            throw new InvalidFieldException("El numero de cuenta debe tener entres 1 a 4 dígitos", new NumberFormatException("The Account Number length must be between 4 and 0 digits"));
        int remaining = 4 - length;
        return Integer.valueOf(type.getId() + (remaining == 0 ? subNumber : "0".repeat(remaining) + subNumber));
    }

    @NonNull
    public static Integer generateNumber(@NotNull Integer subNumber, @NotNull AccountType type) {
        return generateNumber(subNumber.toString(), type);
    }

    @NotNull
    public static Integer getSubNumber(@NotNull Integer number) {
        return Integer.valueOf(number.toString().substring(1));
    }

    @NotNull
    public static String getFormattedNumber(@NotNull Integer number) {
        var str = number.toString();
        return str.charAt(0) + "." + str.substring(1);
    }
}
