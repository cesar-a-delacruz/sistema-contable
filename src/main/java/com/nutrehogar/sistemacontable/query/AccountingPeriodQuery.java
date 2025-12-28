package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountingPeriod;
import com.nutrehogar.sistemacontable.ui.crud.AccountData;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;

import java.util.List;
import java.util.Optional;

public interface AccountingPeriodQuery extends Query {

    @HQL("select distinct a from AccountingPeriod a order by a.year asc")
    List<AccountingPeriod> findAll();

    @Find
    Optional<AccountingPeriod> findById(Integer id);

    @HQL("select distinct a from AccountingPeriod a left join fetch a.entries order by a.year asc")
    List<AccountingPeriod> findAccountingPeriodsAndEntries();


}
