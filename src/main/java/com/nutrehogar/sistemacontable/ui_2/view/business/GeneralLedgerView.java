package com.nutrehogar.sistemacontable.ui_2.view.business;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.nutrehogar.sistemacontable.ui_2.view.BusinessView;
import com.nutrehogar.sistemacontable.model.Account;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;

public abstract class GeneralLedgerView extends BusinessView {
    public abstract JComboBox<AccountType> getCbxAccountType();

    public abstract JComboBox<AccountSubtype> getCbxAccountSubtype();

    public abstract JComboBox<Account> getCbxAccount();

    public abstract JTextField getTxtId();

    public abstract JButton getBtnSearch();

    public abstract JRadioButton getRbtSearchText();

    public abstract JRadioButton getRbtSearchFilter();
}
