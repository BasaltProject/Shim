/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.PopupMenuUI;
import javax.swing.plaf.basic.DefaultMenuLayout;

public class JPopupMenu extends JComponent implements Accessible, MenuElement {
  private static boolean defaultLWPopupEnabledKey = true;

  transient Component invoker;
  transient boolean popupShowing;
  private String label = null;
  private boolean paintBorder = true;
  private Point desiredLocationPoint;
  private boolean lightWeightPopup = true;
  private SingleSelectionModel selectionModel;
  private Dimension popupSize;

  public static void setDefaultLightWeightPopupEnabled(boolean aFlag) { defaultLWPopupEnabledKey = aFlag; }

  public static boolean getDefaultLightWeightPopupEnabled() { return defaultLWPopupEnabledKey; }

  public JPopupMenu() { this(null); }

  public JPopupMenu(String label) {
    this.label = label;
    lightWeightPopup = getDefaultLightWeightPopupEnabled();
    setSelectionModel(new DefaultSingleSelectionModel());
    setFocusTraversalKeysEnabled(false);
    updateUI();
  }

  public PopupMenuUI getUI() { return (PopupMenuUI) ui; }

  public void setUI(PopupMenuUI ui) { super.setUI(ui); }

  public void updateUI() {
    setUI((PopupMenuUI) UIManager.getUI(this));
    if (getLayout() == null) {
      setLayout(new DefaultMenuLayout(this, BoxLayout.Y_AXIS));
    }
  }

  public String getUIClassID() { return "PopupMenuUI"; }

  protected void processFocusEvent(FocusEvent evt) { super.processFocusEvent(evt); }

  protected void processKeyEvent(KeyEvent evt) {
    MenuSelectionManager.defaultManager().processKeyEvent(evt);
    if (evt.isConsumed()) {
      return;
    }
    super.processKeyEvent(evt);
  }

  public SingleSelectionModel getSelectionModel() { return selectionModel; }

  public void setSelectionModel(SingleSelectionModel model) { selectionModel = model; }

  public JMenuItem add(JMenuItem menuItem) {
    super.add(menuItem);
    return menuItem;
  }

  public JMenuItem add(String s) { return add(new JMenuItem(s)); }

  public JMenuItem add(Action a) {
    JMenuItem mi = createActionComponent(a);
    mi.setAction(a);
    add(mi);
    return mi;
  }

  Point adjustPopupLocationToFitScreen(int xPosition, int yPosition) { return new Point(xPosition, yPosition); }

  protected JMenuItem createActionComponent(Action a) {
    JMenuItem mi = new JMenuItem() {
      protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        PropertyChangeListener pcl = createActionChangeListener(this);
        if (pcl == null) {
          pcl = super.createActionPropertyChangeListener(a);
        }
        return pcl;
      }
    };
    mi.setHorizontalTextPosition(JButton.TRAILING);
    mi.setVerticalTextPosition(JButton.CENTER);
    return mi;
  }

  protected PropertyChangeListener createActionChangeListener(JMenuItem b) { return null; }

  public void remove(int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    if (pos > getComponentCount() - 1) {
      throw new IllegalArgumentException("index greater than the number of items.");
    }
    super.remove(pos);
  }

  public void setLightWeightPopupEnabled(boolean aFlag) { lightWeightPopup = aFlag; }

  public boolean isLightWeightPopupEnabled() { return lightWeightPopup; }

  public String getLabel() { return label; }

  public void setLabel(String label) {
    String oldValue = this.label;
    this.label = label;
    firePropertyChange("label", oldValue, label);
    if (accessibleContext != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY, oldValue, label);
    }
    invalidate();
    repaint();
  }

  public void addSeparator() { add(new JPopupMenu.Separator()); }

  public void insert(Action a, int index) {
    JMenuItem mi = createActionComponent(a);
    mi.setAction(a);
    insert(mi, index);
  }

  public void insert(Component component, int index) {
    if (index < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    int nitems = getComponentCount();
    Vector<Component> tempItems = new Vector<Component>();
    for (int i = index; i < nitems; i++) {
      tempItems.addElement(getComponent(index));
      remove(index);
    }
    add(component);
    for (Component tempItem : tempItems) {
      add(tempItem);
    }
  }

  public void addPopupMenuListener(PopupMenuListener l) { listenerList.add(PopupMenuListener.class, l); }

  public void removePopupMenuListener(PopupMenuListener l) { listenerList.remove(PopupMenuListener.class, l); }

  public PopupMenuListener[] getPopupMenuListeners() { return listenerList.getListeners(PopupMenuListener.class); }

  public void addMenuKeyListener(MenuKeyListener l) { listenerList.add(MenuKeyListener.class, l); }

  public void removeMenuKeyListener(MenuKeyListener l) { listenerList.remove(MenuKeyListener.class, l); }

  public MenuKeyListener[] getMenuKeyListeners() { return listenerList.getListeners(MenuKeyListener.class); }

  protected void firePopupMenuWillBecomeVisible() {
    Object[] listeners = listenerList.getListenerList();
    PopupMenuEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PopupMenuListener.class) {
        if (e == null) {
          e = new PopupMenuEvent(this);
        }
        ((PopupMenuListener) listeners[i + 1]).popupMenuWillBecomeVisible(e);
      }
    }
  }

  protected void firePopupMenuWillBecomeInvisible() {
    Object[] listeners = listenerList.getListenerList();
    PopupMenuEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PopupMenuListener.class) {
        if (e == null) {
          e = new PopupMenuEvent(this);
        }
        ((PopupMenuListener) listeners[i + 1]).popupMenuWillBecomeInvisible(e);
      }
    }
  }

  protected void firePopupMenuCanceled() {
    Object[] listeners = listenerList.getListenerList();
    PopupMenuEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PopupMenuListener.class) {
        if (e == null) {
          e = new PopupMenuEvent(this);
        }
        ((PopupMenuListener) listeners[i + 1]).popupMenuCanceled(e);
      }
    }
  }

  public void pack() {
    setSize(popupSize != null ? popupSize : getPreferredSize());
    validate();
  }

  public void setVisible(boolean b) {
    if (b == isVisible()) {
      return;
    }
    if (b == false) {
      Boolean doCanceled = (Boolean) getClientProperty("JPopupMenu.firePopupMenuCanceled");
      if (doCanceled != null && doCanceled == Boolean.TRUE) {
        putClientProperty("JPopupMenu.firePopupMenuCanceled", Boolean.FALSE);
        firePopupMenuCanceled();
      }
      getSelectionModel().clearSelection();
    } else {
      if (isPopupMenu()) {
        MenuElement[] me = new MenuElement[1];
        me[0] = this;
        MenuSelectionManager.defaultManager().setSelectedPath(me);
      }
    }
    if (b) {
      firePopupMenuWillBecomeVisible();
      popupShowing = true;
      firePropertyChange("visible", Boolean.FALSE, Boolean.TRUE);
    } else if (popupShowing) {
      firePopupMenuWillBecomeInvisible();
      popupShowing = false;
      firePropertyChange("visible", Boolean.TRUE, Boolean.FALSE);
      if (isPopupMenu()) {
        MenuSelectionManager.defaultManager().clearSelectedPath();
      }
    }
  }

  boolean isPopupMenu() {
    return ((invoker != null) && !(invoker instanceof JMenu));
  }

  public boolean isVisible() { return popupShowing; }

  public void setLocation(int x, int y) {
    int oldX = getX();
    int oldY = getY();
    if (oldX != x || oldY != y) {
      desiredLocationPoint = new Point(x, y);
      super.setLocation(x, y);
    }
  }

  public Component getInvoker() { return this.invoker; }

  public void setInvoker(Component invoker) {
    Component oldInvoker = this.invoker;
    this.invoker = invoker;
    if ((oldInvoker != this.invoker) && (ui != null)) {
      ui.uninstallUI(this);
      ui.installUI(this);
    }
    invalidate();
  }

  public void show(Component invoker, int x, int y) {
    setInvoker(invoker);
    Point invokerOrigin;
    if (invoker != null) {
      invokerOrigin = invoker.getLocationOnScreen();
      long lx = (long) invokerOrigin.x + (long) x;
      long ly = (long) invokerOrigin.y + (long) y;
      if (lx > Integer.MAX_VALUE) {
        lx = Integer.MAX_VALUE;
      }
      if (lx < Integer.MIN_VALUE) {
        lx = Integer.MIN_VALUE;
      }
      if (ly > Integer.MAX_VALUE) {
        ly = Integer.MAX_VALUE;
      }
      if (ly < Integer.MIN_VALUE) {
        ly = Integer.MIN_VALUE;
      }
      setLocation((int) lx, (int) ly);
    } else {
      setLocation(x, y);
    }
    setVisible(true);
  }

  public Component getComponentAtIndex(int i) { return getComponent(i); }

  public int getComponentIndex(Component c) {
    int ncomponents = this.getComponentCount();
    Component[] component = this.getComponents();
    for (int i = 0; i < ncomponents; i++) {
      Component comp = component[i];
      if (comp == c) {
        return i;
      }
    }
    return -1;
  }

  public void setPopupSize(Dimension d) {
    Dimension oldSize = getPreferredSize();
    popupSize = d;
    if (d != null) {
      setPreferredSize(d);
    }
    firePropertyChange("preferredSize", oldSize, d);
    invalidate();
    if (isVisible()) {
      pack();
    }
  }

  public void setPopupSize(int width, int height) { setPopupSize(new Dimension(width, height)); }

  public void setSelected(Component sel) {
    SingleSelectionModel model = getSelectionModel();
    int index = getComponentIndex(sel);
    model.setSelectedIndex(index);
  }

  public boolean isBorderPainted() { return paintBorder; }

  public void setBorderPainted(boolean b) {
    paintBorder = b;
    repaint();
  }

  protected void paintBorder(Graphics g) {
    if (isBorderPainted()) {
      super.paintBorder(g);
    }
  }

  public Insets getMargin() {
    if (margin == null) {
      return new Insets(0, 0, 0, 0);
    }
    return margin;
  }

  private Insets margin;

  boolean alwaysOnTop() { return true; }

  protected String paramString() {
    String labelString = (label != null ? label : "");
    String paintBorderString = (paintBorder ? "true" : "false");
    String marginString = (margin != null ? margin.toString() : "");
    String lightWeightPopupEnabledString = (isLightWeightPopupEnabled() ? "true" : "false");
    return super.paramString() + ",desiredLocation=" + desiredLocationPoint + ",label=" + labelString
        + ",margin=" + marginString + ",paintBorder=" + paintBorderString + ",lightWeightPopupEnabled="
        + lightWeightPopupEnabledString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJPopupMenu();
    }
    return accessibleContext;
  }

  protected class AccessibleJPopupMenu extends AccessibleJComponent {
    protected AccessibleJPopupMenu() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.POPUP_MENU; }
  }

  public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) { }

  public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
    MenuKeyEvent_dispatch(e, path, manager);
  }

  private void MenuKeyEvent_dispatch(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
    javax.swing.event.MenuKeyEvent mke = new javax.swing.event.MenuKeyEvent(e.getComponent(), e.getID(), e.getWhen(),
        e.getModifiersEx(), e.getKeyCode(), e.getKeyChar(), path, manager);
    processMenuKeyEvent(mke);
    if (mke.isConsumed()) {
      e.consume();
    }
  }

  private void processMenuKeyEvent(javax.swing.event.MenuKeyEvent e) {
    switch (e.getID()) {
      case KeyEvent.KEY_PRESSED:
        fireMenuKeyPressed(e);
        break;
      case KeyEvent.KEY_RELEASED:
        fireMenuKeyReleased(e);
        break;
      case KeyEvent.KEY_TYPED:
        fireMenuKeyTyped(e);
        break;
      default:
        break;
    }
  }

  private void fireMenuKeyPressed(javax.swing.event.MenuKeyEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuKeyListener.class) {
        ((MenuKeyListener) listeners[i + 1]).menuKeyPressed(event);
      }
    }
  }

  private void fireMenuKeyReleased(javax.swing.event.MenuKeyEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuKeyListener.class) {
        ((MenuKeyListener) listeners[i + 1]).menuKeyReleased(event);
      }
    }
  }

  private void fireMenuKeyTyped(javax.swing.event.MenuKeyEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuKeyListener.class) {
        ((MenuKeyListener) listeners[i + 1]).menuKeyTyped(event);
      }
    }
  }

  public void menuSelectionChanged(boolean isIncluded) {
    if (invoker instanceof JMenu) {
      JMenu m = (JMenu) invoker;
      if (isIncluded) {
        m.setPopupMenuVisible(true);
      } else {
        m.setPopupMenuVisible(false);
      }
    }
    if (isPopupMenu() && !isIncluded) {
      setVisible(false);
    }
  }

  public MenuElement[] getSubElements() {
    MenuElement[] result;
    Vector<MenuElement> tmp = new Vector<MenuElement>();
    int c = getComponentCount();
    for (int i = 0; i < c; i++) {
      Component m = getComponent(i);
      if (m instanceof MenuElement) {
        tmp.addElement((MenuElement) m);
      }
    }
    result = new MenuElement[tmp.size()];
    for (int i = 0, n = tmp.size(); i < n; i++) {
      result[i] = tmp.elementAt(i);
    }
    return result;
  }

  public Component getComponent() { return this; }

  public boolean isPopupTrigger(MouseEvent e) {
    return getUI().isPopupTrigger(e);
  }

  public static class Separator extends JSeparator {
    public Separator() { super(JSeparator.HORIZONTAL); }

    public String getUIClassID() { return "PopupMenuSeparatorUI"; }
  }
}
