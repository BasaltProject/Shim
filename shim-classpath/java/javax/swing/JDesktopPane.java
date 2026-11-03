/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.DesktopPaneUI;

public class JDesktopPane extends JLayeredPane implements javax.accessibility.Accessible {
  public static final int LIVE_DRAG_MODE = 0;
  public static final int OUTLINE_DRAG_MODE = 1;

  private transient DesktopManager desktopManager;
  private transient JInternalFrame selectedFrame = null;
  private int dragMode = LIVE_DRAG_MODE;
  private boolean dragModeSet = false;

  public JDesktopPane() {
    setUIProperty("opaque", Boolean.TRUE);
    setFocusCycleRoot(true);
    updateUI();
  }

  public DesktopPaneUI getUI() { return (DesktopPaneUI) ui; }

  public void setUI(DesktopPaneUI ui) { super.setUI(ui); }

  public void setDragMode(int dragMode) {
    int oldDragMode = this.dragMode;
    this.dragMode = dragMode;
    firePropertyChange("dragMode", oldDragMode, this.dragMode);
    dragModeSet = true;
  }

  public int getDragMode() { return dragMode; }

  public DesktopManager getDesktopManager() { return desktopManager; }

  public void setDesktopManager(DesktopManager d) {
    DesktopManager oldValue = desktopManager;
    desktopManager = d;
    firePropertyChange("desktopManager", oldValue, desktopManager);
  }

  public void updateUI() {
    setUI((DesktopPaneUI) UIManager.getUI(this));
    if (desktopManager == null) {
      desktopManager = new DefaultDesktopManager();
    }
  }

  public String getUIClassID() { return "DesktopPaneUI"; }

  public JInternalFrame[] getAllFrames() {
    int i;
    int count;
    JInternalFrame[] results;
    java.util.Vector<JInternalFrame> vResults = new java.util.Vector<JInternalFrame>(10);
    Object next;
    Object tmp;
    count = getComponentCount();
    for (i = 0; i < count; i++) {
      next = getComponent(i);
      if (next instanceof JInternalFrame) {
        vResults.addElement((JInternalFrame) next);
      } else if (next instanceof JInternalFrame.JDesktopIcon) {
        tmp = ((JInternalFrame.JDesktopIcon) next).getInternalFrame();
        if (tmp != null) {
          vResults.addElement((JInternalFrame) tmp);
        }
      }
    }
    results = new JInternalFrame[vResults.size()];
    vResults.copyInto(results);
    return results;
  }

  public JInternalFrame getSelectedFrame() { return selectedFrame; }

  public void setSelectedFrame(JInternalFrame f) { selectedFrame = f; }

  public JInternalFrame[] getAllFramesInLayer(int layer) {
    List<JInternalFrame> results = new ArrayList<JInternalFrame>();
    for (JInternalFrame f : getAllFrames()) {
      if (f.getLayer() == layer) {
        results.add(f);
      }
    }
    return results.toArray(new JInternalFrame[0]);
  }

  private List<JInternalFrame> getFrames() {
    List<JInternalFrame> frames = new ArrayList<JInternalFrame>();
    for (Component c : getComponents()) {
      if (c instanceof JInternalFrame) {
        frames.add((JInternalFrame) c);
      } else if (c instanceof JInternalFrame.JDesktopIcon) {
        JInternalFrame f = ((JInternalFrame.JDesktopIcon) c).getInternalFrame();
        if (f != null) {
          frames.add(f);
        }
      }
    }
    return frames;
  }

  public void remove(Component comp) {
    super.remove(comp);
  }

  JInternalFrame getNextFrame(JInternalFrame f) { return getNextFrame(f, true); }

  private JInternalFrame getNextFrame(JInternalFrame f, boolean forward) {
    List<JInternalFrame> frames = getFrames();
    if (frames.isEmpty()) {
      return null;
    }
    int index = f == null ? -1 : frames.indexOf(f);
    int n = frames.size();
    for (int k = 1; k <= n; k++) {
      int i = forward ? (index + k) % n : ((index - k) % n + n) % n;
      JInternalFrame c = frames.get(i);
      if (c != f && !c.isIcon()) {
        return c;
      }
    }
    return null;
  }

  public JInternalFrame selectFrame(boolean forward) {
    JInternalFrame selected = getSelectedFrame();
    JInternalFrame next = getNextFrame(selected, forward);
    if (next != null) {
      try {
        next.setSelected(true);
      } catch (PropertyVetoException pve) {
        return null;
      }
      next.moveToFront();
      setSelectedFrame(next);
    }
    return next;
  }

  protected void addImpl(Component comp, Object constraints, int index) {
    super.addImpl(comp, constraints, index);
  }

  public void remove(int index) { super.remove(index); }

  public void removeAll() { super.removeAll(); }

  public void setComponentZOrder(Component comp, int index) { super.setComponentZOrder(comp, index); }

  protected String paramString() {
    String desktopManagerString = (desktopManager != null ? desktopManager.toString() : "");
    return super.paramString() + ",desktopManager=" + desktopManagerString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJDesktopPane();
    }
    return accessibleContext;
  }

  protected class AccessibleJDesktopPane extends AccessibleJComponent {
    protected AccessibleJDesktopPane() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.DESKTOP_PANE; }
  }
}
