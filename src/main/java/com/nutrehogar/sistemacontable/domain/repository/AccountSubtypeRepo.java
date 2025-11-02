package com.nutrehogar.sistemacontable.domain.repository;

import com.nutrehogar.sistemacontable.base.domain.repository.AccountSubtypeRepository;
import com.nutrehogar.sistemacontable.domain.model.AccountSubtype;
import com.nutrehogar.sistemacontable.domain.type.AccountType;
import com.nutrehogar.sistemacontable.exception.RepositoryException;
import com.nutrehogar.sistemacontable.persistence.TransactionManager;

import java.util.List;

public class AccountSubtypeRepo extends CRUDRepo<AccountSubtype, Integer>
        implements AccountSubtypeRepository {

    public AccountSubtypeRepo() {
        super(AccountSubtype.class);
    }

    @Override
    public List<AccountSubtype> findAllByAccountType(AccountType accountType) throws RepositoryException {
        return TransactionManager.executeInTransaction(session -> session.createQuery(
                "FROM AccountSubtype WHERE accountType = :accountType ORDER BY id",
                AccountSubtype.class)
                .setParameter("accountType", accountType)
                .list());
    }
}