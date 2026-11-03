/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;

public class BasicBorders {
  public BasicBorders() { }

  private static Border compound(Border outer) {
    return new BorderUIResource.CompoundBorderUIResource(outer, new MarginBorder());
  }

  public static Border getButtonBorder() {
    return compound(new ButtonBorder(UIManager.getColor("Button.shadow"), UIManager.getColor("Button.darkShadow"),
        UIManager.getColor("Button.light"), UIManager.getColor("Button.highlight")));
  }

  public static Border getRadioButtonBorder() {
    return compound(new RadioButtonBorder(UIManager.getColor("RadioButton.shadow"),
        UIManager.getColor("RadioButton.darkShadow"), UIManager.getColor("RadioButton.light"),
        UIManager.getColor("RadioButton.highlight")));
  }

  public static Border getToggleButtonBorder() {
    return compound(new ToggleButtonBorder(UIManager.getColor("ToggleButton.shadow"),
        UIManager.getColor("ToggleButton.darkShadow"), UIManager.getColor("ToggleButton.light"),
        UIManager.getColor("ToggleButton.highlight")));
  }

  public static Border getMenuBarBorder() {
    return new MenuBarBorder(UIManager.getColor("MenuBar.shadow"), UIManager.getColor("MenuBar.highlight"));
  }

  public static Border getSplitPaneBorder() {
    return new SplitPaneBorder(UIManager.getColor("SplitPane.highlight"), UIManager.getColor("SplitPane.darkShadow"));
  }

  public static Border getSplitPaneDividerBorder() {
    return new SplitPaneDividerBorder(UIManager.getColor("SplitPane.highlight"),
        UIManager.getColor("SplitPane.darkShadow"));
  }

  public static Border getTextFieldBorder() {
    return new FieldBorder(UIManager.getColor("TextField.shadow"), UIManager.getColor("TextField.darkShadow"),
        UIManager.getColor("TextField.light"), UIManager.getColor("TextField.highlight"));
  }

  public static Border getProgressBarBorder() {
    return new BorderUIResource.LineBorderUIResource(Color.green, 2);
  }

  public static Border getInternalFrameBorder() {
    return new BorderUIResource.BevelBorderUIResource(javax.swing.border.BevelBorder.RAISED);
  }

  private static void rect(Graphics g, Color c, int x, int y, int w, int h) {
    if (c != null) {
      g.setColor(c);
      g.drawRect(x, y, w - 1, h - 1);
    }
  }

  public static class ButtonBorder extends AbstractBorder implements UIResource {
    protected Color shadow;
    protected Color darkShadow;
    protected Color highlight;
    protected Color lightHighlight;

    public ButtonBorder(Color shadow, Color darkShadow, Color highlight, Color lightHighlight) {
      this.shadow = shadow;
      this.darkShadow = darkShadow;
      this.highlight = highlight;
      this.lightHighlight = lightHighlight;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      rect(g, darkShadow, x, y, width, height);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(2, 3, 3, 3);
      return insets;
    }
  }

  public static class ToggleButtonBorder extends ButtonBorder {
    public ToggleButtonBorder(Color shadow, Color darkShadow, Color highlight, Color lightHighlight) {
      super(shadow, darkShadow, highlight, lightHighlight);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      rect(g, darkShadow, x, y, width, height);
    }
  }

  public static class RadioButtonBorder extends ButtonBorder {
    public RadioButtonBorder(Color shadow, Color darkShadow, Color highlight, Color lightHighlight) {
      super(shadow, darkShadow, highlight, lightHighlight);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(2, 2, 2, 2);
      return insets;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      if (c instanceof AbstractButton && ((AbstractButton) c).isBorderPainted()) {
        rect(g, darkShadow, x, y, width, height);
      }
    }
  }

  public static class RolloverButtonBorder extends ButtonBorder {
    public RolloverButtonBorder(Color shadow, Color darkShadow, Color highlight, Color lightHighlight) {
      super(shadow, darkShadow, highlight, lightHighlight);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover()) {
        rect(g, darkShadow, x, y, width, height);
      }
    }
  }

  public static class MarginBorder extends AbstractBorder implements UIResource {
    public MarginBorder() { }

    public Insets getBorderInsets(Component c, Insets insets) {
      Insets margin = null;
      if (c instanceof AbstractButton) {
        margin = ((AbstractButton) c).getMargin();
      } else if (c instanceof JToolBar) {
        margin = ((JToolBar) c).getMargin();
      } else if (c instanceof JTextComponent) {
        margin = ((JTextComponent) c).getMargin();
      } else if (c instanceof JMenuBar) {
        margin = ((JMenuBar) c).getMargin();
      }
      if (margin == null) {
        insets.set(0, 0, 0, 0);
      } else {
        insets.left = margin.left;
        insets.top = margin.top;
        insets.right = margin.right;
        insets.bottom = margin.bottom;
      }
      return insets;
    }
  }

  public static class MenuBarBorder extends AbstractBorder implements UIResource {
    private Color shadow;
    private Color highlight;

    public MenuBarBorder(Color shadow, Color highlight) {
      this.shadow = shadow;
      this.highlight = highlight;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      if (shadow != null) {
        g.setColor(shadow);
        g.drawLine(x, y + height - 2, x + width - 1, y + height - 2);
      }
      if (highlight != null) {
        g.setColor(highlight);
        g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
      }
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(0, 0, 2, 0);
      return insets;
    }
  }

  public static class SplitPaneBorder implements Border, UIResource {
    protected Color highlight;
    protected Color shadow;

    public SplitPaneBorder(Color highlight, Color shadow) {
      this.highlight = highlight;
      this.shadow = shadow;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      rect(g, shadow, x, y, width, height);
    }

    public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }

    public boolean isBorderOpaque() { return true; }
  }

  public static class FieldBorder extends AbstractBorder implements UIResource {
    protected Color shadow;
    protected Color darkShadow;
    protected Color highlight;
    protected Color lightHighlight;

    public FieldBorder(Color shadow, Color darkShadow, Color highlight, Color lightHighlight) {
      this.shadow = shadow;
      this.highlight = highlight;
      this.darkShadow = darkShadow;
      this.lightHighlight = lightHighlight;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      rect(g, shadow, x, y, width, height);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(2, 2, 2, 2);
      return insets;
    }
  }

  static class SplitPaneDividerBorder implements Border, UIResource {
    private final Color highlight;
    private final Color shadow;

    SplitPaneDividerBorder(Color highlight, Color shadow) {
      this.highlight = highlight;
      this.shadow = shadow;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      rect(g, shadow, x, y, width, height);
    }

    public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }

    public boolean isBorderOpaque() { return true; }
  }
}
