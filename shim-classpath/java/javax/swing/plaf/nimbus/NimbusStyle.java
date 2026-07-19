/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.nimbus;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;

public final class NimbusStyle extends SynthStyle {
  public static final String LARGE_KEY = "large";
  public static final String SMALL_KEY = "small";
  public static final String MINI_KEY = "mini";
  public static final double LARGE_SCALE = 1.15;
  public static final double SMALL_SCALE = 0.857;
  public static final double MINI_SCALE = 0.714;

  private final String prefix;

  private static UIDefaults own() {
    UIDefaults d = NimbusLookAndFeel.ownDefaults();
    return d != null ? d : UIManager.getLookAndFeelDefaults();
  }

  NimbusStyle(String prefix) {
    this.prefix = prefix;
  }

  NimbusStyle() {
    this("Component");
  }

  private static String stateName(int state) {
    if ((state & SynthConstants.DISABLED) != 0) {
      return "Disabled";
    } else if ((state & SynthConstants.PRESSED) != 0) {
      return "Pressed";
    } else if ((state & SynthConstants.MOUSE_OVER) != 0) {
      return "MouseOver";
    } else if ((state & SynthConstants.SELECTED) != 0) {
      return "Selected";
    }
    return "Enabled";
  }

  public Object get(SynthContext ctx, Object key) {
    Object v = own().get(prefix + "[" + stateName(ctx.getComponentState()) + "]." + key);
    return v != null ? v : own().get(prefix + "." + key);
  }

  public boolean isOpaque(SynthContext ctx) {
    Object v = own().get(prefix + ".opaque");
    return v instanceof Boolean && ((Boolean) v).booleanValue();
  }

  public void installDefaults(SynthContext ctx) {
    super.installDefaults(ctx);
  }

  public SynthPainter getPainter(SynthContext ctx) {
    return null;
  }

  protected Color getColorForState(SynthContext ctx, ColorType type) {
    boolean disabled = (ctx.getComponentState() & SynthConstants.DISABLED) != 0;
    Color c = null;
    if (type == ColorType.BACKGROUND) {
      c = own().getColor(prefix + ".background");
      return c != null ? c : own().getColor("control");
    } else if (type == ColorType.FOREGROUND || type == ColorType.TEXT_FOREGROUND) {
      if (disabled) {
        c = own().getColor("nimbusDisabledText");
      }
      if (c == null) {
        c = own().getColor(prefix + (type == ColorType.FOREGROUND ? ".foreground" : ".textForeground"));
      }
      return c != null ? c : UIManager.getColor("text");
    } else if (type == ColorType.TEXT_BACKGROUND) {
      c = own().getColor(prefix + ".background");
      return c != null ? c : UIManager.getColor("textBackground");
    }
    return own().getColor("nimbusFocus");
  }

  protected Font getFontForState(SynthContext ctx) {
    Font f = own().getFont(prefix + ".font");
    return f != null ? f : own().getFont("defaultFont");
  }

  public Painter<Object> getBackgroundPainter(SynthContext ctx) {
    return painter("backgroundPainter", ctx);
  }

  public Painter<Object> getForegroundPainter(SynthContext ctx) {
    return painter("foregroundPainter", ctx);
  }

  public Painter<Object> getBorderPainter(SynthContext ctx) {
    return painter("borderPainter", ctx);
  }

  @SuppressWarnings("unchecked")
  private Painter<Object> painter(String kind, SynthContext ctx) {
    Object v = own().get(prefix + "[" + stateName(ctx.getComponentState()) + "]." + kind);
    if (v instanceof Painter) {
      return (Painter<Object>) v;
    }
    if (!"backgroundPainter".equals(kind)) {
      return null;
    }
    Painter<?> p = shim.swing.NimbusPaint.get(prefix, ctx.getComponentState());
    return (Painter<Object>) p;
  }

  public Insets getInsets(SynthContext ctx, Insets insets) {
    if (insets == null) {
      insets = new Insets(0, 0, 0, 0);
    }
    Insets margin = own().getInsets(prefix + ".contentMargins");
    if (margin != null) {
      insets.set(margin.top, margin.left, margin.bottom, margin.right);
    } else {
      insets.set(0, 0, 0, 0);
    }
    return insets;
  }
}
