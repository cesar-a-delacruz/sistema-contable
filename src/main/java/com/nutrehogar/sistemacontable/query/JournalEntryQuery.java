//package com.nutrehogar.sistemacontable.query;
//
//import com.nutrehogar.sistemacontable.model.DocumentType;
//import com.nutrehogar.sistemacontable.model.JournalEntry;
//import org.hibernate.annotations.processing.Find;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface JournalEntryQuery extends Query{
//
//    @Find("FROM JournalEntry WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
//    List<JournalEntry> findAllByDateRange(LocalDate startDate, LocalDate endDate);
//
//    @Find("FROM JournalEntry  WHERE id.documentType = :type ORDER BY id.documentNumber DESC")
//    JournalEntry findLast(DocumentType type);
//}
