package com.nutrehogar.sistemacontable.ui_2.component;

import com.nutrehogar.sistemacontable.application.config.Theme;

import javax.swing.*;
import java.awt.*;

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
