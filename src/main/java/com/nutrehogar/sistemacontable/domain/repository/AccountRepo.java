package com.nutrehogar.sistemacontable.domain.repository;

import com.nutrehogar.sistemacontable.base.domain.repository.AccountRepository;
import com.nutrehogar.sistemacontable.domain.model.Account;

public class AccountRepo extends CRUDRepo<Account, Integer> implements AccountRepository {

    public AccountRepo() {
        super(Account.class);
    }
}
