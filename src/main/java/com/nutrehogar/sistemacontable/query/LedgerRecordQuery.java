package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.JournalEntry;
import com.nutrehogar.sistemacontable.model.LedgerRecord;
import com.nutrehogar.sistemacontable.model.User;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;

import java.util.List;
import java.util.Optional;

public interface LedgerRecordQuery extends Query {

    @Find
    List<LedgerRecord> findAll();

    @Find
    Optional<LedgerRecord> findById(Long id);

    @HQL("select distinct a from LedgerRecord a inner join fetch a.account where a.entry = :entry")
    List<LedgerRecord> findAllAndAccountsByJournal(JournalEntry entry);

}
