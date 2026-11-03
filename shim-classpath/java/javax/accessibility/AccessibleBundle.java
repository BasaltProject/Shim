/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

import java.util.Locale;

public abstract class AccessibleBundle {
  protected String key;

  public AccessibleBundle() { }

  protected String toDisplayString(String resourceBundleName, Locale locale) {
    try {
      java.util.ResourceBundle b = java.util.ResourceBundle.getBundle(resourceBundleName, locale);
      String r = b.getString(key);
      return r == null ? key : r;
    } catch (Exception e) {
      return key;
    }
  }

  public String toDisplayString(Locale locale) { return toDisplayString("com.sun.accessibility.internal.resources.accessibility", locale); }

  public String toDisplayString() { return toDisplayString(Locale.getDefault()); }

  public String toString() { return toDisplayString(); }
}
