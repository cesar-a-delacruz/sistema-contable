package com.nutrehogar.sistemacontable.model;

import java.time.LocalDateTime;


public interface AuditableFields {

    String getCreatedBy();

    String getUpdatedBy();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    int getVersion();
}
