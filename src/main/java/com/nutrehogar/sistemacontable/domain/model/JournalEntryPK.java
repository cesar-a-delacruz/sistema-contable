package com.nutrehogar.sistemacontable.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;

import com.nutrehogar.sistemacontable.domain.type.DocumentType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString()
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode()
@Embeddable
public class JournalEntryPK implements Serializable {

    @Column(name = "document_number")
    Integer documentNumber;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "document_type")
    DocumentType documentType;
}
