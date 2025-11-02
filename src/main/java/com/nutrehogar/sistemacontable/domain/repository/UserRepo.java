package com.nutrehogar.sistemacontable.domain.repository;

import com.nutrehogar.sistemacontable.base.domain.repository.UserRepository;
import com.nutrehogar.sistemacontable.domain.model.User;

public class UserRepo extends CRUDRepo<User, Integer> implements UserRepository {
    public UserRepo() {
        super(User.class);
    }
}
