/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class MetalComboBoxEditor extends BasicComboBoxEditor {
  protected static Insets editorBorderInsets = new Insets(2, 2, 2, 0);

  public MetalComboBoxEditor() {
    super();
    editor.removeFocusListener(this);
    editor = new JTextField("", 9) {
      public void setText(String s) {
        if (getText().equals(s)) {
          return;
        }
        super.setText(s);
      }

      public void setBorder(Border b) {
        if (!(b instanceof javax.swing.plaf.UIResource)) {
          super.setBorder(b);
        }
      }

      public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension d = super.getPreferredSize();
        return isPreferredSizeSet() ? d : new java.awt.Dimension(d.width, d.height + 4);
      }

      public java.awt.Dimension getMinimumSize() {
        java.awt.Dimension d = super.getMinimumSize();
        return isMinimumSizeSet() ? d : new java.awt.Dimension(d.width, d.height + 4);
      }
    };
    editor.setBorder(new EditorBorder());
    editor.addFocusListener(this);
  }

  protected static class EditorBorder extends AbstractBorder {
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      g.translate(x, y);
      if (MetalLookAndFeel.usingOcean()) {
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        g.drawRect(0, 0, w, h - 1);
        g.setColor(MetalLookAndFeel.getControlShadow());
        g.drawRect(1, 1, w - 2, h - 3);
      } else {
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        g.drawLine(0, 0, w - 1, 0);
        g.drawLine(0, 0, 0, h - 2);
        g.drawLine(0, h - 2, w - 1, h - 2);
        g.setColor(MetalLookAndFeel.getControlHighlight());
        g.drawLine(1, 1, w - 1, 1);
        g.drawLine(1, 1, 1, h - 1);
        g.drawLine(1, h - 1, w - 1, h - 1);
        g.setColor(MetalLookAndFeel.getControl());
        g.drawLine(1, h - 2, 1, h - 2);
      }
      g.translate(-x, -y);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(2, 2, 2, 0);
      return insets;
    }
  }

  public static class UIResource extends MetalComboBoxEditor implements javax.swing.plaf.UIResource {
    public UIResource() {
    }
  }
}
