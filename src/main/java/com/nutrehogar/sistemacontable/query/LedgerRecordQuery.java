//package com.nutrehogar.sistemacontable.query;
//
//import com.nutrehogar.sistemacontable.model.Account;
//import com.nutrehogar.sistemacontable.model.LedgerRecord;
//import org.hibernate.annotations.processing.Find;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface LedgerRecordQuery extends Query{
//    @Find("SELECT lr FROM LedgerRecord lr JOIN FETCH lr.journalEntry WHERE lr.account = :account AND lr.journalEntry.date < :endDate ORDER BY lr.journalEntry.date DESC")
//    List<LedgerRecord> findByDateRangeAndAccount(Account account, LocalDate endDate);
//
//    @Find("SELECT lr FROM LedgerRecord lr JOIN FETCH lr.journalEntry WHERE lr.account.id = :accountId AND lr.journalEntry.date < :endDate ORDER BY lr.journalEntry.date DESC)
//    List<LedgerRecord> findByDateRangeAndAccountId(Integer accountId, LocalDate endDate);
//}
