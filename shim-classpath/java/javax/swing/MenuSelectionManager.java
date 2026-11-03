/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class MenuSelectionManager {
  private static final MenuSelectionManager instance = new MenuSelectionManager();
  private final List<MenuElement> selection = new ArrayList<MenuElement>();
  protected transient ChangeEvent changeEvent = null;
  protected EventListenerList listenerList = new EventListenerList();

  public MenuSelectionManager() { }

  public static MenuSelectionManager defaultManager() { return instance; }

  public void setSelectedPath(MenuElement[] path) {
    int currentSelectionCount = selection.size();
    int firstDifference = 0;
    if (path == null) {
      path = new MenuElement[0];
    }
    for (int i = 0, c = path.length; i < c; i++) {
      if (i < currentSelectionCount && selection.get(i) == path[i]) {
        firstDifference++;
      } else {
        break;
      }
    }
    for (int i = currentSelectionCount - 1; i >= firstDifference; i--) {
      MenuElement me = selection.remove(i);
      me.menuSelectionChanged(false);
    }
    for (int i = firstDifference, c = path.length; i < c; i++) {
      if (path[i] != null) {
        selection.add(path[i]);
        path[i].menuSelectionChanged(true);
      }
    }
    fireStateChanged();
  }

  public MenuElement[] getSelectedPath() { return selection.toArray(new MenuElement[0]); }

  public void clearSelectedPath() {
    if (selection.size() > 0) {
      setSelectedPath(null);
    }
  }

  public void addChangeListener(ChangeListener l) { listenerList.add(ChangeListener.class, l); }

  public void removeChangeListener(ChangeListener l) { listenerList.remove(ChangeListener.class, l); }

  public ChangeListener[] getChangeListeners() { return listenerList.getListeners(ChangeListener.class); }

  protected void fireStateChanged() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

  public void processMouseEvent(MouseEvent event) {
    if (selection.isEmpty()) {
      return;
    }
    if (event.getID() == MouseEvent.MOUSE_RELEASED) {
      Component source = event.getComponent();
      if (source == null || !isComponentPartOfCurrentMenu(source)) {
        clearSelectedPath();
      }
    }
  }

  public Component componentForPoint(Component source, Point sourcePoint) {
    Point screenPoint = new Point(sourcePoint);
    SwingUtilities.convertPointToScreen(screenPoint, source);
    for (int i = selection.size() - 1; i >= 0; i--) {
      Component c = selection.get(i).getComponent();
      if (c != null && c.isShowing()) {
        Point p = new Point(screenPoint);
        SwingUtilities.convertPointFromScreen(p, c);
        if (c.contains(p)) {
          return c;
        }
      }
    }
    return null;
  }

  public void processKeyEvent(KeyEvent e) {
    MenuElement[] sel = getSelectedPath();
    if (sel.length == 0) {
      return;
    }
    sel[sel.length - 1].processKeyEvent(e, sel, this);
  }

  public boolean isComponentPartOfCurrentMenu(Component c) {
    if (selection.size() > 0) {
      return isComponentPartOfMenu(selection.get(0), c);
    }
    return false;
  }

  private boolean isComponentPartOfMenu(MenuElement root, Component c) {
    if (root == null) {
      return false;
    }
    if (root.getComponent() == c) {
      return true;
    }
    MenuElement[] children = root.getSubElements();
    for (MenuElement child : children) {
      if (isComponentPartOfMenu(child, c)) {
        return true;
      }
    }
    return false;
  }
}
