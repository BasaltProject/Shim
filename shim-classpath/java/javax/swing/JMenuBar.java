/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleStateSet;
import javax.swing.plaf.MenuBarUI;
import javax.swing.plaf.basic.DefaultMenuLayout;

public class JMenuBar extends JComponent implements Accessible, MenuElement {
  private transient SingleSelectionModel selectionModel;
  private boolean paintBorder = true;
  private Insets margin = null;

  public JMenuBar() {
    super();
    setFocusTraversalKeysEnabled(false);
    setSelectionModel(new DefaultSingleSelectionModel());
    updateUI();
  }

  public MenuBarUI getUI() { return (MenuBarUI) ui; }

  public void setUI(MenuBarUI ui) { super.setUI(ui); }

  public void updateUI() {
    setUI((MenuBarUI) UIManager.getUI(this));
    if (getLayout() == null) {
      setLayout(new DefaultMenuLayout(this, BoxLayout.LINE_AXIS));
    }
  }

  public String getUIClassID() { return "MenuBarUI"; }

  public SingleSelectionModel getSelectionModel() { return selectionModel; }

  public void setSelectionModel(SingleSelectionModel model) {
    SingleSelectionModel oldValue = selectionModel;
    this.selectionModel = model;
    firePropertyChange("selectionModel", oldValue, selectionModel);
  }

  public JMenu add(JMenu c) {
    super.add(c);
    return c;
  }

  public JMenu getMenu(int index) {
    Component c = getComponentAtIndex(index);
    if (c instanceof JMenu) {
      return (JMenu) c;
    }
    return null;
  }

  public int getMenuCount() { return getComponentCount(); }

  public void setHelpMenu(JMenu menu) { throw new Error("setHelpMenu() not yet implemented."); }

  public JMenu getHelpMenu() { throw new Error("getHelpMenu() not yet implemented."); }

  public Component getComponentAtIndex(int i) {
    int ncomponents = this.getComponentCount();
    if (i >= 0 && i < ncomponents) {
      Component[] component = this.getComponents();
      return component[i];
    }
    return null;
  }

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

  public void setSelected(Component sel) {
    SingleSelectionModel model = getSelectionModel();
    int index = getComponentIndex(sel);
    model.setSelectedIndex(index);
  }

  public boolean isSelected() { return selectionModel.isSelected(); }

  public boolean isBorderPainted() { return paintBorder; }

  public void setBorderPainted(boolean b) {
    boolean oldValue = paintBorder;
    paintBorder = b;
    firePropertyChange("borderPainted", oldValue, paintBorder);
    if (b != oldValue) {
      revalidate();
      repaint();
    }
  }

  protected void paintBorder(Graphics g) {
    if (isBorderPainted()) {
      super.paintBorder(g);
    }
  }

  public void setMargin(Insets m) {
    Insets old = margin;
    this.margin = m;
    firePropertyChange("margin", old, m);
    if (old == null || !old.equals(m)) {
      revalidate();
      repaint();
    }
  }

  public Insets getMargin() {
    if (margin == null) {
      return new Insets(0, 0, 0, 0);
    } else {
      return margin;
    }
  }

  public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) { }

  public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) { }

  public void menuSelectionChanged(boolean isIncluded) { }

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

  protected String paramString() {
    String paintBorderString = (paintBorder ? "true" : "false");
    String marginString = (margin != null ? margin.toString() : "");
    return super.paramString() + ",margin=" + marginString + ",paintBorder=" + paintBorderString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJMenuBar();
    }
    return accessibleContext;
  }

  protected class AccessibleJMenuBar extends AccessibleJComponent implements AccessibleSelection {
    protected AccessibleJMenuBar() { }

    public AccessibleStateSet getAccessibleStateSet() {
      AccessibleStateSet states = super.getAccessibleStateSet();
      return states;
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.MENU_BAR; }

    public AccessibleSelection getAccessibleSelection() { return this; }

    public int getAccessibleSelectionCount() {
      if (JMenuBar.this.isSelected()) {
        return 1;
      }
      return 0;
    }

    public Accessible getAccessibleSelection(int i) {
      if (JMenuBar.this.isSelected()) {
        if (i != 0) {
          return null;
        }
        int j = JMenuBar.this.getSelectionModel().getSelectedIndex();
        if (JMenuBar.this.getComponentAtIndex(j) instanceof Accessible) {
          return (Accessible) JMenuBar.this.getComponentAtIndex(j);
        }
      }
      return null;
    }

    public boolean isAccessibleChildSelected(int i) { return i == JMenuBar.this.getSelectionModel().getSelectedIndex(); }

    public void addAccessibleSelection(int i) {
      int j = JMenuBar.this.getSelectionModel().getSelectedIndex();
      if (i == j) {
        return;
      }
      if (j >= 0 && j < JMenuBar.this.getMenuCount()) {
        JMenu menu = JMenuBar.this.getMenu(j);
        if (menu != null) {
          MenuSelectionManager.defaultManager().setSelectedPath(null);
        }
      }
      JMenuBar.this.getSelectionModel().setSelectedIndex(i);
      JMenu menu = JMenuBar.this.getMenu(i);
      if (menu != null) {
        MenuElement[] me = new MenuElement[3];
        me[0] = JMenuBar.this;
        me[1] = menu;
        me[2] = menu.getPopupMenu();
        MenuSelectionManager.defaultManager().setSelectedPath(me);
      }
    }

    public void removeAccessibleSelection(int i) {
      if (i >= 0 && i < getMenuCount()) {
        JMenu menu = getMenu(i);
        if (menu != null) {
          MenuSelectionManager.defaultManager().setSelectedPath(null);
        }
        getSelectionModel().setSelectedIndex(-1);
      }
    }

    public void clearAccessibleSelection() {
      int i = getSelectionModel().getSelectedIndex();
      if (i >= 0 && i < getMenuCount()) {
        JMenu menu = getMenu(i);
        if (menu != null) {
          MenuSelectionManager.defaultManager().setSelectedPath(null);
        }
      }
      getSelectionModel().setSelectedIndex(-1);
    }

    public void selectAllAccessibleSelection() { }
  }
}
