package com.nutrehogar.sistemacontable.base.domain.repository;

import com.nutrehogar.sistemacontable.domain.model.JournalEntry;
import com.nutrehogar.sistemacontable.domain.model.JournalEntryPK;
import com.nutrehogar.sistemacontable.domain.type.DocumentType;
import com.nutrehogar.sistemacontable.exception.RepositoryException;
import java.time.LocalDate;
import java.util.List;

public interface JournalEntryRepository extends CRUDRepository<JournalEntry, JournalEntryPK> {
    List<JournalEntry> findAllByDateRange(LocalDate startDate, LocalDate endDate) throws RepositoryException;

    JournalEntry findLast(DocumentType type) throws RepositoryException;
}