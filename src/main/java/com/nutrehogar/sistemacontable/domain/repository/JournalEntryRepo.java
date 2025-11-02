package com.nutrehogar.sistemacontable.domain.repository;

import com.nutrehogar.sistemacontable.base.domain.repository.JournalEntryRepository;
import com.nutrehogar.sistemacontable.domain.model.JournalEntry;
import com.nutrehogar.sistemacontable.domain.model.JournalEntryPK;
import com.nutrehogar.sistemacontable.exception.RepositoryException;
import com.nutrehogar.sistemacontable.persistence.TransactionManager;

import java.time.LocalDate;
import java.util.List;

public class JournalEntryRepo extends CRUDRepo<JournalEntry, JournalEntryPK>
        implements JournalEntryRepository {

    public JournalEntryRepo() {
        super(JournalEntry.class);
    }

    @Override
    public List<JournalEntry> findAllByDateRange(LocalDate startDate, LocalDate endDate) throws RepositoryException {
        return TransactionManager.executeInTransaction(session -> session.createQuery(
                "FROM JournalEntry WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC",
                JournalEntry.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .list());
    }
}