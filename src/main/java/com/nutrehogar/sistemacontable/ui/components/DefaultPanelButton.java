package com.nutrehogar.sistemacontable.ui.components;

import com.nutrehogar.sistemacontable.ui.ThemeConfig;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class DefaultPanelButton extends JButton {
    {
        setBorderPainted(false);
        setVerticalTextPosition(SwingConstants.TOP);
        setHorizontalAlignment(SwingConstants.LEADING);
        setMargin(new Insets(4, 6, 3, 14));
        setFont(ThemeConfig.Typography.FONT_LG);
        setForeground(ThemeConfig.Palette.OFFICE_GREEN);
        setIconTextGap(ThemeConfig.Spacing.GAP_MD);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(ThemeConfig.Palette.COLUMBIA_BLUE);
            }
        });
    }

    public DefaultPanelButton() {}

    public DefaultPanelButton(String text) {
        super(text);
    }
}
