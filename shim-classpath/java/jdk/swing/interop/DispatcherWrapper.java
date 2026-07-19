/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.swing.interop;

public abstract class DispatcherWrapper {

  public DispatcherWrapper() {
  }

  public abstract java.awt.SecondaryLoop createSecondaryLoop();
  public abstract boolean isDispatchThread();
  public abstract void scheduleDispatch(java.lang.Runnable runnable);
  public static void setFwDispatcher(java.awt.EventQueue eventQueue, jdk.swing.interop.DispatcherWrapper dispatcherWrapper) {
    throw new UnsupportedOperationException();
  }
}
