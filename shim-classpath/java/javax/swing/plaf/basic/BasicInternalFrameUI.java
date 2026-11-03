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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.UIResource;

public class BasicInternalFrameUI extends InternalFrameUI {
  protected JInternalFrame frame;
  protected MouseInputAdapter borderListener;
  protected PropertyChangeListener propertyChangeListener;
  protected LayoutManager internalFrameLayout;
  protected ComponentListener componentListener;
  protected MouseInputListener glassPaneDispatcher;
  protected JComponent northPane;
  protected JComponent southPane;
  protected JComponent westPane;
  protected JComponent eastPane;
  protected BasicInternalFrameTitlePane titlePane;
  @Deprecated
  protected KeyStroke openMenuKey;
  private boolean keyBindingRegistered;
  private boolean keyBindingActive;

  public BasicInternalFrameUI(JInternalFrame b) {
    frame = b;
  }

  public static ComponentUI createUI(JComponent b) {
    return new BasicInternalFrameUI((JInternalFrame) b);
  }

  public void installUI(JComponent c) {
    frame = (JInternalFrame) c;
    installDefaults();
    installListeners();
    installComponents();
    installKeyboardActions();
    LookAndFeel.installProperty(frame, "opaque", Boolean.TRUE);
  }

  public void uninstallUI(JComponent c) {
    if (c != frame) {
      throw new IllegalComponentStateException("BasicInternalFrameUI can not uninstall a different frame");
    }
    uninstallKeyboardActions();
    uninstallComponents();
    uninstallListeners();
    uninstallDefaults();
    frame = null;
  }

  private static final class IllegalComponentStateException extends java.awt.IllegalComponentStateException {
    IllegalComponentStateException(String s) {
      super(s);
    }
  }

  protected void installDefaults() {
    Icon frameIcon = frame.getFrameIcon();
    if (frameIcon == null || frameIcon instanceof UIResource) {
      frame.setFrameIcon(UIManager.getIcon("InternalFrame.icon"));
    }
    Border border = frame.getBorder();
    if (border == null || border instanceof UIResource) {
      frame.setBorder(UIManager.getBorder("InternalFrame.border"));
    }
    if (frame.getBackground() == null || frame.getBackground() instanceof UIResource) {
      frame.setBackground(UIManager.getColor("control"));
    }
    frame.setLayout(internalFrameLayout = createLayoutManager());
    frame.setMinimumSize(null);
  }

  protected void installKeyboardActions() {
  }

  protected void installComponents() {
    setNorthPane(createNorthPane(frame));
    setSouthPane(createSouthPane(frame));
    setEastPane(createEastPane(frame));
    setWestPane(createWestPane(frame));
  }

  protected void installListeners() {
    borderListener = createBorderListener(frame);
    propertyChangeListener = createPropertyChangeListener();
    frame.addPropertyChangeListener(propertyChangeListener);
    installMouseHandlers(frame);
    componentListener = createComponentListener();
    frame.addComponentListener(componentListener);
  }

  protected void uninstallDefaults() {
    Icon frameIcon = frame.getFrameIcon();
    if (frameIcon instanceof UIResource) {
      frame.setFrameIcon(null);
    }
    internalFrameLayout = null;
    frame.setLayout(null);
    LookAndFeel.uninstallBorder(frame);
  }

  protected void uninstallComponents() {
    setNorthPane(null);
    setSouthPane(null);
    setEastPane(null);
    setWestPane(null);
    if (titlePane != null) {
      titlePane.uninstallDefaults();
    }
    titlePane = null;
  }

  protected void uninstallListeners() {
    if (componentListener != null) {
      frame.removeComponentListener(componentListener);
      componentListener = null;
    }
    deinstallMouseHandlers(frame);
    frame.removePropertyChangeListener(propertyChangeListener);
    propertyChangeListener = null;
    borderListener = null;
  }

  protected void uninstallKeyboardActions() {
  }

  protected LayoutManager createLayoutManager() {
    return new InternalFrameLayout();
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new InternalFramePropertyChangeListener();
  }

  protected void createInternalFrameListener() {
  }

  protected ComponentListener createComponentListener() {
    return new ComponentAdapter() {
    };
  }

  protected MouseInputListener createGlassPaneDispatcher() {
    return null;
  }

  protected MouseInputAdapter createBorderListener(JInternalFrame w) {
    return new MouseInputAdapter() {
    };
  }

  protected void installMouseHandlers(JComponent c) {
    c.addMouseListener(borderListener);
    c.addMouseMotionListener(borderListener);
  }

  protected void deinstallMouseHandlers(JComponent c) {
    c.removeMouseListener(borderListener);
    c.removeMouseMotionListener(borderListener);
  }

  protected JComponent createNorthPane(JInternalFrame w) {
    titlePane = new BasicInternalFrameTitlePane(w);
    return titlePane;
  }

  protected JComponent createSouthPane(JInternalFrame w) {
    return null;
  }

  protected JComponent createWestPane(JInternalFrame w) {
    return null;
  }

  protected JComponent createEastPane(JInternalFrame w) {
    return null;
  }

  protected DesktopManager createDesktopManager() {
    return new DefaultDesktopManager();
  }

  protected void replacePane(JComponent currentPane, JComponent newPane) {
    if (currentPane != null) {
      deinstallMouseHandlers(currentPane);
      frame.remove(currentPane);
    }
    if (newPane != null) {
      frame.add(newPane);
      installMouseHandlers(newPane);
    }
  }

  public JComponent getNorthPane() {
    return northPane;
  }

  public void setNorthPane(JComponent c) {
    if (northPane != null && northPane instanceof BasicInternalFrameTitlePane) {
      ((BasicInternalFrameTitlePane) northPane).uninstallListeners();
    }
    replacePane(northPane, c);
    northPane = c;
    if (c instanceof BasicInternalFrameTitlePane) {
      titlePane = (BasicInternalFrameTitlePane) c;
    }
  }

  public JComponent getSouthPane() {
    return southPane;
  }

  public void setSouthPane(JComponent c) {
    if (southPane != null) {
      frame.remove(southPane);
      deinstallMouseHandlers(southPane);
    }
    if (c != null) {
      frame.add(c);
      installMouseHandlers(c);
    }
    southPane = c;
  }

  public JComponent getWestPane() {
    return westPane;
  }

  public void setWestPane(JComponent c) {
    if (westPane != null) {
      frame.remove(westPane);
      deinstallMouseHandlers(westPane);
    }
    if (c != null) {
      frame.add(c);
      installMouseHandlers(c);
    }
    westPane = c;
  }

  public JComponent getEastPane() {
    return eastPane;
  }

  public void setEastPane(JComponent c) {
    if (eastPane != null) {
      frame.remove(eastPane);
      deinstallMouseHandlers(eastPane);
    }
    if (c != null) {
      frame.add(c);
      installMouseHandlers(c);
    }
    eastPane = c;
  }

  public Dimension getPreferredSize(JComponent x) {
    if (frame == x) {
      return frame.getLayout().preferredLayoutSize(x);
    }
    return new Dimension(100, 100);
  }

  public Dimension getMinimumSize(JComponent x) {
    if (frame == x) {
      return frame.getLayout().minimumLayoutSize(x);
    }
    return new Dimension(0, 0);
  }

  public Dimension getMaximumSize(JComponent x) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  protected DesktopManager getDesktopManager() {
    if (frame.getDesktopPane() != null && frame.getDesktopPane().getDesktopManager() != null) {
      return frame.getDesktopPane().getDesktopManager();
    }
    return createDesktopManager();
  }

  protected void closeFrame(JInternalFrame f) {
    getDesktopManager().closeFrame(f);
  }

  protected void maximizeFrame(JInternalFrame f) {
    getDesktopManager().maximizeFrame(f);
  }

  protected void minimizeFrame(JInternalFrame f) {
    if (!f.isIcon()) {
      getDesktopManager().minimizeFrame(f);
    } else {
      getDesktopManager().deiconifyFrame(f);
    }
  }

  protected void iconifyFrame(JInternalFrame f) {
    getDesktopManager().iconifyFrame(f);
  }

  protected void deiconifyFrame(JInternalFrame f) {
    if (!f.isMaximum()) {
      getDesktopManager().deiconifyFrame(f);
    }
  }

  protected void activateFrame(JInternalFrame f) {
    getDesktopManager().activateFrame(f);
  }

  protected void deactivateFrame(JInternalFrame f) {
    getDesktopManager().deactivateFrame(f);
  }

  protected final boolean isKeyBindingRegistered() {
    return keyBindingRegistered;
  }

  protected final void setKeyBindingRegistered(boolean b) {
    keyBindingRegistered = b;
  }

  public final boolean isKeyBindingActive() {
    return keyBindingActive;
  }

  protected final void setKeyBindingActive(boolean b) {
    keyBindingActive = b;
  }

  protected void setupMenuOpenKey() {
  }

  protected void setupMenuCloseKey() {
  }

  public class InternalFrameLayout implements LayoutManager {
    public InternalFrameLayout() {
    }

    public void addLayoutComponent(String name, Component c) {
    }

    public void removeLayoutComponent(Component c) {
    }

    public Dimension preferredLayoutSize(Container c) {
      Dimension result = frame.getRootPane().getPreferredSize();
      Insets i = frame.getInsets();
      result.height += i.top + i.bottom;
      if (getNorthPane() != null) {
        Dimension d = getNorthPane().getPreferredSize();
        result.width = Math.max(d.width, result.width);
        result.height += d.height;
      }
      if (getSouthPane() != null) {
        Dimension d = getSouthPane().getPreferredSize();
        result.width = Math.max(d.width, result.width);
        result.height += d.height;
      }
      if (getEastPane() != null) {
        Dimension d = getEastPane().getPreferredSize();
        result.width += d.width;
        result.height = Math.max(d.height, result.height);
      }
      if (getWestPane() != null) {
        Dimension d = getWestPane().getPreferredSize();
        result.width += d.width;
        result.height = Math.max(d.height, result.height);
      }
      return result;
    }

    public Dimension minimumLayoutSize(Container c) {
      Dimension result = new Dimension();
      if (getNorthPane() != null && getNorthPane() instanceof BasicInternalFrameTitlePane) {
        result = new Dimension(getNorthPane().getMinimumSize());
      }
      Insets i = frame.getInsets();
      result.width += i.left + i.right;
      result.height += i.top + i.bottom;
      return result;
    }

    public void layoutContainer(Container c) {
      Insets i = frame.getInsets();
      int cx = i.left;
      int cy = i.top;
      int cw = frame.getWidth() - i.left - i.right;
      int ch = frame.getHeight() - i.top - i.bottom;
      if (getNorthPane() != null) {
        Dimension size = getNorthPane().getPreferredSize();
        getNorthPane().setBounds(cx, cy, cw, size.height);
        cy += size.height;
        ch -= size.height;
      }
      if (getSouthPane() != null) {
        Dimension size = getSouthPane().getPreferredSize();
        getSouthPane().setBounds(cx, frame.getHeight() - i.bottom - size.height, cw, size.height);
        ch -= size.height;
      }
      if (getWestPane() != null) {
        Dimension size = getWestPane().getPreferredSize();
        getWestPane().setBounds(cx, cy, size.width, ch);
        cw -= size.width;
        cx += size.width;
      }
      if (getEastPane() != null) {
        Dimension size = getEastPane().getPreferredSize();
        getEastPane().setBounds(cw - size.width + cx, cy, size.width, ch);
        cw -= size.width;
      }
      if (frame.getRootPane() != null) {
        frame.getRootPane().setBounds(cx, cy, cw, ch);
      }
    }
  }

  public class InternalFramePropertyChangeListener implements PropertyChangeListener {
    public InternalFramePropertyChangeListener() {
    }

    public void propertyChange(PropertyChangeEvent evt) {
      String prop = evt.getPropertyName();
      if ("closed".equals(prop) || "selected".equals(prop) || "icon".equals(prop) || "maximum".equals(prop)) {
        frame.repaint();
      }
    }
  }
}
