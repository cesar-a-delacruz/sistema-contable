package com.nutrehogar.sistemacontable.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DocumentType {
    INCOME("Ingreso"), EXPENDITURE("Egreso"), ADJUSTMENT("Ajuste");

    String name;
}
