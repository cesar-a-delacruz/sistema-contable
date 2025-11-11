package com.nutrehogar.sistemacontable.base.ui.view.business;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.nutrehogar.sistemacontable.base.ui.view.BusinessView;
import com.nutrehogar.sistemacontable.domain.model.Account;
import com.nutrehogar.sistemacontable.domain.model.AccountSubtype;
import com.nutrehogar.sistemacontable.domain.type.AccountType;

public abstract class GeneralLedgerView extends BusinessView {
    public abstract JComboBox<AccountType> getCbxAccountType();

    public abstract JComboBox<AccountSubtype> getCbxAccountSubtype();

    public abstract JComboBox<Account> getCbxAccount();

    public abstract JTextField getTxtId();

    public abstract JButton getBtnSearch();

    public abstract JRadioButton getRbtSearchText();

    public abstract JRadioButton getRbtSearchFilter();
}
