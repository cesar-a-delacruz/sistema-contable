package com.nutrehogar.sistemacontable.base.domain.repository;

import com.nutrehogar.sistemacontable.domain.model.Account;
import com.nutrehogar.sistemacontable.domain.model.LedgerRecord;
import com.nutrehogar.sistemacontable.exception.RepositoryException;
import java.time.LocalDate;
import java.util.List;

public interface LedgerRecordRepository extends CRUDRepository<LedgerRecord, Integer> {
    List<LedgerRecord> findByDateRangeAndAccount(Account account, LocalDate endDate)
            throws RepositoryException;

    public List<LedgerRecord> findByDateRangeAndAccountId(Integer accountId, LocalDate endDate)
            throws RepositoryException;
}
