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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.UIResource;

public class BasicMenuItemUI extends javax.swing.plaf.MenuItemUI {
  private javax.swing.MenuElement[] bp_Path;
  protected javax.swing.JMenuItem menuItem;
  protected java.awt.Color selectionBackground;
  protected java.awt.Color selectionForeground;
  protected java.awt.Color disabledForeground;
  protected java.awt.Color acceleratorForeground;
  protected java.awt.Color acceleratorSelectionForeground;
  protected java.lang.String acceleratorDelimiter;
  protected int defaultTextIconGap;
  protected java.awt.Font acceleratorFont;
  protected javax.swing.event.MouseInputListener mouseInputListener;
  protected javax.swing.event.MenuDragMouseListener menuDragMouseListener;
  protected javax.swing.event.MenuKeyListener menuKeyListener;
  protected java.beans.PropertyChangeListener propertyChangeListener;
  protected javax.swing.Icon arrowIcon;
  protected javax.swing.Icon checkIcon;
  protected boolean oldBorderPainted;
  public BasicMenuItemUI() {
  }
  public void update(Graphics g, JComponent c) {
    paint(g, c);
  }
  public MenuElement[] getPath() {
    MenuSelectionManager m = MenuSelectionManager.defaultManager();
    MenuElement[] oldPath = m.getSelectedPath();
    MenuElement[] newPath;
    int i = oldPath.length;
    if (i == 0) {
      return new MenuElement[0];
    }
    Component parent = menuItem.getParent();
    if (oldPath[i - 1].getComponent() == parent) {
      newPath = new MenuElement[i + 1];
      System.arraycopy(oldPath, 0, newPath, 0, i);
      newPath[i] = menuItem;
    } else {
      int j;
      for (j = oldPath.length - 1; j >= 0; j--) {
        if (oldPath[j].getComponent() == parent) {
          break;
        }
      }
      newPath = new MenuElement[j + 2];
      System.arraycopy(oldPath, 0, newPath, 0, j + 1);
      newPath[j + 1] = menuItem;
    }
    return newPath;
  }
  protected void doClick(MenuSelectionManager msm) {
    MenuSelectionManager.defaultManager().clearSelectedPath();
    menuItem.doClick(0);
  }
  protected String getPropertyPrefix() {
    return "MenuItem";
  }
  protected void installKeyboardActions() {
  }
  protected void uninstallKeyboardActions() {
  }
  protected void installDefaults() {
    String prefix = getPropertyPrefix();
    acceleratorFont = UIManager.getFont("MenuItem.acceleratorFont");
    if (acceleratorFont == null) {
      acceleratorFont = UIManager.getFont("MenuItem.font");
    }
    Integer gap = (Integer) UIManager.get(prefix + ".textIconGap");
    defaultTextIconGap = gap != null ? gap.intValue() : 4;
    LookAndFeel.installProperty(menuItem, "opaque", Boolean.TRUE);
    if (menuItem.getMargin() == null || menuItem.getMargin() instanceof UIResource) {
      menuItem.setMargin(UIManager.getInsets(prefix + ".margin"));
    }
    LookAndFeel.installColorsAndFont(menuItem, prefix + ".background", prefix + ".foreground", prefix + ".font");
    selectionBackground = UIManager.getColor(prefix + ".selectionBackground");
    selectionForeground = UIManager.getColor(prefix + ".selectionForeground");
    disabledForeground = UIManager.getColor(prefix + ".disabledForeground");
    acceleratorForeground = UIManager.getColor(prefix + ".acceleratorForeground");
    acceleratorSelectionForeground = UIManager.getColor(prefix + ".acceleratorSelectionForeground");
    acceleratorDelimiter = UIManager.getString("MenuItem.acceleratorDelimiter");
    if (acceleratorDelimiter == null) {
      acceleratorDelimiter = "+";
    }
    if (arrowIcon == null || arrowIcon instanceof UIResource) {
      arrowIcon = UIManager.getIcon(prefix + ".arrowIcon");
    }
    if (checkIcon == null || checkIcon instanceof UIResource) {
      checkIcon = UIManager.getIcon(prefix + ".checkIcon");
      boolean isBorderPainted = UIManager.getBoolean(prefix + ".borderPainted");
      LookAndFeel.installProperty(menuItem, "borderPainted", Boolean.valueOf(isBorderPainted));
    }
    LookAndFeel.installBorder(menuItem, prefix + ".border");
    oldBorderPainted = menuItem.isBorderPainted();
  }
  protected void installListeners() {
    if ((mouseInputListener = createMouseInputListener(menuItem)) != null) {
      menuItem.addMouseListener(mouseInputListener);
      menuItem.addMouseMotionListener(mouseInputListener);
    }
    if ((propertyChangeListener = createPropertyChangeListener(menuItem)) != null) {
      menuItem.addPropertyChangeListener(propertyChangeListener);
    }
  }
  protected void uninstallListeners() {
    if (mouseInputListener != null) {
      menuItem.removeMouseListener(mouseInputListener);
      menuItem.removeMouseMotionListener(mouseInputListener);
    }
    if (propertyChangeListener != null) {
      menuItem.removePropertyChangeListener(propertyChangeListener);
    }
    mouseInputListener = null;
    propertyChangeListener = null;
  }
  protected void uninstallDefaults() {
    menuItem.setArmed(false);
    menuItem.setBorderPainted(oldBorderPainted);
    if (menuItem.getMargin() instanceof UIResource) {
      menuItem.setMargin(null);
    }
    if (arrowIcon instanceof UIResource) {
      arrowIcon = null;
    }
    if (checkIcon instanceof UIResource) {
      checkIcon = null;
    }
    LookAndFeel.uninstallBorder(menuItem);
  }
  protected void installComponents(JMenuItem menuItem) {
  }
  protected MouseInputListener createMouseInputListener(JComponent c) {
    return new MouseInputHandler();
  }
  protected javax.swing.event.MenuDragMouseListener createMenuDragMouseListener(JComponent c) {
    return null;
  }
  protected javax.swing.event.MenuKeyListener createMenuKeyListener(JComponent c) {
    return null;
  }
  protected java.beans.PropertyChangeListener createPropertyChangeListener(JComponent c) {
    return new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if ("labelFor".equals(name) || "displayedMnemonic".equals(name) || "accelerator".equals(name)) {
          menuItem.revalidate();
          menuItem.repaint();
        } else if ("text".equals(name) || "font".equals(name) || "foreground".equals(name)) {
          menuItem.revalidate();
          menuItem.repaint();
        }
      }
    };
  }
  protected void uninstallComponents(JMenuItem menuItem) {
  }
  protected Dimension getPreferredMenuItemSize(JComponent c, Icon checkIcon, Icon arrowIcon, int defaultTextIconGap) {
    int[] col = columns(c, checkIcon, arrowIcon, defaultTextIconGap);
    int gap = defaultTextIconGap;
    int width = 0;
    if (col[0] > 0) {
      width += col[0] + gap;
    }
    width += col[1];
    if (col[2] > 0) {
      width += col[2] + gap;
    }
    if (col[3] > 0) {
      width += col[3] + gap;
    }
    int height = col[4];
    Insets insets = menuItem.getInsets();
    if (insets != null) {
      width += insets.left + insets.right;
      height += insets.top + insets.bottom;
    }
    if (width % 2 == 0) {
      width++;
    }
    if (height % 2 == 0 && !Boolean.TRUE.equals(UIManager.get(getPropertyPrefix() + ".evenHeight"))) {
      height++;
    }
    return new Dimension(width, height);
  }
  protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
    javax.swing.ButtonModel model = menuItem.getModel();
    Color oldColor = g.getColor();
    int menuWidth = menuItem.getWidth();
    int menuHeight = menuItem.getHeight();
    boolean selected = model.isArmed() || (menuItem instanceof JMenu && model.isSelected());
    if (menuItem.isOpaque()) {
      if (selected) {
        g.setColor(bgColor);
        g.fillRect(0, 0, menuWidth, menuHeight);
      } else {
        g.setColor(menuItem.getBackground());
        g.fillRect(0, 0, menuWidth, menuHeight);
      }
      g.setColor(oldColor);
    } else if (selected) {
      g.setColor(bgColor);
      g.fillRect(0, 0, menuWidth, menuHeight);
      g.setColor(oldColor);
    }
  }
  protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
    FontMetrics fm = g.getFontMetrics();
    int mnemIndex = menuItem.getDisplayedMnemonicIndex();
    g.setFont(menuItem.getFont());
    javax.swing.text.View htmlView = BasicHTML.rendererFor(menuItem, text);
    if (htmlView != null) {
      htmlView.paint(g, textRect);
      return;
    }
    BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemIndex, textRect.x,
        textRect.y + (textRect.height - fm.getHeight()) / 2 + fm.getAscent());
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new BasicMenuItemUI();
  }
  public Dimension getMaximumSize(JComponent c) {
    return null;
  }
  public void paint(Graphics g, JComponent c) {
    paintMenuItem(g, c, checkIcon, arrowIcon, selectionBackground, selectionForeground, defaultTextIconGap);
  }
  public Dimension getPreferredSize(JComponent c) {
    return getPreferredMenuItemSize(c, checkIcon, arrowIcon, defaultTextIconGap);
  }
  public Dimension getMinimumSize(JComponent c) {
    return null;
  }
  public void installUI(JComponent c) {
    menuItem = (JMenuItem) c;
    installDefaults();
    installComponents(menuItem);
    installListeners();
    installKeyboardActions();
  }
  public void uninstallUI(JComponent c) {
    menuItem = (JMenuItem) c;
    uninstallDefaults();
    uninstallComponents(menuItem);
    uninstallListeners();
    uninstallKeyboardActions();
    menuItem = null;
  }
  static final String MAX_TEXT_WIDTH = "maxTextWidth";
  static final String MAX_ACC_WIDTH = "maxAccWidth";
  private static final String MAX_ICON_WIDTH = "shim.maxIconWidth";
  private static final String MAX_LABEL_HEIGHT = "shim.maxLabelHeight";
  private Color selectionBackgroundColor;

  private boolean isTopLevel() {
    return menuItem instanceof JMenu && ((JMenu) menuItem).isTopLevelMenu();
  }

  private String acceleratorText() {
    KeyStroke accelerator = menuItem.getAccelerator();
    String acceleratorText = "";
    if (accelerator != null) {
      int modifiers = accelerator.getModifiers();
      if (modifiers > 0) {
        acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
        acceleratorText += acceleratorDelimiter;
      }
      int keyCode = accelerator.getKeyCode();
      if (keyCode != 0) {
        acceleratorText += KeyEvent.getKeyText(keyCode);
      } else {
        acceleratorText += accelerator.getKeyChar();
      }
    }
    return acceleratorText;
  }

  private int[] columns(JComponent c, Icon checkIcon, Icon arrowIcon, int gap) {
    JMenuItem mi = (JMenuItem) c;
    FontMetrics fm = mi.getFontMetrics(mi.getFont());
    FontMetrics fmAcc = mi.getFontMetrics(acceleratorFont);
    String text = mi.getText();
    javax.swing.text.View htmlView = BasicHTML.rendererFor(mi, text);
    int tw = htmlView != null ? (int) htmlView.getPreferredSpan(javax.swing.text.View.X_AXIS)
        : text == null || text.isEmpty() ? 0 : fm.stringWidth(text);
    Icon icon = mi.getIcon();
    int iw = icon == null ? 0 : icon.getIconWidth();
    int labelW = iw + (iw > 0 && tw > 0 ? gap : 0) + tw + (iw > 0 || tw > 0 ? 2 * gap : gap);
    String acc = acceleratorText();
    int aw = acc.isEmpty() ? 0 : fmAcc.stringWidth(acc);
    boolean top = isTopLevel();
    int cw = checkIcon == null || top ? 0 : checkIcon.getIconWidth();
    int arw = arrowIcon == null || top ? 0 : arrowIcon.getIconWidth();
    int h = fm.getHeight();
    if (text == null || text.isEmpty()) {
      h = 0;
    }
    if (icon != null) {
      h = Math.max(h, icon.getIconHeight());
    }
    if (!top && arrowIcon != null) {
      h = Math.max(h, arrowIcon.getIconHeight());
    }
    if (!top && checkIcon != null) {
      h = Math.max(h, checkIcon.getIconHeight());
    }
    Container parent = mi.getParent();
    if (!top && parent instanceof JComponent) {
      JComponent p = (JComponent) parent;
      Integer maxLabel = (Integer) p.getClientProperty(MAX_TEXT_WIDTH);
      Integer maxAcc = (Integer) p.getClientProperty(MAX_ACC_WIDTH);
      Integer maxCheck = (Integer) p.getClientProperty(MAX_ICON_WIDTH);
      if (maxLabel != null && maxLabel.intValue() > labelW) {
        labelW = maxLabel.intValue();
      } else {
        p.putClientProperty(MAX_TEXT_WIDTH, Integer.valueOf(labelW));
      }
      if (maxAcc != null && maxAcc.intValue() > aw) {
        aw = maxAcc.intValue();
      } else {
        p.putClientProperty(MAX_ACC_WIDTH, Integer.valueOf(aw));
      }
      if (maxCheck != null && maxCheck.intValue() > cw) {
        cw = maxCheck.intValue();
      } else {
        p.putClientProperty(MAX_ICON_WIDTH, Integer.valueOf(cw));
      }
    }
    return new int[] {cw, labelW, aw, arw, h, tw, iw};
  }

  protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background,
      Color foreground, int defaultTextIconGap) {
    JMenuItem mi = (JMenuItem) c;
    Font holdf = g.getFont();
    Color holdc = g.getColor();
    g.setFont(mi.getFont());
    int menuWidth = mi.getWidth();
    int menuHeight = mi.getHeight();
    Insets in = mi.getInsets();
    paintBackground(g, mi, background);
    boolean top = isTopLevel();
    boolean enabled = mi.isEnabled();
    boolean selected = mi.isArmed() || (mi instanceof JMenu && mi.isSelected());
    Color fg = enabled ? (selected ? foreground : mi.getForeground()) : disabledForeground;
    if (fg == null) {
      fg = mi.getForeground();
    }
    int[] col = columns(c, checkIcon, arrowIcon, defaultTextIconGap);
    int gap = defaultTextIconGap;
    boolean ltr = mi.getComponentOrientation().isLeftToRight();
    FontMetrics fm = mi.getFontMetrics(mi.getFont());
    int x = ltr ? in.left : menuWidth - in.right;
    int innerH = menuHeight - in.top - in.bottom;
    if (col[0] > 0 && checkIcon != null) {
      int ix = ltr ? x : x - checkIcon.getIconWidth();
      checkIcon.paintIcon(mi, g, ix, in.top + (innerH - checkIcon.getIconHeight()) / 2);
      x += ltr ? col[0] + gap : -(col[0] + gap);
    }
    Icon icon = mi.getIcon();
    if (icon != null) {
      int ix = ltr ? x : x - icon.getIconWidth();
      icon.paintIcon(mi, g, ix, in.top + (innerH - icon.getIconHeight()) / 2);
      x += ltr ? icon.getIconWidth() + gap : -(icon.getIconWidth() + gap);
    }
    String text = mi.getText();
    if (text != null && !text.isEmpty()) {
      int tx = ltr ? x : x - col[5];
      Rectangle textRect = new Rectangle(tx, in.top, col[5], innerH);
      g.setColor(fg);
      paintText(g, mi, textRect, text);
    }
    if (col[2] > 0) {
      String acc = acceleratorText();
      g.setFont(acceleratorFont);
      g.setColor(enabled ? (selected ? acceleratorSelectionForeground : acceleratorForeground) : disabledForeground);
      FontMetrics fma = mi.getFontMetrics(acceleratorFont);
      int arrowW = col[3] > 0 ? col[3] + gap : 0;
      int ax = ltr ? menuWidth - in.right - arrowW - col[2] : in.left + arrowW;
      g.drawString(acc, ax, in.top + (innerH - fma.getHeight()) / 2 + fma.getAscent());
    }
    if (col[3] > 0 && arrowIcon != null && !top) {
      int ax = ltr ? menuWidth - in.right - arrowIcon.getIconWidth() : in.left;
      g.setColor(fg);
      arrowIcon.paintIcon(mi, g, ax, in.top + (innerH - arrowIcon.getIconHeight()) / 2);
    }
    g.setColor(holdc);
    g.setFont(holdf);
  }

  protected class MouseInputHandler implements MouseInputListener {
    private boolean press;
    public void mouseClicked(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
      if (!menuItem.isEnabled()) {
        return;
      }
      MenuSelectionManager manager = MenuSelectionManager.defaultManager();
      Point p = e.getPoint();
      if (p.x >= 0 && p.x < menuItem.getWidth() && p.y >= 0 && p.y < menuItem.getHeight()) {
        doClick(manager);
      } else {
        manager.processMouseEvent(e);
      }
    }
    public void mouseEntered(MouseEvent e) {
      MenuSelectionManager manager = MenuSelectionManager.defaultManager();
      int modifiers = e.getModifiers();
      if ((modifiers & (java.awt.event.InputEvent.BUTTON1_MASK | java.awt.event.InputEvent.BUTTON2_MASK
          | java.awt.event.InputEvent.BUTTON3_MASK)) != 0) {
        MenuSelectionManager.defaultManager().processMouseEvent(e);
      } else {
        manager.setSelectedPath(getPath());
      }
    }
    public void mouseExited(MouseEvent e) {
      MenuSelectionManager manager = MenuSelectionManager.defaultManager();
      int modifiers = e.getModifiers();
      if ((modifiers & (java.awt.event.InputEvent.BUTTON1_MASK | java.awt.event.InputEvent.BUTTON2_MASK
          | java.awt.event.InputEvent.BUTTON3_MASK)) != 0) {
        MenuSelectionManager.defaultManager().processMouseEvent(e);
      } else {
        MenuElement[] path = manager.getSelectedPath();
        if (path.length > 1 && path[path.length - 1] == menuItem) {
          MenuElement[] newPath = new MenuElement[path.length - 1];
          System.arraycopy(path, 0, newPath, 0, path.length - 1);
          manager.setSelectedPath(newPath);
        }
      }
    }
    public void mouseDragged(MouseEvent e) {
      MenuSelectionManager.defaultManager().processMouseEvent(e);
    }
    public void mouseMoved(MouseEvent e) {
    }
  }
}
