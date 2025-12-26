package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;
import org.hibernate.annotations.processing.Find;

import java.util.List;
import java.util.Optional;
public interface AccountSubtypeQuery extends Query {
//    @HQL("select AccountSubtype")
//    List<AccountSubtype> FindAll();

    @Find
    List<AccountSubtype> findAll();

//    @HQL("select distinct a from Account a left join fetch a.subtype")
//    List<AccountSubtype> findAccountsAndSubtypes();

    @Find
    Optional<AccountSubtype> findById(Integer id);

    @Find
    List<AccountSubtype> findByType(AccountType type);
}
