package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.ui.crud.AccountSubtypeMinData;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;

import java.util.List;
import java.util.Optional;

public interface AccountSubtypeQuery extends Query {
//    @HQL("select AccountSubtype")
//    List<AccountSubtype> FindAll();

    @HQL("select distinct a from AccountSubtype a order by a.number asc")
    List<AccountSubtype> findAll();

//    @HQL("select distinct a from Account a left join fetch a.subtype")
//    List<AccountSubtype> findAccountsAndSubtypes();

    @Find
    Optional<AccountSubtype> findById(Integer id);

    @HQL("select distinct a from AccountSubtype a where a.type = :type order by a.number asc")
    List<AccountSubtype> findByType(AccountType type);

    @HQL("select distinct new AccountSubtypeMinData(a.id,a.name,a.number) from AccountSubtype a where a.type = :type order by a.number asc")
    List<AccountSubtypeMinData> findMinDataByType(AccountType type);
}
