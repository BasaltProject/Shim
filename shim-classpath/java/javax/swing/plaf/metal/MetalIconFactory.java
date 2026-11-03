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
import java.awt.Polygon;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

public class MetalIconFactory implements java.io.Serializable {
  public static final boolean DARK = false;
  public static final boolean LIGHT = true;

  private static Icon fileChooserDetailView;
  private static Icon fileChooserHome;
  private static Icon fileChooserList;
  private static Icon fileChooserNew;
  private static Icon fileChooserUp;
  private static Icon treeComputer;
  private static Icon treeFloppy;
  private static Icon treeFolder;
  private static Icon treeHardDrive;
  private static Icon treeLeaf;
  private static Icon treeControlLight;
  private static Icon treeControlDark;
  private static Icon checkBox;
  private static Icon radio;
  private static Icon menuArrow;
  private static Icon menuItemArrow;
  private static Icon checkBoxMenuItem;
  private static Icon radioMenuItem;
  private static Icon hThumb;
  private static Icon vThumb;
  private static Icon ifMenu;

  public MetalIconFactory() {
  }

  private static Color col(String key, Color fallback) {
    Color c = UIManager.getColor(key);
    return c == null ? fallback : c;
  }

  private abstract static class Boxed implements Icon, UIResource, java.io.Serializable {
    final int w;
    final int h;

    Boxed(int w, int h) {
      this.w = w;
      this.h = h;
    }

    public int getIconWidth() { return w; }

    public int getIconHeight() { return h; }
  }

  private static final class Arrow extends Boxed {
    final boolean paints;

    Arrow(boolean paints) {
      super(4, 8);
      this.paints = paints;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (!paints) {
        return;
      }
      boolean ltr = c == null || c.getComponentOrientation().isLeftToRight();
      g.translate(x, y);
      g.setColor(col(c != null && c.isEnabled() ? "controlInfo" : "controlDisabled", Color.BLACK));
      if (ltr) {
        g.drawLine(0, 0, 0, 7);
        g.drawLine(1, 1, 1, 6);
        g.drawLine(2, 2, 2, 5);
        g.drawLine(3, 3, 3, 4);
      } else {
        g.drawLine(3, 0, 3, 7);
        g.drawLine(2, 1, 2, 6);
        g.drawLine(1, 2, 1, 5);
        g.drawLine(0, 3, 0, 4);
      }
      g.translate(-x, -y);
    }
  }

  private static final int[] kCheckMark = {9, 3, 8, 4, 9, 4, 3, 5, 4, 5, 7, 5, 8, 5, 3, 6, 4, 6, 6, 6, 7, 6, 3, 7, 4, 7,
      5, 7, 6, 7, 3, 8, 4, 8, 5, 8, 3, 9, 4, 9};

  private static final class CheckBoxIcon extends Boxed {
    CheckBoxIcon() {
      super(13, 13);
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      ButtonModel model = c instanceof AbstractButton ? ((AbstractButton) c).getModel() : null;
      boolean enabled = model == null || model.isEnabled();
      g.translate(x, y);
      Color dark = MetalLookAndFeel.getControlDarkShadow();
      if (enabled) {
        if (model != null && model.isPressed() && model.isArmed()) {
          g.setColor(shim.swing.MetalPaint.lastColor("CheckBox.gradient", dark));
          g.fillRect(0, 0, 13, 13);
          g.setColor(dark);
          g.drawRect(0, 0, 12, 12);
          g.drawLine(1, 1, 11, 1);
          g.drawLine(1, 1, 1, 11);
        } else {
          shim.swing.MetalPaint.drawGradient(c, g, "CheckBox.gradient", 0, 0, 13, 13, true);
          g.setColor(dark);
          g.drawRect(0, 0, 12, 12);
        }
      } else {
        g.setColor(dark);
        g.drawRect(0, 0, 12, 12);
      }
      if (model != null && model.isSelected()) {
        g.setColor(enabled ? col("CheckBox.foreground", Color.BLACK) : dark);
        for (int i = 0; i < kCheckMark.length; i += 2) {
          g.fillRect(kCheckMark[i], kCheckMark[i + 1], 1, 1);
        }
      }
      g.translate(-x, -y);
    }
  }

  private static final class RadioButtonIcon extends Boxed {
    RadioButtonIcon() {
      super(13, 13);
    }

    private static final int[] kInnerStart = {-1, 4, 2, 2, 1, 1, 1, 1, 2, 2, 4, -1};
    private static final int[] kInnerEnd = {-1, 7, 9, 9, 10, 10, 10, 10, 9, 9, 7, -1};
    private static final int[] kRingLeft = {4, 2, 1, 1, 0, 0, 0, 0, 1, 1, 2, 4};

    public void paintIcon(Component c, Graphics g, int x, int y) {
      ButtonModel model = c instanceof AbstractButton ? ((AbstractButton) c).getModel() : null;
      boolean enabled = model == null || model.isEnabled();
      boolean pressed = enabled && model != null && model.isPressed() && model.isArmed();
      g.translate(x, y);
      Color dark = MetalLookAndFeel.getControlDarkShadow();
      Color ring = enabled ? dark : MetalLookAndFeel.getControlDisabled();
      for (int ry = 0; ry < 12; ry++) {
        int right = 11 - kRingLeft[ry];
        if (ry == 0 || ry == 11) {
          g.setColor(ring);
          g.fillRect(4, ry, 4, 1);
          continue;
        }
        if (enabled && !pressed) {
          g.setColor(shim.swing.MetalPaint.gradientColor("RadioButton.gradient", ry - 1, 10));
        } else if (pressed) {
          g.setColor(shim.swing.MetalPaint.lastColor("RadioButton.gradient", dark));
        } else {
          g.setColor(MetalLookAndFeel.getControl());
        }
        g.fillRect(kInnerStart[ry], ry, kInnerEnd[ry] - kInnerStart[ry] + 1, 1);
        g.setColor(ring);
        if (ry == 1 || ry == 10) {
          g.fillRect(2, ry, 2, 1);
          g.fillRect(8, ry, 2, 1);
          if (enabled) {
            g.setColor(MetalLookAndFeel.getControl());
            g.fillRect(1, ry, 1, 1);
            g.fillRect(10, ry, 1, 1);
          } else {
            g.setColor(MetalLookAndFeel.getControl());
            g.fillRect(3, ry, 6, 1);
          }
        } else {
          g.fillRect(kRingLeft[ry], ry, 1, 1);
          g.fillRect(right, ry, 1, 1);
          if (!enabled && kInnerStart[ry] > kRingLeft[ry] + 1) {
            g.setColor(MetalLookAndFeel.getControl());
            g.fillRect(kRingLeft[ry] + 1, ry, kInnerStart[ry] - kRingLeft[ry] - 1, 1);
            g.fillRect(kInnerEnd[ry] + 1, ry, kInnerStart[ry] - kRingLeft[ry] - 1, 1);
          }
        }
      }
      if (model != null && model.isSelected()) {
        g.setColor(enabled ? col("RadioButton.foreground", Color.BLACK) : dark);
        g.fillRect(4, 3, 4, 1);
        g.fillRect(3, 4, 6, 4);
        g.fillRect(4, 8, 4, 1);
      }
      g.translate(-x, -y);
    }
  }

  private static final class MenuCheckIcon extends Boxed {
    final boolean radio;

    MenuCheckIcon(boolean radio) {
      super(10, 10);
      this.radio = radio;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      ButtonModel model = c instanceof AbstractButton ? ((AbstractButton) c).getModel() : null;
      if (model == null || !model.isSelected()) {
        return;
      }
      g.setColor(col(model.isEnabled() ? "controlInfo" : "controlDisabled", Color.BLACK));
      if (radio) {
        g.fillOval(x + 2, y + 2, 6, 6);
      } else {
        g.drawLine(x + 2, y + 5, x + 2, y + 8);
        g.drawLine(x + 3, y + 5, x + 3, y + 8);
        g.drawLine(x + 4, y + 7, x + 8, y + 2);
        g.drawLine(x + 4, y + 8, x + 8, y + 3);
      }
    }
  }

  private static final class Glyph extends Boxed {
    final int kind;

    Glyph(int w, int h, int kind) {
      super(w, h);
      this.kind = kind;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(col("controlDarkShadow", Color.GRAY));
      g.drawRect(x + 1, y + 1, w - 3, h - 3);
      g.setColor(col("controlInfo", Color.BLACK));
      switch (kind) {
        case 1:
          g.drawLine(x + 4, y + 4, x + w - 5, y + h - 5);
          g.drawLine(x + 4, y + h - 5, x + w - 5, y + 4);
          break;
        case 2:
          g.drawRect(x + 4, y + 4, w - 9, h - 9);
          break;
        case 3:
          g.drawLine(x + 4, y + h - 5, x + w - 5, y + h - 5);
          break;
        default:
          g.drawRect(x + 5, y + 4, w - 9, h - 9);
          break;
      }
    }
  }

  private static final class Thumb extends Boxed {
    final boolean horizontal;

    Thumb(boolean horizontal) {
      super(horizontal ? 15 : 16, horizontal ? 16 : 15);
      this.horizontal = horizontal;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      boolean enabled = c == null || c.isEnabled();
      g.translate(x, y);
      g.setColor(col(enabled ? "Slider.thumb" : "controlShadow", Color.LIGHT_GRAY));
      g.fillOval(0, 0, horizontal ? 14 : 15, horizontal ? 15 : 14);
      g.setColor(col(enabled ? "controlDarkShadow" : "controlShadow", Color.GRAY));
      g.drawOval(0, 0, horizontal ? 14 : 15, horizontal ? 15 : 14);
      g.translate(-x, -y);
    }
  }

  private static final class FileLike extends Boxed {
    final int kind;

    FileLike(int w, int h, int kind) {
      super(w, h);
      this.kind = kind;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(col("controlDarkShadow", Color.GRAY));
      if (kind == 1) {
        g.drawRect(x + 2, y + 4, w - 6, h - 8);
        g.drawLine(x + 2, y + 4, x + 6, y + 2);
      } else if (kind == 2) {
        g.drawRect(x + 2, y + 1, w - 6, h - 3);
        g.setColor(col("controlInfo", Color.BLACK));
        g.drawLine(x + 4, y + 5, x + w - 5, y + 5);
      } else {
        g.drawRect(x, y + 1, w - 2, h - 3);
        g.fillRect(x + 3, y + h - 6, w - 6, 3);
      }
    }
  }

  private static final class FrameButton extends Boxed {
    final int kind;

    FrameButton(int size, int kind) {
      super(size, size);
      this.kind = kind;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(col("controlInfo", Color.BLACK));
      int m = w / 4;
      switch (kind) {
        case 0:
          g.drawLine(x + m, y + m, x + w - m - 1, y + h - m - 1);
          g.drawLine(x + m, y + h - m - 1, x + w - m - 1, y + m);
          break;
        case 1:
          g.drawRect(x + m, y + m, w - 2 * m - 1, h - 2 * m - 1);
          break;
        case 2:
          g.drawRect(x + m, y + m + 1, w - 2 * m - 2, h - 2 * m - 2);
          break;
        default:
          g.drawLine(x + m, y + h - m - 1, x + w - m - 1, y + h - m - 1);
          break;
      }
    }
  }

  public static Icon getMenuItemCheckIcon() {
    return null;
  }

  public static synchronized Icon getMenuItemArrowIcon() {
    if (menuItemArrow == null) {
      menuItemArrow = new Arrow(false);
    }
    return menuItemArrow;
  }

  public static synchronized Icon getMenuArrowIcon() {
    if (menuArrow == null) {
      menuArrow = new Arrow(true);
    }
    return menuArrow;
  }

  public static synchronized Icon getCheckBoxIcon() {
    if (checkBox == null) {
      checkBox = new CheckBoxIcon();
    }
    return checkBox;
  }

  public static synchronized Icon getRadioButtonIcon() {
    if (radio == null) {
      radio = new RadioButtonIcon();
    }
    return radio;
  }

  public static synchronized Icon getCheckBoxMenuItemIcon() {
    if (checkBoxMenuItem == null) {
      checkBoxMenuItem = new MenuCheckIcon(false);
    }
    return checkBoxMenuItem;
  }

  public static synchronized Icon getRadioButtonMenuItemIcon() {
    if (radioMenuItem == null) {
      radioMenuItem = new MenuCheckIcon(true);
    }
    return radioMenuItem;
  }

  public static synchronized Icon getFileChooserDetailViewIcon() {
    if (fileChooserDetailView == null) {
      fileChooserDetailView = new shim.swing.FileIcons(shim.swing.FileIcons.kDetails);
    }
    return fileChooserDetailView;
  }

  public static synchronized Icon getFileChooserHomeFolderIcon() {
    if (fileChooserHome == null) {
      fileChooserHome = new shim.swing.FileIcons(shim.swing.FileIcons.kHome);
    }
    return fileChooserHome;
  }

  public static synchronized Icon getFileChooserListViewIcon() {
    if (fileChooserList == null) {
      fileChooserList = new shim.swing.FileIcons(shim.swing.FileIcons.kList);
    }
    return fileChooserList;
  }

  public static synchronized Icon getFileChooserNewFolderIcon() {
    if (fileChooserNew == null) {
      fileChooserNew = new shim.swing.FileIcons(shim.swing.FileIcons.kNewFolder);
    }
    return fileChooserNew;
  }

  public static synchronized Icon getFileChooserUpFolderIcon() {
    if (fileChooserUp == null) {
      fileChooserUp = new shim.swing.FileIcons(shim.swing.FileIcons.kUpFolder);
    }
    return fileChooserUp;
  }

  public static Icon getInternalFrameAltMaximizeIcon(int size) {
    return new FrameButton(size, 2);
  }

  public static Icon getInternalFrameCloseIcon(int size) {
    return new FrameButton(size, 0);
  }

  public static synchronized Icon getInternalFrameDefaultMenuIcon() {
    if (ifMenu == null) {
      ifMenu = new FileLike(16, 16, 0);
    }
    return ifMenu;
  }

  public static Icon getInternalFrameMaximizeIcon(int size) {
    return new FrameButton(size, 1);
  }

  public static Icon getInternalFrameMinimizeIcon(int size) {
    return new FrameButton(size, 3);
  }

  public static synchronized Icon getTreeComputerIcon() {
    if (treeComputer == null) {
      treeComputer = new shim.swing.FileIcons(shim.swing.FileIcons.kComputer);
    }
    return treeComputer;
  }

  public static synchronized Icon getTreeFloppyDriveIcon() {
    if (treeFloppy == null) {
      treeFloppy = new shim.swing.FileIcons(shim.swing.FileIcons.kFloppy);
    }
    return treeFloppy;
  }

  public static synchronized Icon getTreeFolderIcon() {
    if (treeFolder == null) {
      treeFolder = new TreeFolderIcon();
    }
    return treeFolder;
  }

  public static synchronized Icon getTreeHardDriveIcon() {
    if (treeHardDrive == null) {
      treeHardDrive = new shim.swing.FileIcons(shim.swing.FileIcons.kHardDrive);
    }
    return treeHardDrive;
  }

  public static synchronized Icon getTreeLeafIcon() {
    if (treeLeaf == null) {
      treeLeaf = new TreeLeafIcon();
    }
    return treeLeaf;
  }

  public static synchronized Icon getTreeControlIcon(boolean isCollapsed) {
    if (isCollapsed) {
      if (treeControlLight == null) {
        treeControlLight = new TreeControlIcon(true);
      }
      return treeControlLight;
    }
    if (treeControlDark == null) {
      treeControlDark = new TreeControlIcon(false);
    }
    return treeControlDark;
  }

  public static synchronized Icon getHorizontalSliderThumbIcon() {
    if (hThumb == null) {
      hThumb = new Thumb(true);
    }
    return hThumb;
  }

  public static synchronized Icon getVerticalSliderThumbIcon() {
    if (vThumb == null) {
      vThumb = new Thumb(false);
    }
    return vThumb;
  }

  public static class TreeFolderIcon extends FolderIcon16 {
    public TreeFolderIcon() {
    }

    public int getShift() {
      return -1;
    }

    public int getAdditionalHeight() {
      return 2;
    }
  }

  public static class TreeLeafIcon extends FileIcon16 {
    public TreeLeafIcon() {
    }

    public int getShift() {
      return 2;
    }

    public int getAdditionalHeight() {
      return 4;
    }
  }

  public static class TreeControlIcon implements Icon, java.io.Serializable {
    protected boolean isLight;
    private boolean collapsed;

    public TreeControlIcon(boolean isCollapsed) {
      this.collapsed = isCollapsed;
      this.isLight = false;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      paintMe(c, g, x, y);
    }

    public int getIconWidth() {
      return 18;
    }

    public int getIconHeight() {
      return 18;
    }

    public void paintMe(Component c, Graphics g, int x, int y) {
      g.setColor(col("Tree.line", Color.GRAY));
      int cx = x + 9;
      int cy = y + 9;
      g.drawRect(cx - 4, cy - 4, 8, 8);
      g.drawLine(cx - 2, cy, cx + 2, cy);
      if (collapsed) {
        g.drawLine(cx, cy - 2, cx, cy + 2);
      }
    }
  }

  public static class FileIcon16 implements Icon, java.io.Serializable {
    public FileIcon16() {
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      int top = y + getShift();
      g.setColor(col("controlDarkShadow", Color.GRAY));
      g.drawRect(x + 2, top + 1, 11, 13);
      g.setColor(col("controlHighlight", Color.WHITE));
      g.fillRect(x + 3, top + 2, 10, 12);
    }

    public int getIconWidth() {
      return 16;
    }

    public int getIconHeight() {
      return 16 + getAdditionalHeight();
    }

    public int getShift() {
      return 0;
    }

    public int getAdditionalHeight() {
      return 0;
    }
  }

  public static class FolderIcon16 implements Icon, java.io.Serializable {
    public FolderIcon16() {
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      int top = y + getShift();
      g.setColor(col("controlDarkShadow", Color.GRAY));
      g.drawRect(x + 1, top + 4, 13, 10);
      g.drawLine(x + 1, top + 4, x + 5, top + 2);
      g.setColor(col("controlHighlight", Color.WHITE));
      g.fillRect(x + 2, top + 5, 12, 9);
    }

    public int getIconWidth() {
      return 16;
    }

    public int getIconHeight() {
      return 16 + getAdditionalHeight();
    }

    public int getShift() {
      return 0;
    }

    public int getAdditionalHeight() {
      return 0;
    }
  }

  public static class PaletteCloseIcon implements Icon, UIResource, java.io.Serializable {
    public PaletteCloseIcon() {
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(col("controlInfo", Color.BLACK));
      g.drawLine(x, y, x + 6, y + 6);
      g.drawLine(x, y + 6, x + 6, y);
    }

    public int getIconWidth() {
      return 7;
    }

    public int getIconHeight() {
      return 7;
    }
  }
}
