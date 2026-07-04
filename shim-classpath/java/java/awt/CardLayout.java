/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.util.Vector;


public class CardLayout implements java.awt.LayoutManager2, java.io.Serializable {
  Vector<Card> vector = new Vector<>();
  int currentCard = 0;
  int hgap;
  int vgap;

  static class Card implements java.io.Serializable {
    String name;
    Component comp;

    Card(String cardName, Component cardComponent) {
      name = cardName;
      comp = cardComponent;
    }
  }
  public CardLayout() {
    
  }
  public CardLayout(int p0, int p1) {
    this.hgap = p0;
    this.vgap = p1;
  }
  public java.lang.String toString() {
    return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
  }
  public void next(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      int ncomponents = p0.getComponentCount();
      for (int i = 0; i < ncomponents; i++) {
        Component comp = p0.getComponent(i);
        if (comp.isVisible()) {
          comp.setVisible(false);
          currentCard = (i + 1) % ncomponents;
          comp = p0.getComponent(currentCard);
          comp.setVisible(true);
          p0.validate();
          return;
        }
      }
      showDefaultComponent(p0);
    }
  }
  public void last(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      int ncomponents = p0.getComponentCount();
      for (int i = 0; i < ncomponents; i++) {
        Component comp = p0.getComponent(i);
        if (comp.isVisible()) {
          comp.setVisible(false);
          break;
        }
      }
      if (ncomponents > 0) {
        currentCard = ncomponents - 1;
        p0.getComponent(currentCard).setVisible(true);
        p0.validate();
      }
    }
  }
  public void first(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      int ncomponents = p0.getComponentCount();
      for (int i = 0; i < ncomponents; i++) {
        Component comp = p0.getComponent(i);
        if (comp.isVisible()) {
          comp.setVisible(false);
          break;
        }
      }
      if (ncomponents > 0) {
        currentCard = 0;
        p0.getComponent(0).setVisible(true);
        p0.validate();
      }
    }
  }
  public void previous(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      int ncomponents = p0.getComponentCount();
      for (int i = 0; i < ncomponents; i++) {
        Component comp = p0.getComponent(i);
        if (comp.isVisible()) {
          comp.setVisible(false);
          currentCard = ((i > 0) ? i - 1 : ncomponents - 1);
          comp = p0.getComponent(currentCard);
          comp.setVisible(true);
          p0.validate();
          return;
        }
      }
      showDefaultComponent(p0);
    }
  }
  public void removeLayoutComponent(java.awt.Component p0) {
    synchronized (p0.getTreeLock()) {
      for (int i = 0; i < vector.size(); i++) {
        if (vector.get(i).comp == p0) {
          if (p0.isVisible() && (p0.getParent() != null)) {
            next(p0.getParent());
          }
          vector.remove(i);
          if (currentCard > i) {
            currentCard--;
          }
          break;
        }
      }
    }
  }
  public void addLayoutComponent(java.awt.Component p0, java.lang.Object p1) {
    synchronized (p0.getTreeLock()) {
      if (p1 == null) {
        p1 = "";
      }
      if (p1 instanceof String) {
        addLayoutComponent((String) p1, p0);
      } else {
        throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
      }
    }
  }
  public void addLayoutComponent(java.lang.String p0, java.awt.Component p1) {
    synchronized (p1.getTreeLock()) {
      if (!vector.isEmpty()) {
        p1.setVisible(false);
      }
      for (int i = 0; i < vector.size(); i++) {
        if (vector.get(i).name.equals(p0)) {
          vector.get(i).comp = p1;
          return;
        }
      }
      vector.add(new Card(p0, p1));
    }
  }
  public void layoutContainer(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      Insets insets = p0.getInsets();
      int ncomponents = p0.getComponentCount();
      Component comp = null;
      boolean currentFound = false;
      for (int i = 0; i < ncomponents; i++) {
        comp = p0.getComponent(i);
        comp.setBounds(hgap + insets.left, vgap + insets.top, p0.getWidth() - (hgap * 2 + insets.left + insets.right), p0.getHeight() - (vgap * 2 + insets.top + insets.bottom));
        if (comp.isVisible()) {
          currentFound = true;
        }
      }
      if (!currentFound && ncomponents > 0) {
        p0.getComponent(0).setVisible(true);
      }
    }
  }
  public void invalidateLayout(java.awt.Container p0) {
    
  }
  public java.awt.Dimension preferredLayoutSize(java.awt.Container p0) {
    return layoutSize(p0, false);
  }
  public java.awt.Dimension minimumLayoutSize(java.awt.Container p0) {
    return layoutSize(p0, true);
  }
  public java.awt.Dimension maximumLayoutSize(java.awt.Container p0) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }
  public float getLayoutAlignmentX(java.awt.Container p0) {
    return 0.5f;
  }
  public float getLayoutAlignmentY(java.awt.Container p0) {
    return 0.5f;
  }
  public void show(java.awt.Container p0, java.lang.String p1) {
    synchronized (p0.getTreeLock()) {
      Component next = null;
      int ncomponents = vector.size();
      for (int i = 0; i < ncomponents; i++) {
        Card card = vector.get(i);
        if (card.name.equals(p1)) {
          next = card.comp;
          currentCard = i;
          break;
        }
      }
      if ((next != null) && !next.isVisible()) {
        ncomponents = p0.getComponentCount();
        for (int i = 0; i < ncomponents; i++) {
          Component comp = p0.getComponent(i);
          if (comp.isVisible()) {
            comp.setVisible(false);
            break;
          }
        }
        next.setVisible(true);
        p0.validate();
      }
    }
  }
  public int getHgap() {
    return hgap;
  }
  public void setHgap(int p0) {
    hgap = p0;
  }
  public int getVgap() {
    return vgap;
  }
  public void setVgap(int p0) {
    vgap = p0;
  }

  void showDefaultComponent(Container parent) {
    if (parent.getComponentCount() > 0) {
      currentCard = 0;
      parent.getComponent(0).setVisible(true);
      parent.validate();
    }
  }

  private Dimension layoutSize(Container parent, boolean min) {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int w = 0;
      int h = 0;
      for (int i = 0; i < ncomponents; i++) {
        Component comp = parent.getComponent(i);
        Dimension d = min ? comp.getMinimumSize() : comp.getPreferredSize();
        w = Math.max(d.width, w);
        h = Math.max(d.height, h);
      }
      return new Dimension(insets.left + insets.right + w + hgap * 2, insets.top + insets.bottom + h + vgap * 2);
    }
  }
}
