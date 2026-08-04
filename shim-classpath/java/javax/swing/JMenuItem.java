/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.plaf.MenuItemUI;

public class JMenuItem extends AbstractButton implements Accessible, MenuElement {
  private KeyStroke accelerator;

  public JMenuItem() { this(null, (Icon) null); }

  public JMenuItem(Icon icon) { this(null, icon); }

  public JMenuItem(String text) { this(text, (Icon) null); }

  public JMenuItem(Action a) {
    this();
    setAction(a);
  }

  public JMenuItem(String text, Icon icon) {
    setModel(new DefaultButtonModel());
    init(text, icon);
    initFocusability();
  }

  public JMenuItem(String text, int mnemonic) {
    setModel(new DefaultButtonModel());
    init(text, null);
    setMnemonic(mnemonic);
    initFocusability();
  }

  public void setModel(ButtonModel newModel) { super.setModel(newModel); }

  void initFocusability() { setFocusable(false); }

  protected void init(String text, Icon icon) {
    if (text != null) {
      setText(text);
    }
    if (icon != null) {
      setIcon(icon);
    }
    setUIProperty("borderPainted", Boolean.FALSE);
    setFocusPainted(false);
    setHorizontalTextPosition(TRAILING);
    setHorizontalAlignment(LEADING);
    updateUI();
  }

  public void setUI(MenuItemUI ui) { super.setUI(ui); }

  public void updateUI() { setUI((MenuItemUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "MenuItemUI"; }

  public void setArmed(boolean b) {
    ButtonModel model = getModel();
    if (model.isArmed() != b) {
      model.setArmed(b);
    }
  }

  public boolean isArmed() {
    ButtonModel model = getModel();
    return model.isArmed();
  }

  public void setEnabled(boolean b) {
    if (!b && !UIManager.getBoolean("MenuItem.disabledAreNavigable")) {
      setArmed(false);
    }
    super.setEnabled(b);
  }

  public void setAccelerator(KeyStroke keyStroke) {
    KeyStroke oldAccelerator = accelerator;
    this.accelerator = keyStroke;
    repaint();
    revalidate();
    firePropertyChange("accelerator", oldAccelerator, accelerator);
  }

  public KeyStroke getAccelerator() { return this.accelerator; }

  protected void configurePropertiesFromAction(Action a) {
    super.configurePropertiesFromAction(a);
    configureAcceleratorFromAction(a);
  }

  void configureAcceleratorFromAction(Action a) {
    KeyStroke ks = (a == null) ? null : (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
    setAccelerator(ks);
  }

  protected void actionPropertyChanged(Action action, String propertyName) {
    if (propertyName == Action.ACCELERATOR_KEY) {
      configureAcceleratorFromAction(action);
    } else {
      super.actionPropertyChanged(action, propertyName);
    }
  }

  public void processMouseEvent(MouseEvent e, MenuElement[] path, MenuSelectionManager manager) {
    processMenuDragMouseEvent(new MenuDragMouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(),
        e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), path, manager));
  }

  public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
    MenuKeyEvent mke = new MenuKeyEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), e.getKeyCode(),
        e.getKeyChar(), path, manager);
    processMenuKeyEvent(mke);
    if (mke.isConsumed()) {
      e.consume();
    }
  }

  public void processMenuDragMouseEvent(MenuDragMouseEvent e) {
    switch (e.getID()) {
      case MouseEvent.MOUSE_ENTERED:
        isMouseDragged = false;
        fireMenuDragMouseEntered(e);
        break;
      case MouseEvent.MOUSE_EXITED:
        isMouseDragged = false;
        fireMenuDragMouseExited(e);
        break;
      case MouseEvent.MOUSE_DRAGGED:
        isMouseDragged = true;
        fireMenuDragMouseDragged(e);
        break;
      case MouseEvent.MOUSE_RELEASED:
        if (isMouseDragged) {
          fireMenuDragMouseReleased(e);
        }
        break;
      default:
        break;
    }
  }

  private boolean isMouseDragged = false;

  public void processMenuKeyEvent(MenuKeyEvent e) {
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

  protected void fireMenuDragMouseEntered(MenuDragMouseEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuDragMouseListener.class) {
        ((MenuDragMouseListener) listeners[i + 1]).menuDragMouseEntered(event);
      }
    }
  }

  protected void fireMenuDragMouseExited(MenuDragMouseEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuDragMouseListener.class) {
        ((MenuDragMouseListener) listeners[i + 1]).menuDragMouseExited(event);
      }
    }
  }

  protected void fireMenuDragMouseDragged(MenuDragMouseEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuDragMouseListener.class) {
        ((MenuDragMouseListener) listeners[i + 1]).menuDragMouseDragged(event);
      }
    }
  }

  protected void fireMenuDragMouseReleased(MenuDragMouseEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuDragMouseListener.class) {
        ((MenuDragMouseListener) listeners[i + 1]).menuDragMouseReleased(event);
      }
    }
  }

  protected void fireMenuKeyPressed(MenuKeyEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuKeyListener.class) {
        ((MenuKeyListener) listeners[i + 1]).menuKeyPressed(event);
      }
    }
  }

  protected void fireMenuKeyReleased(MenuKeyEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuKeyListener.class) {
        ((MenuKeyListener) listeners[i + 1]).menuKeyReleased(event);
      }
    }
  }

  protected void fireMenuKeyTyped(MenuKeyEvent event) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == MenuKeyListener.class) {
        ((MenuKeyListener) listeners[i + 1]).menuKeyTyped(event);
      }
    }
  }

  public void menuSelectionChanged(boolean isIncluded) { setArmed(isIncluded); }

  public MenuElement[] getSubElements() { return new MenuElement[0]; }

  public Component getComponent() { return this; }

  public void addMenuDragMouseListener(MenuDragMouseListener l) { listenerList.add(MenuDragMouseListener.class, l); }

  public void removeMenuDragMouseListener(MenuDragMouseListener l) {
    listenerList.remove(MenuDragMouseListener.class, l);
  }

  public MenuDragMouseListener[] getMenuDragMouseListeners() {
    return listenerList.getListeners(MenuDragMouseListener.class);
  }

  public void addMenuKeyListener(MenuKeyListener l) { listenerList.add(MenuKeyListener.class, l); }

  public void removeMenuKeyListener(MenuKeyListener l) { listenerList.remove(MenuKeyListener.class, l); }

  public MenuKeyListener[] getMenuKeyListeners() { return listenerList.getListeners(MenuKeyListener.class); }

  protected String paramString() { return super.paramString(); }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJMenuItem();
    }
    return accessibleContext;
  }

  protected class AccessibleJMenuItem extends AccessibleAbstractButton implements javax.swing.event.ChangeListener {
    AccessibleJMenuItem() {
      super();
      JMenuItem.this.addChangeListener(this);
    }

    public void stateChanged(javax.swing.event.ChangeEvent e) {
      firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY, Boolean.valueOf(false),
          Boolean.valueOf(true));
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.MENU_ITEM; }
  }
}
