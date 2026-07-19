/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.plaf.UIResource;

public abstract class SynthStyle {
  private static final SynthGraphicsUtils kGraphicsUtils = new SynthGraphicsUtils();

  public SynthStyle() { }

  public SynthGraphicsUtils getGraphicsUtils(SynthContext context) {
    return kGraphicsUtils;
  }

  public Color getColor(SynthContext context, ColorType type) {
    JComponent c = context.getComponent();
    if ((context.getComponentState() & SynthConstants.DISABLED) == 0 && !context.getRegion().isSubregion()) {
      if (type == ColorType.BACKGROUND) {
        Color bg = c.getBackground();
        if (bg != null && !(bg instanceof UIResource)) {
          return bg;
        }
      } else if (type == ColorType.FOREGROUND || type == ColorType.TEXT_FOREGROUND) {
        Color fg = c.getForeground();
        if (fg != null && !(fg instanceof UIResource)) {
          return fg;
        }
      }
    }
    return getColorForState(context, type);
  }

  protected abstract Color getColorForState(SynthContext context, ColorType type);

  public Font getFont(SynthContext context) {
    JComponent c = context.getComponent();
    if ((context.getComponentState() & SynthConstants.ENABLED) != 0 && !context.getRegion().isSubregion()) {
      return c.getFont();
    }
    return getFontForState(context);
  }

  protected abstract Font getFontForState(SynthContext context);

  public Insets getInsets(SynthContext context, Insets insets) {
    if (insets == null) {
      insets = new Insets(0, 0, 0, 0);
    }
    insets.top = insets.bottom = insets.left = insets.right = 0;
    return insets;
  }

  public SynthPainter getPainter(SynthContext context) {
    return null;
  }

  public boolean isOpaque(SynthContext context) {
    return false;
  }

  public Object get(SynthContext context, Object key) {
    return null;
  }

  public boolean getBoolean(SynthContext context, Object key, boolean defaultValue) {
    Object value = get(context, key);
    return value instanceof Boolean ? ((Boolean) value).booleanValue() : defaultValue;
  }

  public int getInt(SynthContext context, Object key, int defaultValue) {
    Object value = get(context, key);
    return value instanceof Number ? ((Number) value).intValue() : defaultValue;
  }

  public String getString(SynthContext context, Object key, String defaultValue) {
    Object value = get(context, key);
    return value instanceof String ? (String) value : defaultValue;
  }

  public Icon getIcon(SynthContext context, Object key) {
    Object value = get(context, key);
    return value instanceof Icon ? (Icon) value : null;
  }

  public void installDefaults(SynthContext context) {
    if (!context.getRegion().isSubregion()) {
      JComponent c = context.getComponent();
      Font font = c.getFont();
      if (font == null || font instanceof UIResource) {
        c.setFont(getFontForState(context));
      }
      Color background = c.getBackground();
      if (background == null || background instanceof UIResource) {
        c.setBackground(getColorForState(context, ColorType.BACKGROUND));
      }
      Color foreground = c.getForeground();
      if (foreground == null || foreground instanceof UIResource) {
        c.setForeground(getColorForState(context, ColorType.FOREGROUND));
      }
      LookAndFeel.installProperty(c, "opaque", Boolean.valueOf(isOpaque(context)));
    }
  }

  public void uninstallDefaults(SynthContext context) {
    if (!context.getRegion().isSubregion()) {
      JComponent c = context.getComponent();
      Font font = c.getFont();
      if (font instanceof UIResource) {
        c.setFont(null);
      }
      Color background = c.getBackground();
      if (background instanceof UIResource) {
        c.setBackground(null);
      }
      Color foreground = c.getForeground();
      if (foreground instanceof UIResource) {
        c.setForeground(null);
      }
    }
  }
}
