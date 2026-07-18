/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.View;

public class BasicTabbedPaneUI extends TabbedPaneUI implements SwingConstants {
  protected JTabbedPane tabPane;
  protected java.awt.Color highlight;
  protected java.awt.Color lightHighlight;
  protected java.awt.Color shadow;
  protected java.awt.Color darkShadow;
  protected java.awt.Color focus;
  private java.awt.Color selectedColor;
  protected int textIconGap;
  protected int tabRunOverlay;
  protected Insets tabInsets;
  protected Insets selectedTabPadInsets;
  protected Insets tabAreaInsets;
  protected Insets contentBorderInsets;
  private boolean tabsOverlapBorder;
  private boolean tabsOpaque = true;
  private boolean contentOpaque = true;
  protected KeyStroke upKey;
  protected KeyStroke downKey;
  protected KeyStroke leftKey;
  protected KeyStroke rightKey;
  protected int[] tabRuns = new int[10];
  protected int runCount = 0;
  protected int selectedRun = -1;
  protected Rectangle[] rects = new Rectangle[0];
  protected int maxTabHeight;
  protected int maxTabWidth;
  protected ChangeListener tabChangeListener;
  protected PropertyChangeListener propertyChangeListener;
  protected MouseListener mouseListener;
  protected FocusListener focusListener;
  private Insets currentPadInsets = new Insets(0, 0, 0, 0);
  private Insets currentTabAreaInsets = new Insets(0, 0, 0, 0);
  private Component visibleComponent;
  protected transient Rectangle calcRect = new Rectangle(0, 0, 0, 0);
  private int focusIndex;
  private int rolloverTabIndex = -1;

  public BasicTabbedPaneUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new BasicTabbedPaneUI();
  }

  public void installUI(JComponent c) {
    this.tabPane = (JTabbedPane) c;
    installComponents();
    installDefaults();
    installListeners();
    installKeyboardActions();
    c.setLayout(createLayoutManager());
  }

  public void uninstallUI(JComponent c) {
    uninstallKeyboardActions();
    uninstallListeners();
    uninstallDefaults();
    uninstallComponents();
    c.setLayout(null);
    this.tabPane = null;
  }

  protected LayoutManager createLayoutManager() {
    return new TabbedPaneLayout();
  }

  protected void installComponents() {
  }

  protected void uninstallComponents() {
  }

  protected JButton createScrollButton(int direction) {
    JButton b = new JButton();
    b.setFocusable(false);
    return b;
  }

  protected void installDefaults() {
    LookAndFeel.installColorsAndFont(tabPane, "TabbedPane.background", "TabbedPane.foreground", "TabbedPane.font");
    highlight = UIManager.getColor("TabbedPane.light");
    lightHighlight = UIManager.getColor("TabbedPane.highlight");
    shadow = UIManager.getColor("TabbedPane.shadow");
    darkShadow = UIManager.getColor("TabbedPane.darkShadow");
    focus = UIManager.getColor("TabbedPane.focus");
    selectedColor = UIManager.getColor("TabbedPane.selected");
    textIconGap = UIManager.getInt("TabbedPane.textIconGap");
    tabRunOverlay = UIManager.getInt("TabbedPane.tabRunOverlay");
    tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
    selectedTabPadInsets = UIManager.getInsets("TabbedPane.selectedTabPadInsets");
    tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
    tabsOverlapBorder = UIManager.getBoolean("TabbedPane.tabsOverlapBorder");
    contentBorderInsets = UIManager.getInsets("TabbedPane.contentBorderInsets");
    tabsOpaque = UIManager.getBoolean("TabbedPane.tabsOpaque");
    contentOpaque = UIManager.getBoolean("TabbedPane.contentOpaque");
    Object opaque = UIManager.get("TabbedPane.tabsOpaque");
    if (opaque == null) {
      tabsOpaque = true;
    }
    opaque = UIManager.get("TabbedPane.contentOpaque");
    if (opaque == null) {
      contentOpaque = true;
    }
  }

  protected void uninstallDefaults() {
    highlight = null;
    lightHighlight = null;
    shadow = null;
    darkShadow = null;
    focus = null;
    tabInsets = null;
    selectedTabPadInsets = null;
    tabAreaInsets = null;
    contentBorderInsets = null;
  }

  protected void installListeners() {
    if ((propertyChangeListener = createPropertyChangeListener()) != null) {
      tabPane.addPropertyChangeListener(propertyChangeListener);
    }
    if ((tabChangeListener = createChangeListener()) != null) {
      tabPane.addChangeListener(tabChangeListener);
    }
    if ((mouseListener = createMouseListener()) != null) {
      tabPane.addMouseListener(mouseListener);
    }
    if ((focusListener = createFocusListener()) != null) {
      tabPane.addFocusListener(focusListener);
    }
  }

  protected void uninstallListeners() {
    if (mouseListener != null) {
      tabPane.removeMouseListener(mouseListener);
      mouseListener = null;
    }
    if (focusListener != null) {
      tabPane.removeFocusListener(focusListener);
      focusListener = null;
    }
    if (tabChangeListener != null) {
      tabPane.removeChangeListener(tabChangeListener);
      tabChangeListener = null;
    }
    if (propertyChangeListener != null) {
      tabPane.removePropertyChangeListener(propertyChangeListener);
      propertyChangeListener = null;
    }
  }

  protected void installKeyboardActions() {
  }

  protected void uninstallKeyboardActions() {
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  protected ChangeListener createChangeListener() {
    return new TabSelectionHandler();
  }

  protected FocusListener createFocusListener() {
    return new FocusHandler();
  }

  protected MouseListener createMouseListener() {
    return new MouseHandler();
  }

  protected void assureRectsCreated(int tabCount) {
    int rectArrayLen = rects.length;
    if (tabCount != rectArrayLen) {
      Rectangle[] tempRectArray = new Rectangle[tabCount];
      System.arraycopy(rects, 0, tempRectArray, 0, Math.min(rectArrayLen, tabCount));
      rects = tempRectArray;
      for (int rectIndex = rectArrayLen; rectIndex < tabCount; rectIndex++) {
        rects[rectIndex] = new Rectangle();
      }
    }
  }

  protected void expandTabRunsArray() {
    int rectLen = tabRuns.length;
    int[] newArray = new int[rectLen + 10];
    System.arraycopy(tabRuns, 0, newArray, 0, runCount);
    tabRuns = newArray;
  }

  protected int getRunForTab(int tabCount, int tabIndex) {
    for (int i = 0; i < runCount; i++) {
      int first = tabRuns[i];
      int last = lastTabInRun(tabCount, i);
      if (tabIndex >= first && tabIndex <= last) {
        return i;
      }
    }
    return 0;
  }

  protected int lastTabInRun(int tabCount, int run) {
    if (runCount == 1) {
      return tabCount - 1;
    }
    int nextRun = (run == runCount - 1 ? 0 : run + 1);
    if (tabRuns[nextRun] == 0) {
      return tabCount - 1;
    }
    return tabRuns[nextRun] - 1;
  }

  protected int getTabRunOverlay(int tabPlacement) {
    return tabRunOverlay;
  }

  protected int getTabRunIndent(int tabPlacement, int run) {
    return 0;
  }

  protected boolean shouldPadTabRun(int tabPlacement, int run) {
    return runCount > 1;
  }

  protected boolean shouldRotateTabRuns(int tabPlacement) {
    return true;
  }

  protected Icon getIconForTab(int tabIndex) {
    return (!tabPane.isEnabled() || !tabPane.isEnabledAt(tabIndex)) ? tabPane.getDisabledIconAt(tabIndex)
        : tabPane.getIconAt(tabIndex);
  }

  protected View getTextViewForTab(int tabIndex) {
    return null;
  }

  protected Insets getTabInsets(int tabPlacement, int tabIndex) {
    return tabInsets;
  }

  protected Insets getSelectedTabPadInsets(int tabPlacement) {
    rotateInsets(selectedTabPadInsets, currentPadInsets, tabPlacement);
    return currentPadInsets;
  }

  protected Insets getTabAreaInsets(int tabPlacement) {
    rotateInsets(tabAreaInsets, currentTabAreaInsets, tabPlacement);
    return currentTabAreaInsets;
  }

  protected Insets getContentBorderInsets(int tabPlacement) {
    return contentBorderInsets;
  }

  protected FontMetrics getFontMetrics() {
    Font font = tabPane.getFont();
    return tabPane.getFontMetrics(font);
  }

  protected static void rotateInsets(Insets topInsets, Insets targetInsets, int targetPlacement) {
    switch (targetPlacement) {
      case LEFT:
        targetInsets.top = topInsets.left;
        targetInsets.left = topInsets.top;
        targetInsets.bottom = topInsets.right;
        targetInsets.right = topInsets.bottom;
        break;
      case BOTTOM:
        targetInsets.top = topInsets.bottom;
        targetInsets.left = topInsets.left;
        targetInsets.bottom = topInsets.top;
        targetInsets.right = topInsets.right;
        break;
      case RIGHT:
        targetInsets.top = topInsets.left;
        targetInsets.left = topInsets.bottom;
        targetInsets.bottom = topInsets.right;
        targetInsets.right = topInsets.top;
        break;
      case TOP:
      default:
        targetInsets.top = topInsets.top;
        targetInsets.left = topInsets.left;
        targetInsets.bottom = topInsets.bottom;
        targetInsets.right = topInsets.right;
    }
  }

  protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
    int height = 0;
    View v = getTextViewForTab(tabIndex);
    if (v != null) {
      height += (int) v.getPreferredSpan(View.Y_AXIS);
    } else {
      height += fontHeight;
    }
    Icon icon = getIconForTab(tabIndex);
    if (icon != null) {
      height = Math.max(height, icon.getIconHeight());
    }
    Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
    height += tabInsets.top + tabInsets.bottom + 2;
    return height;
  }

  protected int calculateMaxTabHeight(int tabPlacement) {
    FontMetrics metrics = getFontMetrics();
    int tabCount = tabPane.getTabCount();
    int result = 0;
    int fontHeight = metrics.getHeight();
    for (int i = 0; i < tabCount; i++) {
      result = Math.max(calculateTabHeight(tabPlacement, i, fontHeight), result);
    }
    return result;
  }

  protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
    int width = tabInsets.left + tabInsets.right + 3;
    Icon icon = getIconForTab(tabIndex);
    if (icon != null) {
      width += icon.getIconWidth() + textIconGap;
    }
    View v = getTextViewForTab(tabIndex);
    if (v != null) {
      width += (int) v.getPreferredSpan(View.X_AXIS);
    } else {
      String title = tabPane.getTitleAt(tabIndex);
      width += SwingUtilities.computeStringWidth(metrics, title);
    }
    return width;
  }

  protected int calculateMaxTabWidth(int tabPlacement) {
    FontMetrics metrics = getFontMetrics();
    int tabCount = tabPane.getTabCount();
    int result = 0;
    for (int i = 0; i < tabCount; i++) {
      result = Math.max(calculateTabWidth(tabPlacement, i, metrics), result);
    }
    return result;
  }

  protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
    Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
    int tabRunOverlay = getTabRunOverlay(tabPlacement);
    return (horizRunCount > 0 ? horizRunCount * (maxTabHeight - tabRunOverlay) + tabRunOverlay + tabAreaInsets.top
        + tabAreaInsets.bottom : 0);
  }

  protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount, int maxTabWidth) {
    Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
    int tabRunOverlay = getTabRunOverlay(tabPlacement);
    return (vertRunCount > 0 ? vertRunCount * (maxTabWidth - tabRunOverlay) + tabRunOverlay + tabAreaInsets.left
        + tabAreaInsets.right : 0);
  }

  protected int getTabRunOffset(int tabPlacement, int tabIndex, int index, boolean forward) {
    return 0;
  }

  protected int getFocusIndex() {
    return focusIndex;
  }

  protected int getRolloverTab() {
    return rolloverTabIndex;
  }

  protected void setRolloverTab(int index) {
    rolloverTabIndex = index;
  }

  protected Component getVisibleComponent() {
    return visibleComponent;
  }

  protected void setVisibleComponent(Component component) {
    if (visibleComponent != null && visibleComponent != component && visibleComponent.getParent() == tabPane
        && visibleComponent.isVisible()) {
      visibleComponent.setVisible(false);
    }
    if (component != null && !component.isVisible()) {
      component.setVisible(true);
    }
    visibleComponent = component;
  }

  protected int getBaselineOffset() {
    return 0;
  }

  protected int getBaseline(int tab) {
    FontMetrics fm = getFontMetrics();
    int tabPlacement = tabPane.getTabPlacement();
    Insets insets = getTabInsets(tabPlacement, tab);
    int height = fm.getHeight() + insets.top + insets.bottom + 2;
    return (height - fm.getHeight()) / 2 + fm.getAscent() + getBaselineOffset();
  }

  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    int baseline = calculateBaselineHelper();
    return baseline;
  }

  private int calculateBaselineHelper() {
    if (tabPane.getTabCount() <= 0) {
      return -1;
    }
    int tabPlacement = tabPane.getTabPlacement();
    if (tabPlacement == TOP) {
      Insets areaInsets = getTabAreaInsets(tabPlacement);
      return areaInsets.top + getBaseline(0);
    }
    return -1;
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    switch (tabPane.getTabPlacement()) {
      case LEFT:
      case RIGHT:
      case TOP:
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
      case BOTTOM:
        return Component.BaselineResizeBehavior.CONSTANT_DESCENT;
      default:
        return Component.BaselineResizeBehavior.OTHER;
    }
  }

  void ensureCurrentLayout() {
    if (!tabPane.isValid()) {
      tabPane.validate();
    }
    if (!tabPane.isValid()) {
      TabbedPaneLayout layout = (TabbedPaneLayout) tabPane.getLayout();
      layout.calculateLayoutInfo();
    }
  }

  public Rectangle getTabBounds(JTabbedPane pane, int i) {
    ensureCurrentLayout();
    Rectangle tabRect = new Rectangle();
    return getTabBounds(i, tabRect);
  }

  public int getTabRunCount(JTabbedPane pane) {
    ensureCurrentLayout();
    return runCount;
  }

  public int tabForCoordinate(JTabbedPane pane, int x, int y) {
    ensureCurrentLayout();
    Point p = new Point(x, y);
    int tabCount = tabPane.getTabCount();
    for (int i = 0; i < tabCount; i++) {
      if (rects[i].contains(p.x, p.y)) {
        return i;
      }
    }
    return -1;
  }

  protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
    dest.width = rects[tabIndex].width;
    dest.height = rects[tabIndex].height;
    dest.x = rects[tabIndex].x;
    dest.y = rects[tabIndex].y;
    return dest;
  }

  public void paint(Graphics g, JComponent c) {
    int selectedIndex = tabPane.getSelectedIndex();
    int tabPlacement = tabPane.getTabPlacement();
    ensureCurrentLayout();
    if (tabsOverlapBorder) {
      paintContentBorder(g, tabPlacement, selectedIndex);
    }
    paintTabArea(g, tabPlacement, selectedIndex);
    if (!tabsOverlapBorder) {
      paintContentBorder(g, tabPlacement, selectedIndex);
    }
  }

  protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
    int tabCount = tabPane.getTabCount();
    Rectangle iconRect = new Rectangle();
    Rectangle textRect = new Rectangle();
    Rectangle clipRect = g.getClipBounds();
    for (int i = runCount - 1; i >= 0; i--) {
      int start = tabRuns[i];
      int next = tabRuns[(i == runCount - 1) ? 0 : i + 1];
      int end = (next != 0 ? next - 1 : tabCount - 1);
      for (int j = start; j <= end; j++) {
        if (j != selectedIndex && rects[j].intersects(clipRect)) {
          paintTab(g, tabPlacement, rects, j, iconRect, textRect);
        }
      }
    }
    if (selectedIndex >= 0 && rects[selectedIndex].intersects(clipRect)) {
      paintTab(g, tabPlacement, rects, selectedIndex, iconRect, textRect);
    }
  }

  protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect,
      Rectangle textRect) {
    Rectangle tabRect = rects[tabIndex];
    int selectedIndex = tabPane.getSelectedIndex();
    boolean isSelected = selectedIndex == tabIndex;
    paintTabBackground(g, tabPlacement, tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, isSelected);
    paintTabBorder(g, tabPlacement, tabIndex, tabRect.x, tabRect.y, tabRect.width, tabRect.height, isSelected);
    String title = tabPane.getTitleAt(tabIndex);
    Font font = tabPane.getFont();
    FontMetrics metrics = tabPane.getFontMetrics(font);
    Icon icon = getIconForTab(tabIndex);
    layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected);
    if (title != null && !title.isEmpty()) {
      paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
    }
    paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
    paintFocusIndicator(g, tabPlacement, rects, tabIndex, iconRect, textRect, isSelected);
  }

  protected void paintIcon(Graphics g, int tabPlacement, int tabIndex, Icon icon, Rectangle iconRect,
      boolean isSelected) {
    if (icon != null) {
      icon.paintIcon(tabPane, g, iconRect.x, iconRect.y);
    }
  }

  protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon,
      Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
    Rectangle view = new Rectangle(tabRect);
    Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
    view.x += tabInsets.left;
    view.y += tabInsets.top;
    view.width -= (tabInsets.left + tabInsets.right);
    view.height -= (tabInsets.top + tabInsets.bottom);
    int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
    SwingUtilities.layoutCompoundLabel(tabPane, metrics, title, icon, CENTER, CENTER, CENTER, TRAILING, view, iconRect,
        textRect, textIconGap);
    iconRect.x += xNudge;
    iconRect.y += yNudge;
    textRect.x += xNudge;
    textRect.y += yNudge;
  }

  protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title,
      Rectangle textRect, boolean isSelected) {
    g.setFont(font);
    java.awt.Color fg = tabPane.getForegroundAt(tabIndex);
    if (isSelected && (fg instanceof UIResource)) {
      java.awt.Color selectedFG = UIManager.getColor("TabbedPane.selectedForeground");
      if (selectedFG != null) {
        fg = selectedFG;
      }
    }
    int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
    if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
      g.setColor(fg);
      BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x, textRect.y + metrics.getAscent());
    } else {
      java.awt.Color bg = tabPane.getBackgroundAt(tabIndex);
      g.setColor(bg.brighter());
      BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x, textRect.y + metrics.getAscent());
      g.setColor(bg.darker());
      BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x - 1,
          textRect.y + metrics.getAscent() - 1);
    }
  }

  protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
    Rectangle tabRect = rects[tabIndex];
    String propKey = isSelected ? "selectedLabelShift" : "labelShift";
    int nudge = UIManager.get("TabbedPane." + propKey) != null ? UIManager.getInt("TabbedPane." + propKey) : 1;
    switch (tabPlacement) {
      case LEFT:
        return nudge;
      case RIGHT:
        return -nudge;
      case BOTTOM:
      case TOP:
      default:
        return tabRect.width % 2;
    }
  }

  protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
    Rectangle tabRect = rects[tabIndex];
    String propKey = isSelected ? "selectedLabelShift" : "labelShift";
    int nudge = UIManager.get("TabbedPane." + propKey) != null ? UIManager.getInt("TabbedPane." + propKey) : 1;
    switch (tabPlacement) {
      case BOTTOM:
        return -nudge;
      case LEFT:
      case RIGHT:
        return tabRect.height % 2;
      case TOP:
      default:
        return nudge;
    }
  }

  protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect,
      Rectangle textRect, boolean isSelected) {
    Rectangle tabRect = rects[tabIndex];
    if (tabPane.hasFocus() && isSelected) {
      g.setColor(focus);
      int x;
      int y;
      int w;
      int h;
      switch (tabPlacement) {
        case LEFT:
          x = tabRect.x + 3;
          y = tabRect.y + 3;
          w = tabRect.width - 5;
          h = tabRect.height - 6;
          break;
        case RIGHT:
          x = tabRect.x + 2;
          y = tabRect.y + 3;
          w = tabRect.width - 5;
          h = tabRect.height - 6;
          break;
        case BOTTOM:
          x = tabRect.x + 3;
          y = tabRect.y + 2;
          w = tabRect.width - 6;
          h = tabRect.height - 5;
          break;
        case TOP:
        default:
          x = tabRect.x + 3;
          y = tabRect.y + 3;
          w = tabRect.width - 6;
          h = tabRect.height - 5;
      }
      BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
    }
  }

  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
      boolean isSelected) {
    g.setColor(lightHighlight);
    switch (tabPlacement) {
      case LEFT:
        g.drawLine(x + 1, y + h - 2, x + 1, y + h - 2);
        g.drawLine(x, y + 2, x, y + h - 3);
        g.drawLine(x + 1, y + 1, x + 1, y + 1);
        g.drawLine(x + 2, y, x + w - 1, y);
        g.setColor(shadow);
        g.drawLine(x + 2, y + h - 2, x + w - 1, y + h - 2);
        g.setColor(darkShadow);
        g.drawLine(x + 2, y + h - 1, x + w - 1, y + h - 1);
        break;
      case RIGHT:
        g.drawLine(x, y, x + w - 3, y);
        g.setColor(shadow);
        g.drawLine(x, y + h - 2, x + w - 3, y + h - 2);
        g.drawLine(x + w - 2, y + 2, x + w - 2, y + h - 3);
        g.setColor(darkShadow);
        g.drawLine(x + w - 2, y + 1, x + w - 2, y + 1);
        g.drawLine(x + w - 2, y + h - 2, x + w - 2, y + h - 2);
        g.drawLine(x + w - 1, y + 2, x + w - 1, y + h - 3);
        g.drawLine(x, y + h - 1, x + w - 3, y + h - 1);
        break;
      case BOTTOM:
        g.drawLine(x, y, x, y + h - 3);
        g.drawLine(x + 1, y + h - 2, x + 1, y + h - 2);
        g.setColor(shadow);
        g.drawLine(x + 2, y + h - 2, x + w - 3, y + h - 2);
        g.drawLine(x + w - 2, y, x + w - 2, y + h - 3);
        g.setColor(darkShadow);
        g.drawLine(x + 2, y + h - 1, x + w - 3, y + h - 1);
        g.drawLine(x + w - 2, y + h - 2, x + w - 2, y + h - 2);
        g.drawLine(x + w - 1, y, x + w - 1, y + h - 3);
        break;
      case TOP:
      default:
        g.drawLine(x, y + 2, x, y + h - 1);
        g.drawLine(x + 1, y + 1, x + 1, y + 1);
        g.drawLine(x + 2, y, x + w - 3, y);
        g.setColor(shadow);
        g.drawLine(x + w - 2, y + 2, x + w - 2, y + h - 1);
        g.setColor(darkShadow);
        g.drawLine(x + w - 1, y + 2, x + w - 1, y + h - 1);
        g.drawLine(x + w - 2, y + 1, x + w - 2, y + 1);
    }
  }

  protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
      boolean isSelected) {
    g.setColor(!isSelected || selectedColor == null ? tabPane.getBackgroundAt(tabIndex) : selectedColor);
    switch (tabPlacement) {
      case LEFT:
        g.fillRect(x + 1, y + 1, w - 1, h - 3);
        break;
      case RIGHT:
        g.fillRect(x, y + 1, w - 2, h - 3);
        break;
      case BOTTOM:
        g.fillRect(x + 1, y, w - 3, h - 1);
        break;
      case TOP:
      default:
        g.fillRect(x + 1, y + 1, w - 3, h - 1);
    }
  }

  protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
    int width = tabPane.getWidth();
    int height = tabPane.getHeight();
    Insets insets = tabPane.getInsets();
    Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
    int x = insets.left;
    int y = insets.top;
    int w = width - insets.right - insets.left;
    int h = height - insets.top - insets.bottom;
    switch (tabPlacement) {
      case LEFT:
        x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
        if (tabsOverlapBorder) {
          x -= tabAreaInsets.right;
        }
        w -= (x - insets.left);
        break;
      case RIGHT:
        w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
        if (tabsOverlapBorder) {
          w += tabAreaInsets.left;
        }
        break;
      case BOTTOM:
        h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
        if (tabsOverlapBorder) {
          h += tabAreaInsets.top;
        }
        break;
      case TOP:
      default:
        y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
        if (tabsOverlapBorder) {
          y -= tabAreaInsets.bottom;
        }
        h -= (y - insets.top);
    }
    if (tabPane.getTabCount() > 0 && (contentOpaque || tabPane.isOpaque())) {
      java.awt.Color color = UIManager.getColor("TabbedPane.contentAreaColor");
      if (color != null) {
        g.setColor(color);
      } else if (selectedColor == null || selectedIndex == -1) {
        g.setColor(tabPane.getBackground());
      } else {
        g.setColor(selectedColor);
      }
      g.fillRect(x, y, w, h);
    }
    paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
  }

  protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
      int h) {
    Rectangle selRect = selectedIndex < 0 ? null : getTabBounds(selectedIndex, calcRect);
    g.setColor(lightHighlight);
    if (tabPlacement != TOP || selectedIndex < 0 || (selRect.y + selRect.height + 1 < y)
        || (selRect.x < x || selRect.x > x + w)) {
      g.drawLine(x, y, x + w - 2, y);
    } else {
      g.drawLine(x, y, selRect.x - 1, y);
      if (selRect.x + selRect.width < x + w - 2) {
        g.drawLine(selRect.x + selRect.width, y, x + w - 2, y);
      } else {
        g.setColor(shadow);
        g.drawLine(x + w - 2, y, x + w - 2, y);
      }
    }
  }

  protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
      int h) {
    Rectangle selRect = selectedIndex < 0 ? null : getTabBounds(selectedIndex, calcRect);
    g.setColor(lightHighlight);
    if (tabPlacement != LEFT || selectedIndex < 0 || (selRect.x + selRect.width + 1 < x)
        || (selRect.y < y || selRect.y > y + h)) {
      g.drawLine(x, y, x, y + h - 2);
    } else {
      g.drawLine(x, y, x, selRect.y - 1);
      if (selRect.y + selRect.height < y + h - 2) {
        g.drawLine(x, selRect.y + selRect.height, x, y + h - 2);
      }
    }
  }

  protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
      int h) {
    Rectangle selRect = selectedIndex < 0 ? null : getTabBounds(selectedIndex, calcRect);
    g.setColor(shadow);
    if (tabPlacement != BOTTOM || selectedIndex < 0 || (selRect.y - 1 > h) || (selRect.x < x || selRect.x > x + w)) {
      g.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
      g.setColor(darkShadow);
      g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    } else {
      g.drawLine(x + 1, y + h - 2, selRect.x - 1, y + h - 2);
      g.setColor(darkShadow);
      g.drawLine(x, y + h - 1, selRect.x - 1, y + h - 1);
      if (selRect.x + selRect.width < x + w - 2) {
        g.setColor(shadow);
        g.drawLine(selRect.x + selRect.width, y + h - 2, x + w - 2, y + h - 2);
        g.setColor(darkShadow);
        g.drawLine(selRect.x + selRect.width, y + h - 1, x + w - 1, y + h - 1);
      }
    }
  }

  protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
      int h) {
    Rectangle selRect = selectedIndex < 0 ? null : getTabBounds(selectedIndex, calcRect);
    g.setColor(shadow);
    if (tabPlacement != RIGHT || selectedIndex < 0 || (selRect.x - 1 > w) || (selRect.y < y || selRect.y > y + h)) {
      g.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 3);
      g.setColor(darkShadow);
      g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
    } else {
      g.drawLine(x + w - 2, y + 1, x + w - 2, selRect.y - 1);
      g.setColor(darkShadow);
      g.drawLine(x + w - 1, y, x + w - 1, selRect.y - 1);
      if (selRect.y + selRect.height < y + h - 2) {
        g.setColor(shadow);
        g.drawLine(x + w - 2, selRect.y + selRect.height, x + w - 2, y + h - 2);
        g.setColor(darkShadow);
        g.drawLine(x + w - 1, selRect.y + selRect.height, x + w - 1, y + h - 2);
      }
    }
  }

  public Dimension getMinimumSize(JComponent c) {
    return null;
  }

  public Dimension getMaximumSize(JComponent c) {
    return null;
  }

  protected void selectNextTab(int current) {
    int tabIndex = getNextTabIndex(current);
    if (tabIndex >= 0) {
      tabPane.setSelectedIndex(tabIndex);
    }
  }

  protected void selectPreviousTab(int current) {
    int tabIndex = getPreviousTabIndex(current);
    if (tabIndex >= 0) {
      tabPane.setSelectedIndex(tabIndex);
    }
  }

  protected void selectNextTabInRun(int current) {
    int tabIndex = getNextTabIndexInRun(tabPane.getTabCount(), current);
    tabPane.setSelectedIndex(tabIndex);
  }

  protected void selectPreviousTabInRun(int current) {
    int tabIndex = getPreviousTabIndexInRun(tabPane.getTabCount(), current);
    tabPane.setSelectedIndex(tabIndex);
  }

  protected void selectAdjacentRunTab(int tabPlacement, int tabIndex, int offset) {
    if (runCount < 2) {
      return;
    }
    Rectangle r = rects[tabIndex];
    int x = r.x + r.width / 2 + (tabPlacement == TOP || tabPlacement == BOTTOM ? 0 : offset);
    int y = r.y + r.height / 2 + (tabPlacement == TOP || tabPlacement == BOTTOM ? offset : 0);
    int newIndex = tabForCoordinate(tabPane, x, y);
    if (newIndex != -1) {
      tabPane.setSelectedIndex(newIndex);
    }
  }

  protected int getNextTabIndex(int base) {
    return (base == tabPane.getTabCount() - 1 ? 0 : base + 1);
  }

  protected int getPreviousTabIndex(int base) {
    int tabIndex = (base - 1 >= 0 ? base - 1 : tabPane.getTabCount() - 1);
    return (tabIndex >= 0 ? tabIndex : 0);
  }

  protected int getNextTabIndexInRun(int tabCount, int base) {
    if (runCount < 2) {
      return getNextTabIndex(base);
    }
    int currentRun = getRunForTab(tabCount, base);
    int next = getNextTabIndex(base);
    if (next == tabRuns[getNextTabRun(currentRun)]) {
      return tabRuns[currentRun];
    }
    return next;
  }

  protected int getPreviousTabIndexInRun(int tabCount, int base) {
    if (runCount < 2) {
      return getPreviousTabIndex(base);
    }
    int currentRun = getRunForTab(tabCount, base);
    if (base == tabRuns[currentRun]) {
      int previous = tabRuns[getNextTabRun(currentRun)] - 1;
      return previous != -1 ? previous : tabCount - 1;
    }
    return getPreviousTabIndex(base);
  }

  protected int getPreviousTabRun(int baseRun) {
    int runIndex = (baseRun - 1 >= 0 ? baseRun - 1 : runCount - 1);
    return runIndex >= 0 ? runIndex : 0;
  }

  protected int getNextTabRun(int baseRun) {
    return (baseRun == runCount - 1 ? 0 : baseRun + 1);
  }

  protected void navigateSelectedTab(int direction) {
    int tabPlacement = tabPane.getTabPlacement();
    int current = tabPane.getSelectedIndex();
    boolean horizontal = tabPlacement == TOP || tabPlacement == BOTTOM;
    switch (direction) {
      case NEXT:
        selectNextTab(current);
        break;
      case PREVIOUS:
        selectPreviousTab(current);
        break;
      case NORTH:
        if (horizontal) {
          selectAdjacentRunTab(tabPlacement, current, -maxTabHeight);
        } else {
          selectPreviousTabInRun(current);
        }
        break;
      case SOUTH:
        if (horizontal) {
          selectAdjacentRunTab(tabPlacement, current, maxTabHeight);
        } else {
          selectNextTabInRun(current);
        }
        break;
      case WEST:
        if (horizontal) {
          selectPreviousTabInRun(current);
        } else {
          selectAdjacentRunTab(tabPlacement, current, -maxTabWidth);
        }
        break;
      case EAST:
        if (horizontal) {
          selectNextTabInRun(current);
        } else {
          selectAdjacentRunTab(tabPlacement, current, maxTabWidth);
        }
        break;
      default:
    }
  }

  public class TabbedPaneLayout implements LayoutManager {
    public TabbedPaneLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
      return calculateSize(false);
    }

    public Dimension minimumLayoutSize(Container parent) {
      return calculateSize(true);
    }

    protected Dimension calculateSize(boolean minimum) {
      int tabPlacement = tabPane.getTabPlacement();
      Insets insets = tabPane.getInsets();
      Insets contentInsets = getContentBorderInsets(tabPlacement);
      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      int height = 0;
      int width = 0;
      for (int i = 0; i < tabPane.getTabCount(); i++) {
        Component component = tabPane.getComponentAt(i);
        if (component != null) {
          Dimension size = minimum ? component.getMinimumSize() : component.getPreferredSize();
          if (size != null) {
            height = Math.max(size.height, height);
            width = Math.max(size.width, width);
          }
        }
      }
      int tabExtent;
      switch (tabPlacement) {
        case LEFT:
        case RIGHT:
          height = Math.max(height, calculateMaxTabHeight(tabPlacement));
          tabExtent = preferredTabAreaWidth(tabPlacement, height - tabAreaInsets.top - tabAreaInsets.bottom);
          width += tabExtent;
          break;
        case TOP:
        case BOTTOM:
        default:
          width = Math.max(width, calculateMaxTabWidth(tabPlacement));
          tabExtent = preferredTabAreaHeight(tabPlacement, width - tabAreaInsets.left - tabAreaInsets.right);
          height += tabExtent;
      }
      width += contentInsets.left + contentInsets.right;
      height += contentInsets.top + contentInsets.bottom;
      return new Dimension(width + insets.left + insets.right, height + insets.bottom + insets.top);
    }

    protected int preferredTabAreaHeight(int tabPlacement, int width) {
      FontMetrics metrics = getFontMetrics();
      int tabCount = tabPane.getTabCount();
      int total = 0;
      if (tabCount > 0) {
        int rows = 1;
        int x = 0;
        int maxTabHeight = calculateMaxTabHeight(tabPlacement);
        for (int i = 0; i < tabCount; i++) {
          int tabWidth = calculateTabWidth(tabPlacement, i, metrics);
          if (x != 0 && x + tabWidth > width) {
            rows++;
            x = 0;
          }
          x += tabWidth;
        }
        total = calculateTabAreaHeight(tabPlacement, rows, maxTabHeight);
      }
      return total;
    }

    protected int preferredTabAreaWidth(int tabPlacement, int height) {
      FontMetrics metrics = getFontMetrics();
      int tabCount = tabPane.getTabCount();
      int total = 0;
      if (tabCount > 0) {
        int columns = 1;
        int y = 0;
        int fontHeight = metrics.getHeight();
        maxTabWidth = calculateMaxTabWidth(tabPlacement);
        for (int i = 0; i < tabCount; i++) {
          int tabHeight = calculateTabHeight(tabPlacement, i, fontHeight);
          if (y != 0 && y + tabHeight > height) {
            columns++;
            y = 0;
          }
          y += tabHeight;
        }
        total = calculateTabAreaWidth(tabPlacement, columns, maxTabWidth);
      }
      return total;
    }

    public void layoutContainer(Container parent) {
      int tabPlacement = tabPane.getTabPlacement();
      Insets insets = tabPane.getInsets();
      int selectedIndex = tabPane.getSelectedIndex();
      Component visibleComponent = getVisibleComponent();
      calculateLayoutInfo();
      Component selectedComponent = null;
      if (selectedIndex < 0) {
        if (visibleComponent != null) {
          setVisibleComponent(null);
        }
      } else {
        selectedComponent = tabPane.getComponentAt(selectedIndex);
      }
      int cx;
      int cy;
      int cw;
      int ch;
      int totalTabWidth = 0;
      int totalTabHeight = 0;
      Insets contentInsets = getContentBorderInsets(tabPlacement);
      boolean shouldChangeFocus = false;
      if (selectedComponent != null) {
        if (selectedComponent != visibleComponent && visibleComponent != null) {
          shouldChangeFocus = SwingUtilities.findFocusOwner(visibleComponent) != null;
        }
        setVisibleComponent(selectedComponent);
      }
      Rectangle bounds = tabPane.getBounds();
      int numChildren = tabPane.getComponentCount();
      if (numChildren > 0) {
        switch (tabPlacement) {
          case LEFT:
            totalTabWidth = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
            cx = insets.left + totalTabWidth + contentInsets.left;
            cy = insets.top + contentInsets.top;
            break;
          case RIGHT:
            totalTabWidth = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
            cx = insets.left + contentInsets.left;
            cy = insets.top + contentInsets.top;
            break;
          case BOTTOM:
            totalTabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            cx = insets.left + contentInsets.left;
            cy = insets.top + contentInsets.top;
            break;
          case TOP:
          default:
            totalTabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            cx = insets.left + contentInsets.left;
            cy = insets.top + totalTabHeight + contentInsets.top;
        }
        cw = bounds.width - totalTabWidth - insets.left - insets.right - contentInsets.left - contentInsets.right;
        ch = bounds.height - totalTabHeight - insets.top - insets.bottom - contentInsets.top - contentInsets.bottom;
        for (int i = 0; i < numChildren; i++) {
          Component child = tabPane.getComponent(i);
          child.setBounds(cx, cy, cw, ch);
        }
      }
    }

    public void calculateLayoutInfo() {
      int tabCount = tabPane.getTabCount();
      assureRectsCreated(tabCount);
      calculateTabRects(tabPane.getTabPlacement(), tabCount);
    }

    protected void calculateTabRects(int tabPlacement, int tabCount) {
      FontMetrics metrics = getFontMetrics();
      Dimension size = tabPane.getSize();
      Insets insets = tabPane.getInsets();
      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      int fontHeight = metrics.getHeight();
      int selectedIndex = tabPane.getSelectedIndex();
      int tabRunOverlay;
      int i;
      int j;
      int x;
      int y;
      int returnAt;
      boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
      boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();
      switch (tabPlacement) {
        case LEFT:
          maxTabWidth = calculateMaxTabWidth(tabPlacement);
          x = insets.left + tabAreaInsets.left;
          y = insets.top + tabAreaInsets.top;
          returnAt = size.height - (insets.bottom + tabAreaInsets.bottom);
          break;
        case RIGHT:
          maxTabWidth = calculateMaxTabWidth(tabPlacement);
          x = size.width - insets.right - tabAreaInsets.right - maxTabWidth;
          y = insets.top + tabAreaInsets.top;
          returnAt = size.height - (insets.bottom + tabAreaInsets.bottom);
          break;
        case BOTTOM:
          maxTabHeight = calculateMaxTabHeight(tabPlacement);
          x = insets.left + tabAreaInsets.left;
          y = size.height - insets.bottom - tabAreaInsets.bottom - maxTabHeight;
          returnAt = size.width - (insets.right + tabAreaInsets.right);
          break;
        case TOP:
        default:
          maxTabHeight = calculateMaxTabHeight(tabPlacement);
          x = insets.left + tabAreaInsets.left;
          y = insets.top + tabAreaInsets.top;
          returnAt = size.width - (insets.right + tabAreaInsets.right);
          break;
      }
      tabRunOverlay = getTabRunOverlay(tabPlacement);
      runCount = 0;
      selectedRun = -1;
      if (tabCount == 0) {
        return;
      }
      Rectangle rect;
      for (i = 0; i < tabCount; i++) {
        rect = rects[i];
        if (!verticalTabRuns) {
          if (i > 0) {
            rect.x = rects[i - 1].x + rects[i - 1].width;
          } else {
            tabRuns[0] = 0;
            runCount = 1;
            maxTabWidth = 0;
            rect.x = x;
          }
          rect.width = calculateTabWidth(tabPlacement, i, metrics);
          maxTabWidth = Math.max(maxTabWidth, rect.width);
          if (rect.x != x && rect.x + rect.width > returnAt) {
            if (runCount > tabRuns.length - 1) {
              expandTabRunsArray();
            }
            tabRuns[runCount] = i;
            runCount++;
            rect.x = x;
          }
          rect.y = y;
          rect.height = maxTabHeight;
        } else {
          if (i > 0) {
            rect.y = rects[i - 1].y + rects[i - 1].height;
          } else {
            tabRuns[0] = 0;
            runCount = 1;
            maxTabHeight = 0;
            rect.y = y;
          }
          rect.height = calculateTabHeight(tabPlacement, i, fontHeight);
          maxTabHeight = Math.max(maxTabHeight, rect.height);
          if (rect.y != y && rect.y + rect.height > returnAt) {
            if (runCount > tabRuns.length - 1) {
              expandTabRunsArray();
            }
            tabRuns[runCount] = i;
            runCount++;
            rect.y = y;
          }
          rect.x = x;
          rect.width = maxTabWidth;
        }
        if (i == selectedIndex) {
          selectedRun = runCount - 1;
        }
      }
      if (runCount > 1) {
        int newRun = verticalTabRuns ? y : x;
        normalizeTabRuns(tabPlacement, tabCount, newRun, returnAt);
        selectedRun = getRunForTab(tabCount, selectedIndex);
        if (shouldRotateTabRuns(tabPlacement)) {
          rotateTabRuns(tabPlacement, selectedRun);
        }
      }
      for (i = runCount - 1; i >= 0; i--) {
        int start = tabRuns[i];
        int next = tabRuns[(i == (runCount - 1)) ? 0 : i + 1];
        int end = (next != 0 ? next - 1 : tabCount - 1);
        if (!verticalTabRuns) {
          for (j = start; j <= end; j++) {
            rect = rects[j];
            rect.y = y;
            rect.x += getTabRunIndent(tabPlacement, i);
          }
          if (shouldPadTabRun(tabPlacement, i)) {
            padTabRun(tabPlacement, start, end, returnAt);
          }
          if (tabPlacement == BOTTOM) {
            y -= (maxTabHeight - tabRunOverlay);
          } else {
            y += (maxTabHeight - tabRunOverlay);
          }
        } else {
          for (j = start; j <= end; j++) {
            rect = rects[j];
            rect.x = x;
            rect.y += getTabRunIndent(tabPlacement, i);
          }
          if (shouldPadTabRun(tabPlacement, i)) {
            padTabRun(tabPlacement, start, end, returnAt);
          }
          if (tabPlacement == RIGHT) {
            x -= (maxTabWidth - tabRunOverlay);
          } else {
            x += (maxTabWidth - tabRunOverlay);
          }
        }
      }
      padSelectedTab(tabPlacement, selectedIndex);
      if (!leftToRight && !verticalTabRuns) {
        int rightMargin = size.width - (insets.right + tabAreaInsets.right);
        for (i = 0; i < tabCount; i++) {
          rects[i].x = rightMargin - rects[i].x - rects[i].width;
        }
      }
    }

    protected void rotateTabRuns(int tabPlacement, int selectedRun) {
      for (int i = 0; i < selectedRun; i++) {
        int save = tabRuns[0];
        for (int j = 1; j < runCount; j++) {
          tabRuns[j - 1] = tabRuns[j];
        }
        tabRuns[runCount - 1] = save;
      }
    }

    protected void normalizeTabRuns(int tabPlacement, int tabCount, int start, int max) {
      boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
      int run = runCount - 1;
      boolean keepAdjusting = true;
      double weight = 1.25;
      while (keepAdjusting) {
        int last = lastTabInRun(tabCount, run);
        int prevLast = lastTabInRun(tabCount, run - 1);
        int end;
        int prevLastLen;
        if (!verticalTabRuns) {
          end = rects[last].x + rects[last].width;
          prevLastLen = (int) (maxTabWidth * weight);
        } else {
          end = rects[last].y + rects[last].height;
          prevLastLen = (int) (maxTabHeight * weight * 2);
        }
        if (max - end > prevLastLen) {
          tabRuns[run] = prevLast;
          if (!verticalTabRuns) {
            rects[prevLast].x = start;
          } else {
            rects[prevLast].y = start;
          }
          for (int i = prevLast + 1; i <= last; i++) {
            if (!verticalTabRuns) {
              rects[i].x = rects[i - 1].x + rects[i - 1].width;
            } else {
              rects[i].y = rects[i - 1].y + rects[i - 1].height;
            }
          }
        } else if (run == runCount - 1) {
          keepAdjusting = false;
        }
        if (run - 1 > 0) {
          run -= 1;
        } else {
          run = runCount - 1;
          weight += .25;
        }
      }
    }

    protected void padTabRun(int tabPlacement, int start, int end, int max) {
      Rectangle lastRect = rects[end];
      if (tabPlacement == TOP || tabPlacement == BOTTOM) {
        int runWidth = (lastRect.x + lastRect.width) - rects[start].x;
        int deltaWidth = max - (lastRect.x + lastRect.width);
        float factor = (float) deltaWidth / (float) runWidth;
        for (int j = start; j <= end; j++) {
          Rectangle pastRect = rects[j];
          if (j > start) {
            pastRect.x = rects[j - 1].x + rects[j - 1].width;
          }
          pastRect.width += Math.round((float) pastRect.width * factor);
        }
        lastRect.width = max - lastRect.x;
      } else {
        int runHeight = (lastRect.y + lastRect.height) - rects[start].y;
        int deltaHeight = max - (lastRect.y + lastRect.height);
        float factor = (float) deltaHeight / (float) runHeight;
        for (int j = start; j <= end; j++) {
          Rectangle pastRect = rects[j];
          if (j > start) {
            pastRect.y = rects[j - 1].y + rects[j - 1].height;
          }
          pastRect.height += Math.round((float) pastRect.height * factor);
        }
        lastRect.height = max - lastRect.y;
      }
    }

    protected void padSelectedTab(int tabPlacement, int selectedIndex) {
      if (selectedIndex >= 0) {
        Rectangle selRect = rects[selectedIndex];
        Insets padInsets = getSelectedTabPadInsets(tabPlacement);
        selRect.x -= padInsets.left;
        selRect.width += (padInsets.left + padInsets.right);
        selRect.y -= padInsets.top;
        selRect.height += (padInsets.top + padInsets.bottom);
        Dimension size = tabPane.getSize();
        Insets insets = tabPane.getInsets();
        if (tabPlacement == LEFT || tabPlacement == RIGHT) {
          int top = insets.top - selRect.y;
          if (top > 0) {
            selRect.y += top;
            selRect.height -= top;
          }
          int bottom = (selRect.y + selRect.height) + insets.bottom - size.height;
          if (bottom > 0) {
            selRect.height -= bottom;
          }
        } else {
          int left = insets.left - selRect.x;
          if (left > 0) {
            selRect.x += left;
            selRect.width -= left;
          }
          int right = (selRect.x + selRect.width) + insets.right - size.width;
          if (right > 0) {
            selRect.width -= right;
          }
        }
      }
    }
  }

  public class FocusHandler extends FocusAdapter {
    public FocusHandler() {
    }

    public void focusGained(FocusEvent e) {
      tabPane.repaint();
    }

    public void focusLost(FocusEvent e) {
      tabPane.repaint();
    }
  }

  public class MouseHandler extends MouseAdapter {
    public MouseHandler() {
    }

    public void mousePressed(MouseEvent e) {
      if (!tabPane.isEnabled()) {
        return;
      }
      int tabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());
      if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex)) {
        if (tabIndex != tabPane.getSelectedIndex()) {
          tabPane.setSelectedIndex(tabIndex);
        } else if (tabPane.isRequestFocusEnabled()) {
          tabPane.requestFocus();
        }
      }
    }
  }

  public class TabSelectionHandler implements ChangeListener {
    public TabSelectionHandler() {
    }

    public void stateChanged(ChangeEvent e) {
      JTabbedPane tabPane = (JTabbedPane) e.getSource();
      tabPane.revalidate();
      tabPane.repaint();
      int index = tabPane.getSelectedIndex();
      if (index < rects.length && index != -1) {
        tabPane.repaint(rects[index].x, rects[index].y, rects[index].width, rects[index].height);
      }
    }
  }

  public class PropertyChangeHandler implements PropertyChangeListener {
    public PropertyChangeHandler() {
    }

    public void propertyChange(PropertyChangeEvent e) {
      JTabbedPane pane = (JTabbedPane) e.getSource();
      String name = e.getPropertyName();
      if ("tabLayoutPolicy" == name) {
        pane.revalidate();
        pane.repaint();
      } else if ("tabPlacement" == name || "opaque" == name || "background" == name || "indexForTabComponent" == name) {
        pane.revalidate();
        pane.repaint();
      }
    }
  }
}
