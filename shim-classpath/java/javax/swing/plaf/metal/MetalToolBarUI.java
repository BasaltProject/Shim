/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.metal;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicToolBarUI;

public class MetalToolBarUI extends BasicToolBarUI {
  protected ContainerListener contListener;
  protected PropertyChangeListener rolloverListener;

  public MetalToolBarUI() {
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new MetalToolBarUI();
  }

  public void installUI(JComponent c) {
    super.installUI(c);
  }

  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }

  protected void installListeners() {
    super.installListeners();
    contListener = createContainerListener();
    if (contListener != null) {
      toolBar.addContainerListener(contListener);
    }
    rolloverListener = createRolloverListener();
    if (rolloverListener != null) {
      toolBar.addPropertyChangeListener(rolloverListener);
    }
  }

  protected void uninstallListeners() {
    super.uninstallListeners();
    if (contListener != null) {
      toolBar.removeContainerListener(contListener);
    }
    rolloverListener = null;
    contListener = null;
  }

  protected Border createRolloverBorder() {
    return super.createRolloverBorder();
  }

  protected Border createNonRolloverBorder() {
    return super.createNonRolloverBorder();
  }

  protected javax.swing.event.MouseInputListener createDockingListener() {
    return super.createDockingListener();
  }

  protected void setBorderToNonRollover(Component c) {
    super.setBorderToNonRollover(c);
  }

  protected ContainerListener createContainerListener() {
    return null;
  }

  protected PropertyChangeListener createRolloverListener() {
    return null;
  }

  protected void setDragOffset(java.awt.Point p) {
  }

  public void update(Graphics g, JComponent c) {
    if (c.isOpaque()) {
      g.setColor(c.getBackground());
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }
    paint(g, c);
  }
}
