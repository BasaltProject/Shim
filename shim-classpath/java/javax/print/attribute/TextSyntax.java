/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;
import java.util.Locale;

public abstract class TextSyntax implements Serializable, Cloneable {
  private final String value;
  private final Locale locale;

  protected TextSyntax(String value, Locale locale) {
    this.value = verify(value);
    this.locale = verify(locale);
  }

  private static String verify(String x) {
    if (x == null) {
      throw new NullPointerException(" value is null");
    }
    return x;
  }

  private static Locale verify(Locale x) {
    if (x == null) {
      return Locale.getDefault();
    }
    return x;
  }

  public String getValue() { return value; }

  public Locale getLocale() { return locale; }

  public int hashCode() { return value.hashCode() ^ locale.hashCode(); }

  public boolean equals(Object object) {
    return object != null && object instanceof TextSyntax && value.equals(((TextSyntax) object).value)
        && locale.equals(((TextSyntax) object).locale);
  }

  public String toString() { return value; }
}
