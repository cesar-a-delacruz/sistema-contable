package com.nutrehogar.sistemacontable.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Permission {
    AUDIT("Auditar"), CONTRIBUTE("Contribuir"), ADMIN("Administrar");

    String name;
}
