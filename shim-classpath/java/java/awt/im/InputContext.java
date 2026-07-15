/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.im;

import java.awt.AWTEvent;
import java.awt.Component;
import java.lang.Character.Subset;
import java.util.Locale;

public class InputContext {
  private static InputContext instance;

  protected InputContext() { }

  public static InputContext getInstance() {
    synchronized (InputContext.class) {
      if (instance == null) {
        instance = new InputContext();
      }
      return instance;
    }
  }

  public boolean selectInputMethod(Locale locale) { return false; }

  public Locale getLocale() { return null; }

  public void setCharacterSubsets(Subset[] subsets) { }

  public void setCompositionEnabled(boolean enable) { throw new UnsupportedOperationException(); }

  public boolean isCompositionEnabled() { throw new UnsupportedOperationException(); }

  public void reconvert() { throw new UnsupportedOperationException(); }

  public void dispatchEvent(AWTEvent event) { }

  public void removeNotify(Component client) { }

  public void endComposition() { }

  public void dispose() { }

  public Object getInputMethodControlObject() { return null; }
}
