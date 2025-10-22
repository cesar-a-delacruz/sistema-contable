package com.nutrehogar.sistemacontable.application.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditableDTO {
    String createdBy;
    String updatedBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
