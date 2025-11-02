package com.nutrehogar.sistemacontable.domain.type;

import lombok.Getter;

@Getter
public enum DocumentType {
    INCOME("INGRESO"), EXPENDITURE("EGRESO"), ADJUSTMENT("AJUSTE");

    private final String name;

    DocumentType(String name) {
        this.name = name;
    }
}
