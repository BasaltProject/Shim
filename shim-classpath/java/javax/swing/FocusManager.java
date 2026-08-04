/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;

public abstract class FocusManager extends DefaultKeyboardFocusManager {
  public static final String FOCUS_MANAGER_CLASS_PROPERTY = "FocusManagerClassName";

  protected FocusManager() { }

  public static FocusManager getCurrentManager() {
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    if (manager instanceof FocusManager) {
      return (FocusManager) manager;
    }
    return new DelegatingDefaultFocusManager(manager);
  }

  public static void setCurrentManager(FocusManager aFocusManager) throws SecurityException {
    KeyboardFocusManager toSet = (aFocusManager instanceof DelegatingDefaultFocusManager)
        ? ((DelegatingDefaultFocusManager) aFocusManager).getDelegate() : aFocusManager;
    KeyboardFocusManager.setCurrentKeyboardFocusManager(toSet);
  }

  public static void disableSwingFocusManager() { }

  public static boolean isFocusManagerEnabled() { return false; }

  private static final class DelegatingDefaultFocusManager extends DefaultFocusManager {
    private final KeyboardFocusManager delegate;

    DelegatingDefaultFocusManager(KeyboardFocusManager delegate) { this.delegate = delegate; }

    KeyboardFocusManager getDelegate() { return delegate; }

    public Component getFocusOwner() { return delegate.getFocusOwner(); }

    public Window getFocusedWindow() { return delegate.getFocusedWindow(); }

    public Window getActiveWindow() { return delegate.getActiveWindow(); }

    public boolean dispatchEvent(AWTEvent e) { return delegate.dispatchEvent(e); }

    public boolean dispatchKeyEvent(KeyEvent e) { return delegate.dispatchKeyEvent(e); }

    public void focusNextComponent(Component c) { delegate.focusNextComponent(c); }

    public void focusPreviousComponent(Component c) { delegate.focusPreviousComponent(c); }

    public void upFocusCycle(Component c) { delegate.upFocusCycle(c); }

    public void downFocusCycle(Container c) { delegate.downFocusCycle(c); }
  }
}
