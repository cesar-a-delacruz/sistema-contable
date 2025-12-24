package com.nutrehogar.sistemacontable.ui;

import com.nutrehogar.sistemacontable.model.AuditableEntity;
import com.nutrehogar.sistemacontable.model.User;
import com.nutrehogar.sistemacontable.ui_2.builder.CustomTableCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Optional;

public abstract class CRUDView<Entity extends AuditableEntity, ID> extends SimpleView<Entity> {
    public CRUDView(User user) {
        super(user);
    }

    protected void configureTable(@NotNull JTable tblData){
        tblData.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        tblData.setDefaultRenderer(BigDecimal.class, new CustomTableCellRenderer());
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tblData.rowAtPoint(e.getPoint());
                if (row != -1) {
                    int selectedRow = tblData.getSelectedRow();
                    if (selectedRow < 0) {
//                        deselect();
                        return;
                    }
                    selected= Optional.of(data.get(selectedRow));
//                    setAuditoria();
                }
            }
        });
    }


}
