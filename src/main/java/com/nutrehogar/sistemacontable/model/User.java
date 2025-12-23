package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User extends AuditableEntity {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Basic(optional = false)
    @Column(nullable = false)
    @NonNull
    String password;

    @Basic(optional = false)
    @Column(nullable = false, unique = true)
    @NaturalId
    @NonNull
    String username;

    @Basic(optional = false)
    @Column(name = "enabled", nullable = false)
    @NonNull
    Boolean enabled;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NonNull
    Permission permissions;

    public User(@NonNull String password, @NonNull String username, @NonNull Boolean enabled, @NonNull Permission permissions, @NotNull String updatedBy) {
        super(updatedBy);
        this.password = password;
        this.username = username;
        this.enabled = enabled;
        this.permissions = permissions;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return user.getUsername().equals(this.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}