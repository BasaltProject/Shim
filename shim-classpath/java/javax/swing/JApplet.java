/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.applet.Applet;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;

@SuppressWarnings("removal")
public class JApplet extends Applet implements Accessible, RootPaneContainer, TransferHandler.HasGetTransferHandler {
  protected JRootPane rootPane;
  protected boolean rootPaneCheckingEnabled = false;
  protected AccessibleContext accessibleContext = null;
  private TransferHandler transferHandler;

  public JApplet() throws HeadlessException {
    super();
    enableEvents(java.awt.AWTEvent.KEY_EVENT_MASK);
    setRootPane(createRootPane());
    setRootPaneCheckingEnabled(true);
  }

  public void setTransferHandler(TransferHandler newHandler) {
    TransferHandler oldHandler = transferHandler;
    transferHandler = newHandler;
    getRootPane().firePropertyChange("transferHandler", oldHandler, newHandler);
  }

  public TransferHandler getTransferHandler() { return transferHandler; }

  public void update(Graphics g) { paint(g); }

  public void setJMenuBar(JMenuBar menuBar) { getRootPane().setJMenuBar(menuBar); }

  public JMenuBar getJMenuBar() { return getRootPane().getJMenuBar(); }

  protected boolean isRootPaneCheckingEnabled() { return rootPaneCheckingEnabled; }

  protected void setRootPaneCheckingEnabled(boolean enabled) { rootPaneCheckingEnabled = enabled; }

  protected void addImpl(Component comp, Object constraints, int index) {
    if (isRootPaneCheckingEnabled()) {
      getContentPane().add(comp, constraints, index);
    } else {
      super.addImpl(comp, constraints, index);
    }
  }

  public void remove(Component comp) {
    if (comp == rootPane) {
      super.remove(comp);
    } else {
      getContentPane().remove(comp);
    }
  }

  public void setLayout(LayoutManager manager) {
    if (isRootPaneCheckingEnabled()) {
      getContentPane().setLayout(manager);
    } else {
      super.setLayout(manager);
    }
  }

  public JRootPane getRootPane() { return rootPane; }

  protected JRootPane createRootPane() {
    JRootPane rp = new JRootPane();
    rp.setOpaque(true);
    return rp;
  }

  protected void setRootPane(JRootPane root) {
    if (rootPane != null) {
      remove(rootPane);
    }
    rootPane = root;
    if (rootPane != null) {
      boolean checkingEnabled = isRootPaneCheckingEnabled();
      try {
        setRootPaneCheckingEnabled(false);
        add(rootPane, java.awt.BorderLayout.CENTER);
      } finally {
        setRootPaneCheckingEnabled(checkingEnabled);
      }
    }
  }

  public Container getContentPane() { return getRootPane().getContentPane(); }

  public void setContentPane(Container contentPane) { getRootPane().setContentPane(contentPane); }

  public JLayeredPane getLayeredPane() { return getRootPane().getLayeredPane(); }

  public void setLayeredPane(JLayeredPane layeredPane) { getRootPane().setLayeredPane(layeredPane); }

  public Component getGlassPane() { return getRootPane().getGlassPane(); }

  public void setGlassPane(Component glassPane) { getRootPane().setGlassPane(glassPane); }

  protected String paramString() {
    String rootPaneString = (rootPane != null ? rootPane.toString() : "");
    String rootPaneCheckingEnabledString = (rootPaneCheckingEnabled ? "true" : "false");
    return super.paramString() + ",rootPane=" + rootPaneString + ",rootPaneCheckingEnabled="
        + rootPaneCheckingEnabledString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = super.getAccessibleContext();
    }
    return accessibleContext;
  }
}
