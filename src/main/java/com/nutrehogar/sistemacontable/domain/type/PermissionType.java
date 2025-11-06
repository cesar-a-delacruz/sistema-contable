package com.nutrehogar.sistemacontable.domain.type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum PermissionType {
    VIEW("ViewerAuditor"), CREATE("DataAdministrator"), ADD_ONLY("DataContributor");

    String name;
}
