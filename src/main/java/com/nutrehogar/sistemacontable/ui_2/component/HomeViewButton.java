package com.nutrehogar.sistemacontable.ui_2.component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

import com.nutrehogar.sistemacontable.config.Theme;

public class HomeViewButton extends JButton {
    {
        setBorderPainted(false);
        setVerticalTextPosition(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.LEADING);
        setMargin(new Insets(4, 6, 3, 14));
        setFont(Theme.Typography.FONT_2XL);
        setForeground(Theme.Palette.OFFICE_GREEN);
        setIconTextGap(Theme.Spacing.GAP_MD);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(Theme.Palette.COLUMBIA_BLUE);
            }
        });
    }

    public HomeViewButton() {}

    public HomeViewButton(String text) {
        super(text);
    }
}
