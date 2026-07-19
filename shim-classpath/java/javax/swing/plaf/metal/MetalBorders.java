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
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicBorders;

public class MetalBorders {
  public MetalBorders() { }

  private static void outline(Graphics g, String colorKey, int x, int y, int w, int h) {
    Color c = UIManager.getColor(colorKey);
    if (c == null && colorKey.equals("controlDarkShadow")) {
      c = MetalLookAndFeel.getControlDarkShadow();
    }
    if (c != null) {
      g.setColor(c);
      g.drawRect(x, y, w - 1, h - 1);
    }
  }

  public static Border getButtonBorder() {
    return new BorderUIResource.CompoundBorderUIResource(new ButtonBorder(), new BasicBorders.MarginBorder());
  }

  public static Border getToggleButtonBorder() {
    return new BorderUIResource.CompoundBorderUIResource(new ToggleButtonBorder(), new BasicBorders.MarginBorder());
  }

  public static Border getTextFieldBorder() {
    return new BorderUIResource.CompoundBorderUIResource(new TextFieldBorder(), new BasicBorders.MarginBorder());
  }

  public static Border getTextBorder() {
    return new BorderUIResource.CompoundBorderUIResource(new Flush3DBorder(), new BasicBorders.MarginBorder());
  }

  public static Border getDesktopIconBorder() {
    return new BorderUIResource.CompoundBorderUIResource(
        new javax.swing.border.LineBorder(UIManager.getColor("controlDarkShadow"), 1),
        new javax.swing.border.MatteBorder(2, 2, 1, 2, UIManager.getColor("control")));
  }

  public static class ButtonBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    protected static Insets borderInsets = new Insets(3, 3, 3, 3);
    public ButtonBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      if (!(c instanceof javax.swing.AbstractButton)) {
        return;
      }
      javax.swing.AbstractButton button = (javax.swing.AbstractButton) c;
      javax.swing.ButtonModel model = button.getModel();
      if (model.isEnabled()) {
        boolean pressed = model.isPressed() && model.isArmed();
        boolean isDefault = button instanceof javax.swing.JButton && ((javax.swing.JButton) button).isDefaultButton();
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        g.drawRect(x, y, w - 1, h - 1);
        if (pressed) {
          g.drawLine(x + 1, y + 1, x + w - 2, y + 1);
          g.drawLine(x + 1, y + 1, x + 1, y + h - 2);
        } else if (isDefault) {
          g.drawRect(x + 1, y + 1, w - 3, h - 3);
        }
      } else {
        g.setColor(MetalLookAndFeel.getControlDisabled());
        g.drawRect(x, y, w - 1, h - 1);
      }
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(3, 3, 3, 3);
      return i;
    }
  }

  public static class Flush3DBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    public Flush3DBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      if (c.isEnabled()) {
        drawFlush3D(g, x, y, w, h);
      } else {
        drawDisabled(g, x, y, w, h);
      }
    }

    static void drawFlush3D(Graphics g, int x, int y, int w, int h) {
      g.translate(x, y);
      g.setColor(MetalLookAndFeel.getControlDarkShadow());
      g.drawRect(0, 0, w - 2, h - 2);
      g.setColor(MetalLookAndFeel.getControlHighlight());
      g.drawRect(1, 1, w - 2, h - 2);
      g.setColor(MetalLookAndFeel.getControl());
      g.drawLine(0, h - 1, 1, h - 2);
      g.drawLine(w - 1, 0, w - 2, 1);
      g.translate(-x, -y);
    }

    static void drawDisabled(Graphics g, int x, int y, int w, int h) {
      g.translate(x, y);
      g.setColor(MetalLookAndFeel.getControlShadow());
      g.drawRect(0, 0, w - 1, h - 1);
      g.translate(-x, -y);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(2, 2, 2, 2);
      return i;
    }
  }

  public static class TextFieldBorder extends Flush3DBorder {
    public TextFieldBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      if (!(c instanceof javax.swing.text.JTextComponent)) {
        super.paintBorder(c, g, x, y, w, h);
        return;
      }
      if (c.isEnabled() && ((javax.swing.text.JTextComponent) c).isEditable()) {
        drawFlush3D(g, x, y, w, h);
      } else {
        drawDisabled(g, x, y, w, h);
      }
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(2, 2, 2, 2);
      return i;
    }
  }

  public static class ToggleButtonBorder extends ButtonBorder {
    public ToggleButtonBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      super.paintBorder(c, g, x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(3, 3, 3, 3);
      return i;
    }
  }

  public static class RolloverButtonBorder extends ButtonBorder {
    public RolloverButtonBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(3, 3, 3, 3);
      return i;
    }
  }

  public static class TableHeaderBorder extends javax.swing.border.AbstractBorder {
    protected Insets editorBorderInsets = new Insets(2, 2, 2, 2);
    public TableHeaderBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(2, 2, 2, 2);
      return i;
    }
  }

  public static class ScrollPaneBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    public ScrollPaneBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(1, 1, 2, 2);
      return i;
    }
  }

  public static class ToolBarBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource, javax.swing.SwingConstants {
    public ToolBarBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(1, 2, 3, 2);
      if (c instanceof javax.swing.JToolBar && ((javax.swing.JToolBar) c).isFloatable()) {
        if (((javax.swing.JToolBar) c).getOrientation() == HORIZONTAL) {
          if (c.getComponentOrientation().isLeftToRight()) {
            i.left = 16;
          } else {
            i.right = 16;
          }
        } else {
          i.top = 16;
        }
      }
      return i;
    }
  }

  public static class PopupMenuBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    protected static Insets borderInsets = new Insets(3, 1, 2, 1);
    public PopupMenuBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(3, 1, 2, 1);
      return i;
    }
  }

  public static class MenuItemBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    protected static Insets borderInsets = new Insets(2, 2, 2, 2);
    public MenuItemBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(2, 2, 2, 2);
      return i;
    }
  }

  public static class MenuBarBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    protected static Insets borderInsets = new Insets(0, 0, 2, 0);
    public MenuBarBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(0, 0, 2, 0);
      return i;
    }
  }

  public static class OptionDialogBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    public OptionDialogBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(3, 3, 3, 3);
      return i;
    }
  }

  public static class PaletteBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    public PaletteBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(1, 1, 1, 1);
      return i;
    }
  }

  public static class InternalFrameBorder extends javax.swing.border.AbstractBorder implements javax.swing.plaf.UIResource {
    public InternalFrameBorder() { }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      outline(g, "controlDarkShadow", x, y, w, h);
    }

    public Insets getBorderInsets(Component c, Insets i) {
      i.set(4, 4, 4, 4);
      return i;
    }
  }

}
