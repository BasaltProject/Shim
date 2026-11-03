/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;

public class Container extends java.awt.Component {
  private final java.util.ArrayList<Component> component = new java.util.ArrayList<>();
  private LayoutManager layoutMgr;
  private final java.util.concurrent.CopyOnWriteArrayList<ContainerListener> containerListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private boolean focusCycleRoot;
  private FocusTraversalPolicy focusTraversalPolicy;

  public Container() {
  }
  public void remove(java.awt.Component p0) {
    synchronized (getTreeLock()) {
      int index = component.indexOf(p0);
      if (index >= 0) {
        remove(index);
      }
    }
  }
  public void remove(int p0) {
    synchronized (getTreeLock()) {
      if (p0 < 0 || p0 >= component.size()) {
        throw new ArrayIndexOutOfBoundsException(p0);
      }
      Component comp = component.remove(p0);
      if (layoutMgr != null) {
        layoutMgr.removeLayoutComponent(comp);
      }
      comp.parent = null;
      invalidate();
      dispatchEvent(new ContainerEvent(this, ContainerEvent.COMPONENT_REMOVED, comp));
    }
  }
  public void update(java.awt.Graphics p0) {
    paint(p0);
  }
  public java.awt.Component add(java.awt.Component p0) {
    addImpl(p0, null, -1);
    return p0;
  }
  public void add(java.awt.Component p0, java.lang.Object p1) {
    addImpl(p0, p1, -1);
  }
  public java.awt.Component add(java.awt.Component p0, int p1) {
    addImpl(p0, null, p1);
    return p0;
  }
  public void add(java.awt.Component p0, java.lang.Object p1, int p2) {
    addImpl(p0, p1, p2);
  }
  public java.awt.Component add(java.lang.String p0, java.awt.Component p1) {
    addImpl(p1, p0, -1);
    return p1;
  }
  public void list(java.io.PrintStream p0, int p1) {
    p0.println(" ".repeat(p1) + this);
    for (Component c : component) {
      c.list(p0, p1 + 1);
    }
  }
  public void list(java.io.PrintWriter p0, int p1) {
    p0.println(" ".repeat(p1) + this);
    for (Component c : component) {
      c.list(p0, p1 + 1);
    }
  }
  public void validate() {
    synchronized (getTreeLock()) {
      if (!valid) {
        validateTree();
      }
    }
  }
  public void print(java.awt.Graphics p0) {
    paint(p0);
  }
  protected java.lang.String paramString() {
    String str = super.paramString();
    LayoutManager lm = this.layoutMgr;
    if (lm != null) {
      str += ",layout=" + lm.getClass().getName();
    }
    return str;
  }
  public void removeAll() {
    synchronized (getTreeLock()) {
      while (!component.isEmpty()) {
        remove(component.size() - 1);
      }
    }
  }
  public java.awt.Component getComponent(int p0) {
    synchronized (getTreeLock()) {
      if ((p0 < 0) || (p0 >= component.size())) {
        throw new ArrayIndexOutOfBoundsException("No such child: " + p0);
      }
      return component.get(p0);
    }
  }
  public void invalidate() {
    LayoutManager lm = layoutMgr;
    if (lm instanceof LayoutManager2) {
      ((LayoutManager2) lm).invalidateLayout(this);
    }
    super.invalidate();
  }
  public boolean isValidateRoot() {
    return false;
  }
  public void setLayout(java.awt.LayoutManager p0) {
    layoutMgr = p0;
    invalidate();
  }
  public void addNotify() {
    
  }
  public void layout() {
    LayoutManager lm = layoutMgr;
    if (lm != null) {
      lm.layoutContainer(this);
    }
  }
  public int countComponents() {
    return component.size();
  }
  public java.awt.Component[] getComponents() {
    synchronized (getTreeLock()) {
      return component.toArray(new Component[0]);
    }
  }
  public java.awt.Insets insets() {
    return new Insets(0, 0, 0, 0);
  }
  public java.awt.Insets getInsets() {
    return insets();
  }
  protected void addImpl(java.awt.Component p0, java.lang.Object p1, int p2) {
    synchronized (getTreeLock()) {
      if (p2 > component.size() || (p2 < 0 && p2 != -1)) {
        throw new IllegalArgumentException("illegal component position");
      }
      if (p0 instanceof Container) {
        for (Container cn = this; cn != null; cn = cn.parent) {
          if (cn == p0) {
            throw new IllegalArgumentException("adding container's parent to itself");
          }
        }
      }
      if (p0 instanceof Window) {
        throw new IllegalArgumentException("adding a window to a container");
      }
      if (p0.parent != null) {
        p0.parent.remove(p0);
      }
      if (p2 == -1) {
        component.add(p0);
      } else {
        component.add(p2, p0);
      }
      p0.parent = this;
      invalidate();
      if (layoutMgr != null) {
        if (layoutMgr instanceof LayoutManager2) {
          ((LayoutManager2) layoutMgr).addLayoutComponent(p0, p1);
        } else if (p1 instanceof String) {
          layoutMgr.addLayoutComponent((String) p1, p0);
        }
      }
      ContainerEvent e = new ContainerEvent(this, ContainerEvent.COMPONENT_ADDED, p0);
      dispatchEvent(e);
    }
  }
  public int getComponentZOrder(java.awt.Component p0) {
    return component.indexOf(p0);
  }
  public void removeNotify() {
    
  }
  public boolean isFocusCycleRoot(java.awt.Container p0) {
    return focusCycleRoot && p0 == this || super.isFocusCycleRoot(p0);
  }
  public boolean isFocusCycleRoot() {
    return focusCycleRoot;
  }
  public java.awt.FocusTraversalPolicy getFocusTraversalPolicy() {
    if (!isFocusTraversalPolicyProvider() && !isFocusCycleRoot()) {
      return null;
    }
    java.awt.FocusTraversalPolicy policy = focusTraversalPolicy;
    if (policy != null) {
      return policy;
    }
    Container rootAncestor = getFocusCycleRootAncestor();
    if (rootAncestor != null) {
      return rootAncestor.getFocusTraversalPolicy();
    }
    return KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalPolicy();
  }
  public int getComponentCount() {
    return component.size();
  }
  protected void validateTree() {
    if (!valid) {
      doLayout();
      for (Component c : component.toArray(new Component[0])) {
        if (c instanceof Container) {
          ((Container) c).validateTree();
        } else {
          c.validate();
        }
      }
      valid = true;
    }
  }
  public void doLayout() {
    layout();
  }
  public void setFont(java.awt.Font p0) {
    super.setFont(p0);
  }
  public java.awt.Dimension preferredSize() {
    if (prefSize != null) {
      return new Dimension(prefSize);
    }
    LayoutManager lm = layoutMgr;
    if (lm != null) {
      return lm.preferredLayoutSize(this);
    }
    return super.preferredSize();
  }
  public java.awt.Dimension minimumSize() {
    if (minSize != null) {
      return new Dimension(minSize);
    }
    LayoutManager lm = layoutMgr;
    if (lm != null) {
      return lm.minimumLayoutSize(this);
    }
    return super.minimumSize();
  }
  public java.awt.Dimension getMaximumSize() {
    if (maxSize != null) {
      return new Dimension(maxSize);
    }
    LayoutManager lm = layoutMgr;
    if (lm instanceof LayoutManager2) {
      return ((LayoutManager2) lm).maximumLayoutSize(this);
    }
    return super.getMaximumSize();
  }
  public float getAlignmentX() {
    LayoutManager lm = layoutMgr;
    if (lm instanceof LayoutManager2) {
      return ((LayoutManager2) lm).getLayoutAlignmentX(this);
    }
    return super.getAlignmentX();
  }
  public float getAlignmentY() {
    LayoutManager lm = layoutMgr;
    if (lm instanceof LayoutManager2) {
      return ((LayoutManager2) lm).getLayoutAlignmentY(this);
    }
    return super.getAlignmentY();
  }
  public void paint(java.awt.Graphics p0) {
    paintComponents(p0);
  }
  public <T extends java.util.EventListener> T[] getListeners(java.lang.Class<T> p0) {
    EventListener[] l;
    if (p0 == ContainerListener.class) {
      l = containerListeners.toArray(new EventListener[0]);
    } else {
      return super.getListeners(p0);
    }
    T[] result = (T[]) java.lang.reflect.Array.newInstance(p0, l.length);
    for (int i = 0; i < l.length; i++) {
      result[i] = (T) l[i];
    }
    return result;
  }
  protected void processContainerEvent(java.awt.event.ContainerEvent p0) {
    for (ContainerListener l : containerListeners.toArray(new ContainerListener[0])) {
      if (p0.getID() == ContainerEvent.COMPONENT_ADDED) {
        l.componentAdded(p0);
      } else if (p0.getID() == ContainerEvent.COMPONENT_REMOVED) {
        l.componentRemoved(p0);
      }
    }
  }
  protected void processEvent(java.awt.AWTEvent p0) {
    if (p0 instanceof ContainerEvent) {
      processContainerEvent((ContainerEvent) p0);
      return;
    }
    super.processEvent(p0);
  }
  public java.awt.Component getComponentAt(int p0, int p1) {
    if (!contains(p0, p1)) {
      return null;
    }
    for (Component comp : component) {
      if (comp != null && comp.isVisible() && comp.contains(p0 - comp.x, p1 - comp.y)) {
        return comp;
      }
    }
    return this;
  }
  public java.awt.Component getComponentAt(java.awt.Point p0) {
    return getComponentAt(p0.x, p0.y);
  }
  public void deliverEvent(java.awt.Event p0) {
    
  }
  public java.awt.Component locate(int p0, int p1) {
    return getComponentAt(p0, p1);
  }
  public java.awt.Component findComponentAt(java.awt.Point p0) {
    return findComponentAt(p0.x, p0.y);
  }
  public java.awt.Component findComponentAt(int p0, int p1) {
    if (!contains(p0, p1)) {
      return null;
    }
    for (Component comp : component) {
      if (comp != null && comp.isVisible() && comp.contains(p0 - comp.x, p1 - comp.y)) {
        if (comp instanceof Container) {
          Component found = ((Container) comp).findComponentAt(p0 - comp.x, p1 - comp.y);
          if (found != null) {
            return found;
          }
        }
        return comp;
      }
    }
    return this;
  }
  public final boolean isFocusTraversalPolicyProvider() {
    return false;
  }
  public void applyComponentOrientation(java.awt.ComponentOrientation p0) {
    super.applyComponentOrientation(p0);
    for (Component c : component.toArray(new Component[0])) {
      c.applyComponentOrientation(p0);
    }
  }
  public void addPropertyChangeListener(java.lang.String p0, java.beans.PropertyChangeListener p1) {
    super.addPropertyChangeListener(p0, p1);
  }
  public void addPropertyChangeListener(java.beans.PropertyChangeListener p0) {
    super.addPropertyChangeListener(p0);
  }
  public void addContainerListener(java.awt.event.ContainerListener p0) {
    if (p0 != null) {
      containerListeners.add(p0);
    }
  }
  public java.awt.LayoutManager getLayout() {
    return layoutMgr;
  }
  public void setComponentZOrder(java.awt.Component p0, int p1) {
    synchronized (getTreeLock()) {
      if (component.remove(p0)) {
        component.add(Math.min(p1, component.size()), p0);
        invalidate();
      }
    }
  }
  public java.awt.Dimension getPreferredSize() {
    return preferredSize();
  }
  public java.awt.Dimension getMinimumSize() {
    return minimumSize();
  }
  public void paintComponents(java.awt.Graphics p0) {
    for (int i = component.size() - 1; i >= 0; i--) {
      Component comp = component.get(i);
      if (comp != null && comp.isVisible()) {
        Graphics cg = p0.create(comp.x, comp.y, comp.width, comp.height);
        try {
          comp.paint(cg);
        } finally {
          cg.dispose();
        }
      }
    }
  }
  public void printComponents(java.awt.Graphics p0) {
    paintComponents(p0);
  }
  public void removeContainerListener(java.awt.event.ContainerListener p0) {
    containerListeners.remove(p0);
  }
  public java.awt.event.ContainerListener[] getContainerListeners() {
    return containerListeners.toArray(new ContainerListener[0]);
  }
  public java.awt.Point getMousePosition(boolean p0) throws java.awt.HeadlessException {
    throw new HeadlessException();
  }
  public boolean isAncestorOf(java.awt.Component p0) {
    Container p;
    if (p0 == null || ((p = p0.getParent()) == null)) {
      return false;
    }
    while (p != null) {
      if (p == this) {
        return true;
      }
      p = p.getParent();
    }
    return false;
  }
  public void setFocusTraversalPolicy(java.awt.FocusTraversalPolicy p0) {
    focusTraversalPolicy = p0;
  }
  public boolean isFocusTraversalPolicySet() {
    return focusTraversalPolicy != null;
  }
  public void setFocusCycleRoot(boolean p0) {
    focusCycleRoot = p0;
  }
  public final void setFocusTraversalPolicyProvider(boolean p0) {
    
  }
  public void transferFocusDownCycle() {
    
  }

  protected class AccessibleAWTContainer extends AccessibleAWTComponent {
    protected java.awt.event.ContainerListener accessibleContainerHandler;

    protected AccessibleAWTContainer() { }

    public int getAccessibleChildrenCount() { return Container.this.getAccessibleChildrenCount0(); }

    public javax.accessibility.Accessible getAccessibleChild(int i) { return Container.this.getAccessibleChild0(i); }

    public javax.accessibility.Accessible getAccessibleAt(Point p) {
      Component c = Container.this.getComponentAt(p);
      return c instanceof javax.accessibility.Accessible && c != Container.this
          ? (javax.accessibility.Accessible) c : null;
    }
  }

  int getAccessibleChildrenCount0() {
    int count = 0;
    for (int i = 0; i < getComponentCount(); i++) {
      if (getComponent(i) instanceof javax.accessibility.Accessible) {
        count++;
      }
    }
    return count;
  }

  javax.accessibility.Accessible getAccessibleChild0(int i) {
    int count = 0;
    for (int k = 0; k < getComponentCount(); k++) {
      Component c = getComponent(k);
      if (c instanceof javax.accessibility.Accessible) {
        if (count == i) {
          return (javax.accessibility.Accessible) c;
        }
        count++;
      }
    }
    return null;
  }
}
