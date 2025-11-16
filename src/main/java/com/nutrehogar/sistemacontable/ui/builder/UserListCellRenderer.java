package com.nutrehogar.sistemacontable.ui.builder;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.nutrehogar.sistemacontable.application.config.Theme;
import com.nutrehogar.sistemacontable.domain.model.User;

import java.awt.*;
import javax.swing.*;

public class UserListCellRenderer extends DefaultListCellRenderer {
    private static final FlatSVGIcon userIcon = new FlatSVGIcon("svgs/user.svg", Theme.ICON_MD,
            Theme.ICON_MD);
    private static final FlatSVGIcon userDisableIcon = new FlatSVGIcon("svgs/user_disable.svg", Theme.ICON_MD,
            Theme.ICON_MD);

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof User user) {
            setText(user.getUsername());
            setIcon(user.isEnable() ? userIcon : userDisableIcon);
            setForeground(user.isEnable() ? Theme.Palette.OFFICE_GREEN : Theme.Palette.PLATINUM);
            setBackground(isSelected ? Theme.Palette.URANIAN_BLUE
                    : user.isEnable() ? Color.WHITE : Theme.Palette.WHITE_SMOKE);
            setFont(Theme.Typography.FONT_LG);
            setIconTextGap(Theme.Spacing.GAP_MD);
            setBorder(Theme.Spacing.BORDER_MD);
        }
        return this;
    }
}