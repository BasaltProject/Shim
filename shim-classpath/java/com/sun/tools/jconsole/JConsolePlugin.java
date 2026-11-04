/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.tools.jconsole;

public abstract class JConsolePlugin {

  protected JConsolePlugin() {
  }

  public final void addContextPropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener) {
    throw new UnsupportedOperationException();
  }
  public void dispose() {
    throw new UnsupportedOperationException();
  }
  public final com.sun.tools.jconsole.JConsoleContext getContext() {
    throw new UnsupportedOperationException();
  }
  public abstract java.util.Map<java.lang.String, javax.swing.JPanel> getTabs();
  public abstract javax.swing.SwingWorker<?, ?> newSwingWorker();
  public final void removeContextPropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener) {
    throw new UnsupportedOperationException();
  }
  public final synchronized void setContext(com.sun.tools.jconsole.JConsoleContext jConsoleContext) {
    throw new UnsupportedOperationException();
  }
}
