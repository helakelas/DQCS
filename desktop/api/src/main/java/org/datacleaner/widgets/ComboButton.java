/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.widgets;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.HorizontalLayout;

/**
 * Represents a set of related buttons; typically a set of toggle buttons or a
 * button with an alternate mode/dropdown selector.
 * 代表一组相关的按钮；通常是一组切换按钮或带有备用模式/下拉选择器的按钮。
 */
public class ComboButton extends JPanel {

    private static final long serialVersionUID = 1L;

    private final List<AbstractButton> _buttons;
    private final ActionListener _commonToggleButtonActionListener;

    /**
     * Constructs a {@link ComboButton}.
     */
    public ComboButton() {
        super(new HorizontalLayout(0));
        _buttons = new ArrayList<>(2);
        _commonToggleButtonActionListener = e -> {
            for (final AbstractButton button : _buttons) {
                final Object source = e.getSource();
                if (source != button) {
                    button.setSelected(false);
                }
            }

        };

        final Border border = new LineBorder(WidgetUtils.BG_COLOR_LESS_BRIGHT, 1, false);
        setBorder(border);
    }

    /**
     * Adds a button to the {@link ComboButton}.
     *
     * @param text
     *            the text of the button
     * @param toggleButton
     *            whether or not this button should be a toggle button (true) or
     *            a regular button (false)
     * @return
     */
    public AbstractButton addButton(final String text, final boolean toggleButton) {
        return addButton(text, (Icon) null, toggleButton);
    }

    /**
     * Adds a button to this {@link ComboButton}
     *
     * @param text
     *            the text of the button
     * @param icon
     *            the icon of the button
     * @param toggleButton
     *            whether or not this button should be a toggle button (true) or
     *            a regular button (false)
     * @return
     */
    public AbstractButton addButton(final String text, final Icon icon, final boolean toggleButton) {
        final AbstractButton button;
        if (toggleButton) {
            button = new JToggleButton(text, icon);
            button.addActionListener(_commonToggleButtonActionListener);
        } else {
            button = new JButton(text, icon);
        }

        addButton(button);
        return button;
    }

    /**
     * Adds a button to this {@link ComboButton}. Beware that this method does
     * change the styling (colors, borders etc.) of the button to make it fit
     * the {@link ComboButton}.
     *
     * @param button
     */
    public void addButton(final AbstractButton button) {
        WidgetUtils.setDefaultButtonStyle(button);
        final EmptyBorder baseBorder =
                new EmptyBorder(WidgetUtils.BORDER_WIDE_WIDTH - 1, 9, WidgetUtils.BORDER_WIDE_WIDTH - 1, 9);
        if (getComponentCount() == 0) {
            button.setBorder(baseBorder);
        } else {
            final Component lastComponent = getComponent(getComponentCount() - 1);
            if (lastComponent instanceof AbstractButton) {
                // previous component was also a button - add a line on the left
                // side
                final Border outsideBorder = new MatteBorder(0, 1, 0, 0, WidgetUtils.BG_COLOR_LESS_BRIGHT);
                button.setBorder(new CompoundBorder(outsideBorder, baseBorder));
            } else {
                button.setBorder(baseBorder);
            }
        }
        button.setOpaque(false);
        _buttons.add(button);

        add(button);
    }

    /**
     * Gets the currently selected toggle button, if any.
     *
     * @return
     */
    public JToggleButton getSelectedToggleButton() {
        for (final AbstractButton button : _buttons) {
            if (button instanceof JToggleButton) {
                if (button.isSelected()) {
                    return (JToggleButton) button;
                }
            }
        }
        return null;
    }

    /**
     * Adds a button to this {@link ComboButton}
     *
     * @param text
     *            the text of the button
     * @param iconImagePath
     *            the icon path of the button
     * @param toggleButton
     *            whether or not this button should be a toggle button (true) or
     *            a regular button (false)
     * @return
     */
    public AbstractButton addButton(final String text, final String iconImagePath, final boolean toggleButton) {
        final ImageIcon icon = ImageManager.get().getImageIcon(iconImagePath, IconUtils.ICON_SIZE_MEDIUM);
        return addButton(text, icon, toggleButton);
    }

    // a simple test app
    public static void main(final String[] args) {
        LookAndFeelManager.get().init();

        final ComboButton comboButton1 = new ComboButton();
        comboButton1.addButton("Foo!", IconUtils.ACTION_ADD_DARK, true);
        comboButton1.addButton("Boo!", IconUtils.ACTION_REMOVE_DARK, true);

        final ComboButton comboButton2 = new ComboButton();
        comboButton2.addButton("Foo!", IconUtils.ACTION_ADD_DARK, false);
        comboButton2.addButton("Boo!", IconUtils.ACTION_REMOVE_DARK, false);
        comboButton2.addButton("Mrr!", IconUtils.ACTION_REFRESH, true);
        comboButton2.addButton("Rrrh!", IconUtils.ACTION_DRILL_TO_DETAIL, true);

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.add(comboButton1);
        panel.add(comboButton2);

        final JButton regularButton = WidgetFactory.createDefaultButton("Regular button", IconUtils.ACTION_ADD_DARK);
        panel.add(regularButton);

        final JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
    }
}
