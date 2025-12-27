package com.nutrehogar.sistemacontable.query;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.User;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;

import java.util.List;
import java.util.Optional;

public interface UserQuery extends Query {

    @Find
    List<User> findAll();

    @Find
    Optional<User> findById(Integer id);

    @HQL("select distinct a from User a")
    List<User> findAccountsAndSubtypes();

}
