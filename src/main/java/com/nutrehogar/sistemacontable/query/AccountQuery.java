package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountingPeriod;
import com.nutrehogar.sistemacontable.model.DocumentType;
import com.nutrehogar.sistemacontable.ui.crud.AccountData;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;

import java.util.List;
import java.util.Optional;

public interface AccountQuery extends Query {

    @HQL("select distinct a from Account a order by a.number asc")
    List<Account> findAll();

    @Find
    Optional<Account> findById(Integer id);

    @HQL("select distinct a from Account a left join fetch a.subtype order by a.number asc")
    List<Account> findAccountsAndSubtypes();

    @HQL("select distinct new AccountData(a.id, a.number, a.name, a.type, s.id, s.name, a.createdBy, a.updatedBy, a.createdAt, a.updatedAt, a.version) from Account a left join a.subtype s order by a.number asc")
    List<AccountData> findAllDataAndSubtypes();

    @HQL("select cast(count(lr.id) as boolean) from LedgerRecord lr where lr.account = :account")
    boolean isUsed(Account account);
}
