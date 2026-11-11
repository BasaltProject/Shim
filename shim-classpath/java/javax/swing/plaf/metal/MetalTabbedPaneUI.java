/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class MetalTabbedPaneUI extends BasicTabbedPaneUI {
  protected int minTabWidth = 40;
  private Color unselectedBackground;
  protected Color tabAreaBackground;
  protected Color selectColor;
  protected Color selectHighlight;
  private boolean tabsOpaque = true;

  public MetalTabbedPaneUI() {
  }

  public static ComponentUI createUI(JComponent x) {
    return new MetalTabbedPaneUI();
  }

  protected LayoutManager createLayoutManager() {
    return new TabbedPaneLayout();
  }

  protected void installDefaults() {
    super.installDefaults();
    tabAreaBackground = UIManager.getColor("TabbedPane.tabAreaBackground");
    selectColor = UIManager.getColor("TabbedPane.selected");
    selectHighlight = UIManager.getColor("TabbedPane.selectHighlight");
    tabsOpaque = UIManager.getBoolean("TabbedPane.tabsOpaque");
    unselectedBackground = UIManager.getColor("TabbedPane.unselectedBackground");
  }

  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
      boolean isSelected) {
    int bottom = y + (h - 1);
    int right = x + (w - 1);
    switch (tabPlacement) {
      case LEFT:
        paintLeftTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
        break;
      case BOTTOM:
        paintBottomTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
        break;
      case RIGHT:
        paintRightTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
        break;
      case TOP:
      default:
        paintTopTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
    }
  }

  private static final int kSlant = 6;

  protected void paintTopTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght,
      boolean isSelected) {
    int right = tabIndex < tabPane.getTabCount() - 1 ? x + w : rght;
    Color line = isSelected ? MetalLookAndFeel.getPrimaryControlDarkShadow() : MetalLookAndFeel.getControlDarkShadow();
    Color fill = isSelected ? selectColor : getUnselectedFill(tabIndex);
    for (int k = 1; k <= kSlant; k++) {
      int edge = x + kSlant - k;
      g.setColor(fill);
      g.fillRect(edge + 1, y + k, right - edge - 1, 1);
    }
    g.setColor(fill);
    g.fillRect(x + 1, y + kSlant + 1, right - x - 1, btm - y - kSlant);
    g.setColor(line);
    g.fillRect(x + kSlant, y, right - x - kSlant + 1, 1);
    for (int k = 1; k <= kSlant; k++) {
      g.fillRect(x + kSlant - k, y + k, 1, 1);
    }
    g.fillRect(x, y + kSlant, 1, btm - y - kSlant + 1);
    g.fillRect(right, y, 1, btm - y + 1);
    if (isSelected) {
      g.setColor(Color.WHITE);
      g.fillRect(x + kSlant, y + 1, right - x - kSlant, 1);
      for (int k = 2; k <= kSlant; k++) {
        g.fillRect(x + kSlant - k + 1, y + k, 1, 1);
      }
      g.fillRect(x + 1, y + kSlant, 1, btm - y - kSlant + 1);
    }
  }

  private Color getUnselectedFill(int tabIndex) {
    Color bg = tabPane.getBackgroundAt(tabIndex);
    return bg instanceof javax.swing.plaf.UIResource && unselectedBackground != null ? unselectedBackground : bg;
  }

  protected boolean shouldFillGap(int currentRun, int tabIndex, int x, int y) {
    return false;
  }

  protected Color getColorForGap(int currentRun, int x, int y) {
    return tabAreaBackground;
  }

  protected void paintLeftTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght,
      boolean isSelected) {
    g.setColor(isSelected ? darkShadow : shadow);
    g.drawLine(x + 2, y, rght, y);
    g.drawLine(x + 1, y + 1, x + 1, y + 1);
    g.drawLine(x, y + 2, x, btm - 2);
    g.drawLine(x + 1, btm - 1, x + 1, btm - 1);
    g.drawLine(x + 2, btm, rght, btm);
  }

  protected void paintBottomTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght,
      boolean isSelected) {
    g.setColor(isSelected ? darkShadow : shadow);
    g.drawLine(x, y, x, btm - 2);
    g.drawLine(x + 1, btm - 1, x + 1, btm - 1);
    g.drawLine(x + 2, btm, rght - 2, btm);
    g.drawLine(rght - 1, btm - 1, rght - 1, btm - 1);
    g.drawLine(rght, btm - 2, rght, y);
  }

  protected void paintRightTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght,
      boolean isSelected) {
    g.setColor(isSelected ? darkShadow : shadow);
    g.drawLine(x, y, rght - 2, y);
    g.drawLine(rght - 1, y + 1, rght - 1, y + 1);
    g.drawLine(rght, y + 2, rght, btm - 2);
    g.drawLine(rght - 1, btm - 1, rght - 1, btm - 1);
    g.drawLine(x, btm, rght - 2, btm);
  }

  public void update(Graphics g, JComponent c) {
    if (c.isOpaque()) {
      g.setColor(tabAreaBackground);
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }
    paint(g, c);
  }

  protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
      boolean isSelected) {
    if (isSelected) {
      g.setColor(selectColor);
    } else {
      Color bg = tabPane.getBackgroundAt(tabIndex);
      g.setColor(bg instanceof javax.swing.plaf.UIResource && unselectedBackground != null ? unselectedBackground : bg);
    }
    switch (tabPlacement) {
      case LEFT:
        g.fillRect(x + 1, y + 1, w - 1, h - 1);
        break;
      case BOTTOM:
        g.fillRect(x, y, w - 1, h - 1);
        break;
      case RIGHT:
        g.fillRect(x, y, w - 1, h - 1);
        break;
      case TOP:
      default:
        break;
    }
  }

  protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
    return 0;
  }

  protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
    return 0;
  }

  protected int getBaselineOffset() {
    return 0;
  }

  public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
  }

  protected void paintHighlightBelowTab() {
  }

  protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect,
      Rectangle textRect, boolean isSelected) {
    if (tabPane.hasFocus() && isSelected) {
      Rectangle tabRect = rects[tabIndex];
      g.setColor(focus);
      switch (tabPlacement) {
        case LEFT:
          g.drawRect(tabRect.x + 2, tabRect.y + 2, tabRect.width - 3, tabRect.height - 5);
          break;
        case RIGHT:
          g.drawRect(tabRect.x + 2, tabRect.y + 2, tabRect.width - 4, tabRect.height - 5);
          break;
        case BOTTOM:
          g.drawRect(tabRect.x + 2, tabRect.y + 2, tabRect.width - 5, tabRect.height - 3);
          break;
        case TOP:
        default:
          g.drawRect(tabRect.x + 2, tabRect.y + 2, tabRect.width - 5, tabRect.height - 3);
      }
    }
  }

  protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
      int h) {
    Rectangle selRect = selectedIndex < 0 ? null : getTabBounds(selectedIndex, new Rectangle());
    boolean gap = tabPlacement == TOP && selRect != null && selRect.y + selRect.height + 1 >= y && selRect.x >= x
        && selRect.x <= x + w;
    g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
    if (!gap) {
      g.drawLine(x, y, x + w - 2, y);
      g.setColor(Color.WHITE);
      g.drawLine(x + 1, y + 1, x + w - 2, y + 1);
    } else {
      g.drawLine(x, y, selRect.x + 1, y);
      int tabRight = selectedIndex < tabPane.getTabCount() - 1 ? selRect.x + selRect.width : selRect.x + selRect.width - 1;
      g.drawLine(tabRight, y, x + w - 2, y);
      g.setColor(Color.WHITE);
      g.drawLine(x + 1, y + 1, selRect.x + 1, y + 1);
      g.drawLine(tabRight, y + 1, x + w - 2, y + 1);
    }
  }

  protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
      int h) {
    g.setColor(MetalLookAndFeel.getControlDarkShadow());
    g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
  }

  protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
      int h) {
    g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
    g.drawLine(x, y, x, y + h - 1);
  }

  protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
      int h) {
    g.setColor(MetalLookAndFeel.getControlDarkShadow());
    g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
  }

  protected int calculateMaxTabHeight(int tabPlacement) {
    java.awt.FontMetrics metrics = getFontMetrics();
    int height = metrics.getHeight();
    boolean tallerIcons = false;
    for (int i = 0; i < tabPane.getTabCount(); ++i) {
      javax.swing.Icon icon = tabPane.getIconAt(i);
      if (icon != null && icon.getIconHeight() > height) {
        tallerIcons = true;
        break;
      }
    }
    return super.calculateMaxTabHeight(tabPlacement) - (tallerIcons ? (tabInsets.top + tabInsets.bottom) : 0);
  }

  protected int getTabRunOverlay(int tabPlacement) {
    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      int maxTabHeight = calculateMaxTabHeight(tabPlacement);
      Insets tabInsets = getTabInsets(tabPlacement, 0);
      int overlap = tabInsets.left + tabInsets.right + 3;
      return (overlap > maxTabHeight / 2) ? maxTabHeight / 2 : overlap;
    }
    return 0;
  }

  protected boolean shouldRotateTabRuns(int tabPlacement, int selectedRun) {
    return false;
  }

  protected boolean shouldPadTabRun(int tabPlacement, int run) {
    return runCount > 1 && run < runCount - 1;
  }

  public class TabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {
    public TabbedPaneLayout() {
      super();
    }

    protected void padSelectedTab(int tabPlacement, int selectedIndex) {
    }

    protected void normalizeTabRuns(int tabPlacement, int tabCount, int start, int max) {
      if (tabPlacement == TOP || tabPlacement == BOTTOM) {
        super.normalizeTabRuns(tabPlacement, tabCount, start, max);
      }
    }

    protected void rotateTabRuns(int tabPlacement, int selectedRun) {
    }
  }
}
