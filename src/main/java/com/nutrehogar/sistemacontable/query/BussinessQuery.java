package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.Tuple;
import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountingPeriod;
import com.nutrehogar.sistemacontable.ui.Period;
import com.nutrehogar.sistemacontable.ui.business.AccountTotal;
import com.nutrehogar.sistemacontable.ui.business.JournalData;
import org.hibernate.annotations.processing.HQL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BussinessQuery extends Query {
    @HQL("select new com.nutrehogar.sistemacontable.ui.business.JournalData(j.id, j.date, j.number, j.type, new com.nutrehogar.sistemacontable.ui.business.AccountMinData(r.account.number,r.account.name,r.account.type), r.reference, r.debit, r.credit) from LedgerRecord r inner join r.entry j inner join j.period p where p.id = :periodId and extract(month from j.date) = :month order by j.date ,j.createdAt , r.createdAt")
    List<JournalData> findJournalByPeriodIdAndMonth(int  periodId, int month);
    @HQL("select new com.nutrehogar.sistemacontable.ui.business.JournalData(j.id, j.date, j.number, j.type, new com.nutrehogar.sistemacontable.ui.business.AccountMinData(r.account.number,r.account.name,r.account.type), r.reference, r.debit, r.credit) from LedgerRecord r inner join r.entry j inner join j.period p where p.id = :periodId and r.account = :account order by j.date ,j.createdAt , r.createdAt")
    List<JournalData> findJournalByPeriodIdAndAccount(int  periodId, Account account);
    @HQL("select new com.nutrehogar.sistemacontable.ui.business.AccountTotal(cast(coalesce(sum(r.debit), 0) as BigDecimal), cast(coalesce(sum(r.credit), 0) as BigDecimal)) from LedgerRecord r inner join r.entry j inner join j.period p where p.year < :periodYear and r.account = :account")
    Optional<AccountTotal> findAccountPreTotal(int periodYear, Account account);
}
