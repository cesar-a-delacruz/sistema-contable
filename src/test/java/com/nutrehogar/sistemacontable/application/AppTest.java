package com.nutrehogar.sistemacontable.application;

import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountNumber;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void main() {

    }

    @Test
    void insertSubtypes() {
        HibernateUtil.getSessionFactory().inTransaction(session -> {
            for (var type : AccountType.values()) {
                for (int i = 1000; i < 9999; i++) {
                    session.persist(new AccountSubtype(AccountNumber.generateNumber(String.valueOf(i), type), type.getName() + " " + i, type, "Root"));
                }
            }
        });
    }

    @Test
    void inertAccounts() {
        HibernateUtil.getSessionFactory().inTransaction(session -> {
            for (var type : AccountType.values()) {
                for (int i = 1000; i < 9999; i++) {
                    session.persist(new Account(AccountNumber.generateNumber(String.valueOf(i), type), type.getName() + " " + i, type, "Root"));
                }
            }
        });
    }
}