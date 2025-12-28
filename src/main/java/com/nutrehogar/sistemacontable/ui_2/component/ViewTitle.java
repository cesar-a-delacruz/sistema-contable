package com.nutrehogar.sistemacontable.ui_2.component;

import com.nutrehogar.sistemacontable.config.Theme;

import javax.swing.*;

public class ViewTitle extends JLabel {
    {
        setText("N/A");
        setVerticalTextPosition(SwingConstants.LEFT);
        setHorizontalAlignment(SwingConstants.LEADING);
        setFont(Theme.Typography.FONT_XL);
        setForeground(Theme.Palette.OFFICE_GREEN);
        setIconTextGap(Theme.Spacing.GAP_MD);
    }
}
