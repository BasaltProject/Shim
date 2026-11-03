/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public final class ToolTipManager extends MouseAdapter implements MouseMotionListener {
  private static ToolTipManager instance;

  boolean enabled = true;
  protected boolean lightWeightPopupEnabled = true;
  protected boolean heavyWeightPopupEnabled = false;
  private int initialDelay = 750;
  private int dismissDelay = 4000;
  private int reshowDelay = 500;
  JComponent insideComponent;
  MouseEvent mouseEvent;
  String toolTipText;
  private final MouseMotionListener moveBeforeEnterListener = new MouseMotionListener() {
    public void mouseMoved(MouseEvent e) { }

    public void mouseDragged(MouseEvent e) { }
  };

  ToolTipManager() { }

  public static ToolTipManager sharedInstance() {
    synchronized (ToolTipManager.class) {
      if (instance == null) {
        instance = new ToolTipManager();
      }
      return instance;
    }
  }

  public void setEnabled(boolean flag) {
    enabled = flag;
    if (!flag) {
      insideComponent = null;
      toolTipText = null;
    }
  }

  public boolean isEnabled() { return enabled; }

  public void setLightWeightPopupEnabled(boolean aFlag) { lightWeightPopupEnabled = aFlag; }

  public boolean isLightWeightPopupEnabled() { return lightWeightPopupEnabled; }

  public void setInitialDelay(int milliseconds) { initialDelay = milliseconds; }

  public int getInitialDelay() { return initialDelay; }

  public void setDismissDelay(int milliseconds) { dismissDelay = milliseconds; }

  public int getDismissDelay() { return dismissDelay; }

  public void setReshowDelay(int milliseconds) { reshowDelay = milliseconds; }

  public int getReshowDelay() { return reshowDelay; }

  public void registerComponent(JComponent component) {
    component.removeMouseListener(this);
    component.addMouseListener(this);
    component.removeMouseMotionListener(moveBeforeEnterListener);
    component.addMouseMotionListener(moveBeforeEnterListener);
  }

  public void unregisterComponent(JComponent component) {
    component.removeMouseListener(this);
    component.removeMouseMotionListener(moveBeforeEnterListener);
  }

  public void mouseEntered(MouseEvent event) {
    JComponent component = (JComponent) event.getSource();
    component.removeMouseMotionListener(moveBeforeEnterListener);
    component.addMouseMotionListener(this);
    if (enabled) {
      insideComponent = component;
      mouseEvent = event;
      toolTipText = component.getToolTipText(event);
    }
  }

  public void mouseExited(MouseEvent event) {
    JComponent component = (JComponent) event.getSource();
    component.removeMouseMotionListener(this);
    component.addMouseMotionListener(moveBeforeEnterListener);
    if (insideComponent == component) {
      insideComponent = null;
      toolTipText = null;
    }
  }

  public void mousePressed(MouseEvent event) {
    toolTipText = null;
  }

  public void mouseDragged(MouseEvent event) { }

  public void mouseMoved(MouseEvent event) {
    if (enabled && insideComponent != null) {
      mouseEvent = event;
      toolTipText = insideComponent.getToolTipText(event);
    }
  }
}
