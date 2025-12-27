package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.Account;
import jakarta.persistence.criteria.CriteriaBuilder;
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

}
