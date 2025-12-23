package com.nutrehogar.sistemacontable.ui;

import com.nutrehogar.sistemacontable.model.User;

public abstract class CRUDView extends SimpleView{
    protected final User user;
    public CRUDView(User user) {
        this.user = user;
    }
}
