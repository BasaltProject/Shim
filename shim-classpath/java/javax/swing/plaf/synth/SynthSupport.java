/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import java.beans.PropertyChangeEvent;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;

final class SynthSupport {
  private SynthSupport() { }

  static int state(JComponent c) {
    int state = c.isEnabled() ? SynthConstants.ENABLED : SynthConstants.DISABLED;
    if (c.hasFocus()) {
      state |= SynthConstants.FOCUSED;
    }
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      if (b.getModel().isRollover()) {
        state |= SynthConstants.MOUSE_OVER;
      }
      if (b.getModel().isPressed() && b.getModel().isArmed()) {
        state |= SynthConstants.PRESSED;
      }
      if (b.isSelected()) {
        state |= SynthConstants.SELECTED;
      }
      if (c instanceof JButton && ((JButton) c).isDefaultButton()) {
        state |= SynthConstants.DEFAULT;
      }
    }
    return state;
  }

  static SynthContext context(JComponent c, Region region, int state) {
    return new SynthContext(c, region, SynthLookAndFeel.getStyle(c, region), state);
  }

  static void install(JComponent c, Region region) {
    SynthContext ctx = context(c, region, state(c));
    ctx.getStyle().installDefaults(ctx);
    if (c instanceof AbstractButton) {
      java.awt.Insets m = ((AbstractButton) c).getMargin();
      if (m == null || m instanceof javax.swing.plaf.UIResource) {
        ((AbstractButton) c).setMargin(new javax.swing.plaf.InsetsUIResource(0, 0, 0, 0));
      }
    }
    javax.swing.border.Border border = c.getBorder();
    if (!(c instanceof javax.swing.JViewport) && (border == null || border instanceof javax.swing.plaf.UIResource)) {
      c.setBorder(new SynthBorder(region));
    }
  }

  private static final class SynthBorder extends javax.swing.border.AbstractBorder
      implements javax.swing.plaf.UIResource {
    private final Region region;

    SynthBorder(Region region) {
      this.region = region;
    }

    public java.awt.Insets getBorderInsets(java.awt.Component c, java.awt.Insets insets) {
      if (insets == null) {
        insets = new java.awt.Insets(0, 0, 0, 0);
      }
      if (c instanceof JComponent) {
        SynthContext ctx = context((JComponent) c, region, state((JComponent) c));
        ctx.getStyle().getInsets(ctx, insets);
      } else {
        insets.set(0, 0, 0, 0);
      }
      return insets;
    }
  }

  static void uninstall(JComponent c, Region region) {
    SynthContext ctx = context(c, region, state(c));
    ctx.getStyle().uninstallDefaults(ctx);
  }

  static void propertyChange(PropertyChangeEvent e, Region region) {
    if (e.getSource() instanceof JComponent) {
      String name = e.getPropertyName();
      if ("name".equals(name) || "componentOrientation".equals(name)) {
        install((JComponent) e.getSource(), region);
      }
    }
  }

  static void update(java.awt.Graphics g, JComponent c, Region region) {
    SynthContext ctx = context(c, region, state(c));
    SynthStyle style = ctx.getStyle();
    if (c.isOpaque()) {
      g.setColor(style.getColor(ctx, ColorType.BACKGROUND));
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }
    if (style instanceof javax.swing.plaf.nimbus.NimbusStyle) {
      javax.swing.Painter<Object> p = ((javax.swing.plaf.nimbus.NimbusStyle) style).getBackgroundPainter(ctx);
      if (p != null) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        try {
          p.paint(g2, c, c.getWidth(), c.getHeight());
        } finally {
          g2.dispose();
        }
      }
    }
  }
}
