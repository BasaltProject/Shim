/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.metal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDesktopIconUI;

public class MetalDesktopIconUI extends BasicDesktopIconUI {
  private JButton button;

  public MetalDesktopIconUI() { }

  public static ComponentUI createUI(JComponent c) {
    return new MetalDesktopIconUI();
  }

  protected void installDefaults() {
    super.installDefaults();
    LookAndFeel.installColorsAndFont(desktopIcon, "DesktopIcon.background", "DesktopIcon.foreground",
        "DesktopIcon.font");
  }

  protected void installComponents() {
    frame = desktopIcon.getInternalFrame();
    Icon icon = frame.getFrameIcon();
    if (icon == null) {
      icon = javax.swing.UIManager.getIcon("InternalFrame.icon");
    }
    String title = frame.getTitle();
    button = new JButton(title, icon);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deiconize();
      }
    });
    button.setFont(desktopIcon.getFont());
    button.setBackground(desktopIcon.getBackground());
    button.setForeground(desktopIcon.getForeground());
    int buttonH = button.getPreferredSize().height;
    final int dragWidth = buttonH / 3;
    final int dragHeight = buttonH;
    Icon drag = new Icon() {
      public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        for (int row = 0; row + 1 < dragHeight; row += 2) {
          for (int col = 0; col + 1 < dragWidth; col += 2) {
            g.fillRect(x + col, y + row, 1, 1);
          }
        }
      }

      public int getIconWidth() {
        return dragWidth;
      }

      public int getIconHeight() {
        return dragHeight;
      }
    };
    javax.swing.JLabel dragLabel = new javax.swing.JLabel(drag);
    dragLabel.addMouseMotionListener(createMouseInputListener());
    dragLabel.addMouseListener(createMouseInputListener());
    desktopIcon.setLayout(new BorderLayout(2, 0));
    desktopIcon.add(button, BorderLayout.CENTER);
    desktopIcon.add(dragLabel, BorderLayout.WEST);
  }

  protected void uninstallComponents() {
    desktopIcon.setLayout(null);
    desktopIcon.removeAll();
    button = null;
    frame = null;
  }

  protected void installListeners() {
    super.installListeners();
    desktopIcon.getInternalFrame().addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (button != null && "title".equals(e.getPropertyName())) {
          button.setText((String) e.getNewValue());
        }
      }
    });
  }

  protected void uninstallListeners() {
    super.uninstallListeners();
  }

  public Dimension getPreferredSize(JComponent c) {
    return desktopIcon.getLayout().preferredLayoutSize(desktopIcon);
  }

  public Dimension getMinimumSize(JComponent c) {
    return new Dimension(getPreferredSize(c));
  }

  public Dimension getMaximumSize(JComponent c) {
    return getMinimumSize(c);
  }
}
