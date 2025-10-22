package com.nutrehogar.sistemacontable.ui.components;

import java.awt.*;
import javax.swing.JScrollPane;

public class ScrollPanel extends JScrollPane {
    {
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        getViewport().setOpaque(false);
        setHorizontalScrollBar(null);
        setBorder(null);
    }

    public ScrollPanel() {}

    public ScrollPanel(Component view) {
        super(view);
    }
}
