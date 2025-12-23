package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.Account;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;

import java.util.List;

public interface AccountQuery extends Query {
    @HQL("select Account")
    List<Account> FindAll();

    @Find
    List<Account> findAccounts();

    @HQL("select distinct a from Account a left join fetch a.subtype")
    List<Account> findAccountsAndSubtypes();

}
