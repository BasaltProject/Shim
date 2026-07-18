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
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

public class BasicToolBarUI extends javax.swing.plaf.ToolBarUI implements javax.swing.SwingConstants {
  private boolean bp_Floating;
  private boolean bp_RolloverBorders;
  private java.awt.Color bp_DockingColor;
  private java.awt.Color bp_FloatingColor;
  protected javax.swing.JToolBar toolBar;
  protected int focusedCompIndex;
  protected java.awt.Color dockingColor;
  protected java.awt.Color floatingColor;
  protected java.awt.Color dockingBorderColor;
  protected java.awt.Color floatingBorderColor;
  protected javax.swing.event.MouseInputListener dockingListener;
  protected java.beans.PropertyChangeListener propertyListener;
  protected java.awt.event.ContainerListener toolBarContListener;
  protected java.awt.event.FocusListener toolBarFocusListener;
  protected java.lang.String constraintBeforeFloating;
  protected javax.swing.KeyStroke upKey;
  protected javax.swing.KeyStroke downKey;
  protected javax.swing.KeyStroke leftKey;
  protected javax.swing.KeyStroke rightKey;
  public BasicToolBarUI() {
  }
  public boolean isFloating() { return bp_Floating; }
  protected void installKeyboardActions() {
  }
  protected void uninstallKeyboardActions() {
  }
  protected void installDefaults() {
    LookAndFeel.installBorder(toolBar, "ToolBar.border");
    LookAndFeel.installColorsAndFont(toolBar, "ToolBar.background", "ToolBar.foreground", "ToolBar.font");
    if (rolloverBorder == null) {
      rolloverBorder = createRolloverBorder();
    }
    if (nonRolloverBorder == null) {
      nonRolloverBorder = createNonRolloverBorder();
    }
    if (normalBorder == null) {
      normalBorder = createNormalBorder();
    }
    setRolloverBorders(toolBar.isRollover());
  }
  protected void installListeners() {
    toolBarContListener = createToolBarContListener();
    if (toolBarContListener != null) {
      toolBar.addContainerListener(toolBarContListener);
    }
    propertyListener = createPropertyListener();
    if (propertyListener != null) {
      toolBar.addPropertyChangeListener(propertyListener);
    }
  }
  protected void uninstallListeners() {
    if (toolBarContListener != null) {
      toolBar.removeContainerListener(toolBarContListener);
    }
    if (propertyListener != null) {
      toolBar.removePropertyChangeListener(propertyListener);
    }
    toolBarContListener = null;
    propertyListener = null;
  }
  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(toolBar);
    rolloverBorder = null;
    nonRolloverBorder = null;
    normalBorder = null;
  }
  protected void installComponents() {
  }
  protected void uninstallComponents() {
  }
  public void setFloating(boolean b, java.awt.Point p) {
  }
  protected Border createRolloverBorder() {
    Object border = UIManager.get("ToolBar.rolloverBorder");
    if (border != null) {
      return (Border) border;
    }
    return new javax.swing.border.EmptyBorder(2, 2, 2, 2);
  }
  protected Border createNonRolloverBorder() {
    Object border = UIManager.get("ToolBar.nonrolloverBorder");
    if (border != null) {
      return (Border) border;
    }
    return createRolloverBorder();
  }
  public boolean isRolloverBorders() {
    return rolloverFlag;
  }
  public void setRolloverBorders(boolean rollover) {
    super_setRolloverBorders(rollover);
  }
  protected void installNormalBorders(JComponent c) {
    Component[] components = c.getComponents();
    for (Component component : components) {
      setBorderToNormal(component);
    }
  }
  protected javax.swing.event.MouseInputListener createDockingListener() {
    return new DockingListener(toolBar);
  }
  protected PropertyChangeListener createPropertyListener() {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if ("floatable".equals(propertyName) || "orientation".equals(propertyName)) {
          toolBar.revalidate();
          toolBar.repaint();
        } else if ("borderPainted".equals(propertyName)) {
          toolBar.repaint();
        } else if ("rollover".equals(propertyName)) {
          setRolloverBorders(((Boolean) e.getNewValue()).booleanValue());
        }
      }
    };
  }
  protected ContainerListener createToolBarContListener() {
    return new ContainerListener() {
      public void componentAdded(ContainerEvent e) {
        Component c = e.getChild();
        if (isRolloverBorders()) {
          setBorderToRollover(c);
        } else {
          setBorderToNonRollover(c);
        }
      }

      public void componentRemoved(ContainerEvent e) {
        Component c = e.getChild();
        setBorderToNormal(c);
      }
    };
  }
  protected java.awt.event.FocusListener createToolBarFocusListener() {
    return null;
  }
  protected java.awt.event.WindowListener createFrameListener() {
    return null;
  }
  protected javax.swing.RootPaneContainer createFloatingWindow(javax.swing.JToolBar toolbar) {
    java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(toolbar);
    javax.swing.JDialog dialog;
    if (window instanceof java.awt.Frame) {
      dialog = new javax.swing.JDialog((java.awt.Frame) window, toolbar.getName(), false);
    } else if (window instanceof java.awt.Dialog) {
      dialog = new javax.swing.JDialog((java.awt.Dialog) window, toolbar.getName(), false);
    } else {
      dialog = new javax.swing.JDialog((java.awt.Frame) null, toolbar.getName(), false);
    }
    dialog.setTitle(toolbar.getName());
    dialog.setResizable(false);
    return dialog;
  }
  protected void installRolloverBorders(JComponent c) {
    Component[] components = c.getComponents();
    for (Component component : components) {
      if (component instanceof JComponent) {
        ((JComponent) component).updateUI();
        setBorderToRollover(component);
      }
    }
  }
  protected void installNonRolloverBorders(JComponent c) {
    Component[] components = c.getComponents();
    for (Component component : components) {
      if (component instanceof JComponent) {
        ((JComponent) component).updateUI();
        setBorderToNonRollover(component);
      }
    }
  }
  protected void setBorderToRollover(Component c) {
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      Border border = b.getBorder();
      if (border == null || border instanceof UIResource) {
        b.setBorder(getRolloverBorder(b));
      }
      b.setRolloverEnabled(true);
    }
  }
  protected void setBorderToNonRollover(Component c) {
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      Border border = b.getBorder();
      if (border == null || border instanceof UIResource) {
        b.setBorder(getNonRolloverBorder(b));
      }
      b.setRolloverEnabled(false);
    }
  }
  protected void setBorderToNormal(Component c) {
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      Border border = b.getBorder();
      if (border == null || border instanceof UIResource) {
        b.setBorder(normalBorder);
      }
    }
  }
  protected Border getRolloverBorder(AbstractButton b) {
    return rolloverBorder;
  }
  protected Border getNonRolloverBorder(AbstractButton b) {
    if (b.isRolloverEnabled() && !(b.getBorder() instanceof UIResource)) {
      return rolloverBorder;
    }
    return nonRolloverBorder;
  }
  public boolean canDock(Component c, java.awt.Point p) {
    return false;
  }
  public java.awt.Color getDockingColor() { return bp_DockingColor; }
  public java.awt.Color getFloatingColor() { return bp_FloatingColor; }
  public void setFloatingLocation(int x, int y) {
  }
  protected void navigateFocusedComp(int direction) {
  }
  protected javax.swing.JFrame createFloatingFrame(javax.swing.JToolBar toolbar) {
    java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(toolbar);
    javax.swing.JFrame frame = new javax.swing.JFrame(toolbar.getName(),
        window != null ? window.getGraphicsConfiguration() : null);
    frame.setTitle(toolbar.getName());
    frame.setResizable(false);
    return frame;
  }
  public void setDockingColor(java.awt.Color p0) { bp_DockingColor = p0; }
  public void setFloatingColor(java.awt.Color p0) { bp_FloatingColor = p0; }
  protected void dragTo(java.awt.Point position, java.awt.Point origin) {
  }
  protected void floatAt(java.awt.Point position, java.awt.Point origin) {
  }
  protected void paintDragWindow(java.awt.Graphics g) {
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new BasicToolBarUI();
  }
  public void setOrientation(int orientation) {
    toolBar.setOrientation(orientation);
  }
  public void installUI(JComponent c) {
    toolBar = (JToolBar) c;
    installDefaults();
    installComponents();
    installListeners();
    installKeyboardActions();
    focusedCompIndex = -1;
  }
  public void uninstallUI(JComponent c) {
    uninstallDefaults();
    uninstallComponents();
    uninstallListeners();
    uninstallKeyboardActions();
    toolBar = null;
  }

  public class DockingListener implements javax.swing.event.MouseInputListener {
    protected javax.swing.JToolBar toolBar;
    protected boolean isDragging;
    protected java.awt.Point origin;
    public DockingListener(javax.swing.JToolBar p1) {
    }
    public void mousePressed(java.awt.event.MouseEvent e) {
    }
    public void mouseReleased(java.awt.event.MouseEvent e) {
    }
    public void mouseClicked(java.awt.event.MouseEvent e) {
    }
    public void mouseExited(java.awt.event.MouseEvent e) {
    }
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    public void mouseMoved(java.awt.event.MouseEvent e) {
    }
    public void mouseDragged(java.awt.event.MouseEvent e) {
    }
  }
  private Border rolloverBorder;
  private Border nonRolloverBorder;
  private Border normalBorder;

  protected Border createNormalBorder() {
    return null;
  }

  private void super_setRolloverBorders(boolean rollover) {
    setRolloverFlag(rollover);
    if (rollover) {
      installRolloverBorders(toolBar);
    } else {
      installNonRolloverBorders(toolBar);
    }
  }
  private boolean rolloverFlag;

  private void setRolloverFlag(boolean v) {
    rolloverFlag = v;
  }
}
