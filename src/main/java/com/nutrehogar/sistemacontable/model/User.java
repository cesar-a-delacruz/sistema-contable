package com.nutrehogar.sistemacontable.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
    @NotNull
    String password;

    @Basic(optional = false)
    @Column(nullable = false, unique = true)
    @NotNull
    String username;

    @Basic(optional = false)
    @Column(name = "enabled", nullable = false)
    @NotNull
    Boolean enabled;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    Permission permissions;

    public User(@NotNull String updatedBy) {
        super(updatedBy);
    }

    public User(@NotNull String password, @NotNull String username, @NotNull Boolean enabled, @NotNull Permission permissions, @NotNull String updatedBy) {
        super(updatedBy);
        this.password = password;
        this.username = username;
        this.enabled = enabled;
        this.permissions = permissions;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof User user)) return false;

        return Objects.equals(id, user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public boolean isAdmin(){
        return permissions.equals(Permission.ADMIN);
    }
    public boolean isContributor() {
        return permissions.equals(Permission.CONTRIBUTE);
    }
    public boolean isAudit() {
        return permissions.equals(Permission.AUDIT);
    }
}