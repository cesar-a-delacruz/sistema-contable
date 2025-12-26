package com.nutrehogar.sistemacontable.ui;

import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AuditableEntity;

import java.util.Map;

public class EntityInfo {
    public static final Map<Class<? extends AuditableEntity>, UIEntityInfo> entityInfo = Map.of(
            Account.class, new UIEntityInfo("Libro", "Libros", true),
            AccountSubtype.class, new UIEntityInfo("Subtipo", "Subtipos", true)
    );
}
