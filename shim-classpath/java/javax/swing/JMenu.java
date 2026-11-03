/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class JMenu extends JMenuItem implements Accessible, MenuElement {
  private JPopupMenu popupMenu;
  private ChangeListener menuChangeListener = null;
  private MenuEvent menuEvent = null;
  private int delay = 200;
  private Point customMenuLocation = null;
  protected WinListener popupListener;

  public JMenu() { this(""); }

  public JMenu(String s) { super(s); }

  public JMenu(Action a) {
    this();
    setAction(a);
  }

  public JMenu(String s, boolean b) { this(s); }

  void initFocusability() { }

  public void updateUI() {
    setUI((javax.swing.plaf.MenuItemUI) UIManager.getUI(this));
    if (popupMenu != null) {
      popupMenu.setUI((javax.swing.plaf.PopupMenuUI) UIManager.getUI(popupMenu));
    }
  }

  public String getUIClassID() { return "MenuUI"; }

  public void setModel(ButtonModel newModel) {
    ButtonModel oldModel = getModel();
    super.setModel(newModel);
    if (oldModel != null && menuChangeListener != null) {
      oldModel.removeChangeListener(menuChangeListener);
      menuChangeListener = null;
    }
    model = newModel;
    if (newModel != null) {
      menuChangeListener = createMenuChangeListener();
      newModel.addChangeListener(menuChangeListener);
    }
  }

  public boolean isSelected() { return getModel().isSelected(); }

  public void setSelected(boolean b) {
    ButtonModel model = getModel();
    boolean oldValue = model.isSelected();
    if (b != model.isSelected()) {
      getModel().setSelected(b);
    }
  }

  public boolean isPopupMenuVisible() {
    ensurePopupMenuCreated();
    return popupMenu.isVisible();
  }

  public void setPopupMenuVisible(boolean b) {
    boolean isVisible = isPopupMenuVisible();
    if (b != isVisible && (isEnabled() || !b)) {
      ensurePopupMenuCreated();
      if ((b == true) && isShowing()) {
        Point p = customMenuLocation;
        if (p == null) {
          p = getPopupMenuOrigin();
        }
        getPopupMenu().show(this, p.x, p.y);
      } else {
        getPopupMenu().setVisible(false);
      }
    }
  }

  protected Point getPopupMenuOrigin() {
    int x;
    int y;
    JPopupMenu pm = getPopupMenu();
    java.awt.Dimension s = getSize();
    java.awt.Dimension pmSize = pm.getSize();
    if (pmSize.width == 0) {
      pmSize = pm.getPreferredSize();
    }
    java.awt.Container parent = getParent();
    if (parent instanceof JPopupMenu) {
      x = s.width;
      y = 0;
    } else {
      x = 0;
      y = s.height;
    }
    return new Point(x, y);
  }

  public int getDelay() { return delay; }

  public void setDelay(int d) {
    if (d < 0) {
      throw new IllegalArgumentException("Delay must be a positive integer");
    }
    delay = d;
  }

  private void ensurePopupMenuCreated() {
    if (popupMenu == null) {
      this.popupMenu = new JPopupMenu();
      popupMenu.setInvoker(this);
      popupListener = createWinListener(popupMenu);
    }
  }

  public void setMenuLocation(int x, int y) {
    customMenuLocation = new Point(x, y);
    if (popupMenu != null) {
      popupMenu.setLocation(x, y);
    }
  }

  public JMenuItem add(JMenuItem menuItem) {
    ensurePopupMenuCreated();
    return popupMenu.add(menuItem);
  }

  public Component add(Component c) {
    ensurePopupMenuCreated();
    popupMenu.add(c);
    return c;
  }

  public Component add(Component c, int index) {
    ensurePopupMenuCreated();
    popupMenu.insert(c, index);
    return c;
  }

  public JMenuItem add(String s) { return add(new JMenuItem(s)); }

  public JMenuItem add(Action a) {
    JMenuItem mi = createActionComponent(a);
    mi.setAction(a);
    add(mi);
    return mi;
  }

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

  public void addSeparator() {
    ensurePopupMenuCreated();
    popupMenu.addSeparator();
  }

  public void insert(String s, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    ensurePopupMenuCreated();
    popupMenu.insert(new JMenuItem(s), pos);
  }

  public JMenuItem insert(JMenuItem mi, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    ensurePopupMenuCreated();
    popupMenu.insert(mi, pos);
    return mi;
  }

  public JMenuItem insert(Action a, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    ensurePopupMenuCreated();
    JMenuItem mi = new JMenuItem(a);
    mi.setHorizontalTextPosition(JButton.TRAILING);
    mi.setVerticalTextPosition(JButton.CENTER);
    popupMenu.insert(mi, pos);
    return mi;
  }

  public void insertSeparator(int index) {
    if (index < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    ensurePopupMenuCreated();
    popupMenu.insert(new JPopupMenu.Separator(), index);
  }

  public JMenuItem getItem(int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    Component c = getMenuComponent(pos);
    if (c instanceof JMenuItem) {
      JMenuItem mi = (JMenuItem) c;
      return mi;
    }
    return null;
  }

  public int getItemCount() { return getMenuComponentCount(); }

  public boolean isTearOff() { throw new Error("boolean isTearOff() {} not yet implemented"); }

  public void remove(JMenuItem item) {
    if (popupMenu != null) {
      popupMenu.remove(item);
    }
  }

  public void remove(int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException("index less than zero.");
    }
    if (pos > getItemCount()) {
      throw new IllegalArgumentException("index greater than the number of items.");
    }
    if (popupMenu != null) {
      popupMenu.remove(pos);
    }
  }

  public void remove(Component c) {
    if (popupMenu != null) {
      popupMenu.remove(c);
    }
  }

  public void removeAll() {
    if (popupMenu != null) {
      popupMenu.removeAll();
    }
  }

  public int getMenuComponentCount() {
    int componentCount = 0;
    if (popupMenu != null) {
      componentCount = popupMenu.getComponentCount();
    }
    return componentCount;
  }

  public Component getMenuComponent(int n) {
    if (popupMenu != null) {
      return popupMenu.getComponent(n);
    }
    return null;
  }

  public Component[] getMenuComponents() {
    if (popupMenu != null) {
      return popupMenu.getComponents();
    }
    return new Component[0];
  }

  public boolean isTopLevelMenu() { return getParent() instanceof JMenuBar; }

  public boolean isMenuComponent(Component c) {
    if (c == this) {
      return true;
    }
    if (c instanceof JPopupMenu) {
      JPopupMenu comp = (JPopupMenu) c;
      if (comp == this.getPopupMenu()) {
        return true;
      }
    }
    int ncomponents = this.getMenuComponentCount();
    Component[] component = this.getMenuComponents();
    for (int i = 0; i < ncomponents; i++) {
      Component comp = component[i];
      if (comp == c) {
        return true;
      }
      if (comp instanceof JMenu) {
        JMenu subMenu = (JMenu) comp;
        if (subMenu.isMenuComponent(c)) {
          return true;
        }
      }
    }
    return false;
  }

  public JPopupMenu getPopupMenu() {
    ensurePopupMenuCreated();
    return popupMenu;
  }

  public void addMenuListener(MenuListener l) { listenerList.add(MenuListener.class, l); }

  public void removeMenuListener(MenuListener l) { listenerList.remove(MenuListener.class, l); }

  public MenuListener[] getMenuListeners() { return listenerList.getListeners(MenuListener.class); }

  protected void fireMenuSelected() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuListener.class) {
        if (listeners[i + 1] == null) {
          throw new Error(getText() + " has a NULL Listener!! " + i);
        } else {
          if (menuEvent == null) {
            menuEvent = new MenuEvent(this);
          }
          ((MenuListener) listeners[i + 1]).menuSelected(menuEvent);
        }
      }
    }
  }

  protected void fireMenuDeselected() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuListener.class) {
        if (menuEvent == null) {
          menuEvent = new MenuEvent(this);
        }
        ((MenuListener) listeners[i + 1]).menuDeselected(menuEvent);
      }
    }
  }

  protected void fireMenuCanceled() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuListener.class) {
        if (menuEvent == null) {
          menuEvent = new MenuEvent(this);
        }
        ((MenuListener) listeners[i + 1]).menuCanceled(menuEvent);
      }
    }
  }

  private ChangeListener createMenuChangeListener() { return new MenuChangeListener(); }

  private class MenuChangeListener implements ChangeListener, Serializable {
    boolean isSelected = false;

    public void stateChanged(ChangeEvent e) {
      ButtonModel model = (ButtonModel) e.getSource();
      boolean modelSelected = model.isSelected();
      if (modelSelected != isSelected) {
        if (modelSelected == true) {
          fireMenuSelected();
        } else {
          fireMenuDeselected();
        }
        isSelected = modelSelected;
      }
    }
  }

  protected WinListener createWinListener(JPopupMenu p) { return new WinListener(p); }

  protected class WinListener extends WindowAdapter implements Serializable {
    JPopupMenu popupMenu;

    public WinListener(JPopupMenu p) { this.popupMenu = p; }

    public void windowClosing(WindowEvent e) { setSelected(false); }
  }

  public void menuSelectionChanged(boolean isIncluded) { setSelected(isIncluded); }

  public MenuElement[] getSubElements() {
    if (popupMenu == null) {
      return new MenuElement[0];
    } else {
      MenuElement[] result = new MenuElement[1];
      result[0] = popupMenu;
      return result;
    }
  }

  public Component getComponent() { return this; }

  public void applyComponentOrientation(ComponentOrientation o) {
    super.applyComponentOrientation(o);
    if (popupMenu != null) {
      int ncomponents = getMenuComponentCount();
      for (int i = 0; i < ncomponents; ++i) {
        getMenuComponent(i).applyComponentOrientation(o);
      }
      popupMenu.setComponentOrientation(o);
    }
  }

  public void setComponentOrientation(ComponentOrientation o) {
    super.setComponentOrientation(o);
    if (popupMenu != null) {
      popupMenu.setComponentOrientation(o);
    }
  }

  public void setAccelerator(KeyStroke keyStroke) {
    throw new Error("setAccelerator() is not defined for JMenu.  Use setMnemonic() instead.");
  }

  protected void processKeyEvent(KeyEvent evt) {
    MenuSelectionManager.defaultManager().processKeyEvent(evt);
    if (evt.isConsumed()) {
      return;
    }
    super.processKeyEvent(evt);
  }

  public void doClick(int pressTime) {
    MenuElement[] me = buildMenuElementArray(this);
    MenuSelectionManager.defaultManager().setSelectedPath(me);
  }

  private MenuElement[] buildMenuElementArray(JMenu leaf) {
    java.util.Vector<MenuElement> elements = new java.util.Vector<MenuElement>();
    Component current = leaf.getPopupMenu();
    JPopupMenu pop;
    JMenu menu;
    JMenuBar bar;
    while (true) {
      if (current instanceof JPopupMenu) {
        pop = (JPopupMenu) current;
        elements.insertElementAt(pop, 0);
        current = pop.getInvoker();
      } else if (current instanceof JMenu) {
        menu = (JMenu) current;
        elements.insertElementAt(menu, 0);
        current = menu.getParent();
      } else if (current instanceof JMenuBar) {
        bar = (JMenuBar) current;
        elements.insertElementAt(bar, 0);
        MenuElement[] me = new MenuElement[elements.size()];
        elements.copyInto(me);
        return me;
      } else {
        MenuElement[] me = new MenuElement[elements.size()];
        elements.copyInto(me);
        return me;
      }
    }
  }

  protected String paramString() { return super.paramString(); }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJMenu();
    }
    return accessibleContext;
  }

  protected class AccessibleJMenu extends AccessibleJMenuItem implements javax.accessibility.AccessibleSelection {
    protected AccessibleJMenu() { }

    public int getAccessibleChildrenCount() {
      Component[] children = getMenuComponents();
      int count = 0;
      for (Component child : children) {
        if (child instanceof Accessible) {
          count++;
        }
      }
      return count;
    }

    public Accessible getAccessibleChild(int i) {
      Component[] children = getMenuComponents();
      int count = 0;
      for (Component child : children) {
        if (child instanceof Accessible) {
          if (count == i) {
            return (Accessible) child;
          }
          count++;
        }
      }
      return null;
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.MENU; }

    public javax.accessibility.AccessibleSelection getAccessibleSelection() { return this; }

    public int getAccessibleSelectionCount() {
      MenuElement[] me = MenuSelectionManager.defaultManager().getSelectedPath();
      if (me != null) {
        for (int i = 0; i < me.length; i++) {
          if (me[i] == JMenu.this) {
            if (i + 1 < me.length) {
              return 1;
            }
          }
        }
      }
      return 0;
    }

    public Accessible getAccessibleSelection(int i) {
      if (i < 0 || i >= getItemCount()) {
        return null;
      }
      MenuElement[] me = MenuSelectionManager.defaultManager().getSelectedPath();
      if (me != null) {
        for (int j = 0; j < me.length; j++) {
          if (me[j] == JMenu.this) {
            while (++j < me.length) {
              if (me[j] instanceof JMenuItem) {
                return (Accessible) me[j];
              }
            }
          }
        }
      }
      return null;
    }

    public boolean isAccessibleChildSelected(int i) {
      MenuElement[] me = MenuSelectionManager.defaultManager().getSelectedPath();
      if (me != null) {
        JMenuItem mi = JMenu.this.getItem(i);
        for (int j = 0; j < me.length; j++) {
          if (me[j] == mi) {
            return true;
          }
        }
      }
      return false;
    }

    public void addAccessibleSelection(int i) {
      if (i < 0 || i >= getItemCount()) {
        return;
      }
      JMenuItem mi = getItem(i);
      if (mi != null) {
        if (mi instanceof JMenu) {
          MenuElement[] me = buildMenuElementArray((JMenu) mi);
          MenuSelectionManager.defaultManager().setSelectedPath(me);
        } else {
          MenuSelectionManager.defaultManager().setSelectedPath(null);
          mi.setArmed(true);
        }
      }
    }

    public void removeAccessibleSelection(int i) {
      if (i < 0 || i >= getItemCount()) {
        return;
      }
      JMenuItem mi = getItem(i);
      if (mi != null && mi instanceof JMenu) {
        if (((JMenu) mi).isSelected()) {
          MenuElement[] old = MenuSelectionManager.defaultManager().getSelectedPath();
          MenuElement[] me = new MenuElement[old.length - 2];
          for (int j = 0; j < old.length - 2; j++) {
            me[j] = old[j];
          }
          MenuSelectionManager.defaultManager().setSelectedPath(me);
        }
      } else if (mi != null) {
        mi.setArmed(false);
      }
    }

    public void clearAccessibleSelection() {
      MenuElement[] old = MenuSelectionManager.defaultManager().getSelectedPath();
      if (old != null) {
        for (int j = 0; j < old.length; j++) {
          if (old[j] == JMenu.this) {
            MenuElement[] me = new MenuElement[j + 1];
            System.arraycopy(old, 0, me, 0, j);
            me[j] = JMenu.this.getPopupMenu();
            MenuSelectionManager.defaultManager().setSelectedPath(me);
          }
        }
      }
    }

    public void selectAllAccessibleSelection() { }
  }
}
