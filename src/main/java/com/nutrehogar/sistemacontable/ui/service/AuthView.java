package com.nutrehogar.sistemacontable.ui.service;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.nutrehogar.sistemacontable.HibernateUtil;
import com.nutrehogar.sistemacontable.application.config.PasswordHasher;
import com.nutrehogar.sistemacontable.exception.ApplicationException;
import com.nutrehogar.sistemacontable.model.AccountSubtype;
import com.nutrehogar.sistemacontable.model.AccountType;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.query.AccountSubtypeQuery_;
import com.nutrehogar.sistemacontable.query.UserQuery;
import com.nutrehogar.sistemacontable.query.UserQuery_;
import com.nutrehogar.sistemacontable.ui_2.builder.UserListCellRenderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class AuthView extends JDialog {
    @NotNull
    private static final FlatSVGIcon icon = new FlatSVGIcon("svgs/key.svg");

    @NotNull
    private final DefaultListModel<User> userListModel;

    @NotNull
    private final User adminUser;

    @Getter
    private User autenicateUser;

    public AuthView(@NotNull Frame owner, boolean modal, @NotNull User adminUser) {
        super(owner, modal);
        this.adminUser = adminUser;
        this.userListModel = new DefaultListModel<>();
        initComponents();
        new UserListDataLoader().execute();

        setIconImage(icon.getImage());
        lblPing.setIcon(icon);
        lstUser.setCellRenderer(new UserListCellRenderer());
        lstUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        String cancelName = "cancel";

        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);

        ActionMap actionMap = getRootPane().getActionMap();

        actionMap.put(cancelName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeDialog(null);
            }
        });

        BtnCancel.addActionListener(_ -> {
            log.info("System.exit");
            System.exit(0);
        });

        lstUser.addListSelectionListener(_-> txtPing.grabFocus());

        txtPing.addActionListener(_->btnOk.doClick());


        btnOk.addActionListener(_ -> {
            if (userListModel.isEmpty()) return;

            var selectedUser = lstUser.getSelectedValue();

            if(selectedUser == null) return;

            if(selectedUser.isAdmin()
                    && selectedUser.getUsername().equals(adminUser.getUsername())
                    && selectedUser.getPassword().equals(adminUser.getPassword())) {
                autenicateUser = adminUser;
                setVisible(false);
                dispose();
                IO.println("bien termino-admin");
                return;
            }

            if(!PasswordHasher.verifyPassword(String.valueOf(txtPing.getPassword()),  selectedUser.getPassword())){
                showError("Contraseña Incorrecta.", new ApplicationException("incorrect Password"));
                return;
            }

            IO.println("bien termino");

            autenicateUser = selectedUser;
            setVisible(false);
            dispose();
        });

    }
    protected class UserListDataLoader extends SwingWorker<List<User>, Void>{
        @Override
        protected List<User> doInBackground() {
            AtomicReference<List<User>> list = new AtomicReference<>(List.of());
            try{
                HibernateUtil.getSessionFactory().inTransaction(session -> list.set(new UserQuery_(session).findAll()));
            } catch (Exception e) {
                showError("Error al obtener los subtipos de cuentas", e);
            }
            return list.get();
        }

        @Override
        protected void done() {
            try {
                userListModel.removeAllElements();
                userListModel.addAll(get());
                userListModel.addElement(adminUser);
                if (!userListModel.isEmpty()) {
                    lstUser.setSelectedIndex(0);
                    btnOk.setEnabled(true);
                }
            } catch (Exception e) {
                showError("Error al cargar datos de usuario",
                        new ApplicationException("Failure to find all users.", e));
            }
        }
    }
    void showError(@NotNull String message, @Nullable Exception cause) {
        if (cause != null)
            log.error(cause.getMessage(), cause);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnOk = new javax.swing.JButton();
        BtnCancel = new javax.swing.JButton();
        scrollPanel1 = new com.nutrehogar.sistemacontable.ui_2.component.ScrollPanel();
        lstUser = new javax.swing.JList<>();
        lblTitle = new javax.swing.JLabel();
        txtPing = new javax.swing.JPasswordField();
        lblPing = new javax.swing.JLabel();

        setTitle("Ingrese su PING");
        setIconImage(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        btnOk.setText("OK");

        BtnCancel.setText("Cancelar");

        lstUser.setModel(userListModel);
        scrollPanel1.setViewportView(lstUser);

        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("Usuarios");

        lblPing.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPing.setText("Ping:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BtnCancel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblPing)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPing)))
                        .addGap(13, 13, 13)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {BtnCancel, btnOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPing))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnCancel)
                    .addComponent(btnOk))
                .addContainerGap())
        );

        getRootPane().setDefaultButton(btnOk);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_closeDialog
        System.exit(0);
    }// GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel lblPing;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JList<User> lstUser;
    private com.nutrehogar.sistemacontable.ui_2.component.ScrollPanel scrollPanel1;
    private javax.swing.JPasswordField txtPing;
    // End of variables declaration//GEN-END:variables

}
