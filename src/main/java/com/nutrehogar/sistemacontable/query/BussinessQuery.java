package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.ui.business.JournalData;
import org.hibernate.annotations.processing.HQL;

import java.time.LocalDate;
import java.util.List;

public interface BussinessQuery extends Query {
    @HQL("select new com.nutrehogar.sistemacontable.ui.business.JournalData(j.id, j.date, j.number, j.type, new com.nutrehogar.sistemacontable.ui.business.AccountMinData(r.account.number,r.account.name,r.account.type), r.reference, r.debit, r.credit) from LedgerRecord r inner join r.entry j where j.date between :startDate and :endDate order by j.date desc ")
    List<JournalData> findJournalByDateRange(LocalDate startDate, LocalDate endDate);
//
//    @Find("FROM JournalEntry  WHERE id.documentType = :type ORDER BY id.documentNumber DESC")
//    JournalEntry findLast(DocumentType type);
    //    @Find("SELECT lr FROM LedgerRecord lr JOIN FETCH lr.journalEntry WHERE lr.account = :account AND lr.journalEntry.date < :endDate ORDER BY lr.journalEntry.date DESC")
//    List<LedgerRecord> findByDateRangeAndAccount(Account account, LocalDate endDate);
//
//    @Find("SELECT lr FROM LedgerRecord lr JOIN FETCH lr.journalEntry WHERE lr.account.id = :accountId AND lr.journalEntry.date < :endDate ORDER BY lr.journalEntry.date DESC)
//    List<LedgerRecord> findByDateRangeAndAccountId(Integer accountId, LocalDate endDate);
}
