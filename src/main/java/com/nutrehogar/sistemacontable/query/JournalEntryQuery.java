package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.AccountingPeriod;
import com.nutrehogar.sistemacontable.model.DocumentType;
import com.nutrehogar.sistemacontable.model.JournalEntry;
import com.nutrehogar.sistemacontable.model.JournalEntry;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;

import java.util.List;
import java.util.Optional;

public interface JournalEntryQuery extends Query {

    @Find
    List<JournalEntry> findAll();

    @Find
    Optional<JournalEntry> findById(Long id);

    @HQL("select coalesce(max(j.number), 2) from JournalEntry j")
    Integer findNexTDocNumber();

    @HQL("select (coalesce(max(j.number), 1) + 1) from JournalEntry j where j.period = :period and j.type = :type")
    Integer findNextNumByTypeAndPeriod(DocumentType type, AccountingPeriod  period);
    @HQL("select j from JournalEntry j inner join fetch j.period where j.id = :id")
    Optional<JournalEntry> findAndPeriodById(Long id);

    @HQL("select cast(count(j.id) as boolean) from JournalEntry j where j.period = :period and j.type = :type and j.number = :number")
    boolean existByNumAndTypeAndPeriod(DocumentType type, Integer number, AccountingPeriod period);
}
