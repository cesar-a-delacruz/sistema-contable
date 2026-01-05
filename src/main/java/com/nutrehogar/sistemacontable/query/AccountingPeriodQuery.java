package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountingPeriod;
import com.nutrehogar.sistemacontable.ui.Period;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;

import java.util.List;
import java.util.Optional;

public interface AccountingPeriodQuery extends Query {

    @HQL("select distinct a from AccountingPeriod a order by a.year asc")
    List<AccountingPeriod> findAll();

    @HQL("select distinct new Period(a.id, a.year) from AccountingPeriod a order by a.year asc")
    List<Period> findAllMinData();

    @HQL("select distinct a from AccountingPeriod a where a.closed = false order by a.year asc")
    List<AccountingPeriod> findAllOpen();

    @Find
    Optional<AccountingPeriod> findById(Integer id);

    @HQL("select distinct a from AccountingPeriod a left join fetch a.entries order by a.year asc")
    List<AccountingPeriod> findAccountingPeriodsAndEntries();

    @HQL("select a from AccountingPeriod a where a.year = :year")
    Optional<AccountingPeriod> findByYear(Integer year);

    @HQL("select cast(count(je.id) as boolean) from JournalEntry je where je.period = :period")
    boolean isUsed(AccountingPeriod period);

    @HQL("select cast(coalesce(count(a.id), 0) as boolean) from AccountingPeriod a where a.year = :year")
    boolean yearIsUsed(int year);

}
