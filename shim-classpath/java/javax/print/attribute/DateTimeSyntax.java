/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;
import java.util.Date;

public abstract class DateTimeSyntax implements Serializable, Cloneable {
  private final Date value;

  protected DateTimeSyntax(Date value) {
    if (value == null) {
      throw new NullPointerException(" value is null");
    }
    this.value = value;
  }

  public Date getValue() { return new Date(value.getTime()); }

  public boolean equals(Object object) {
    return object != null && object instanceof DateTimeSyntax && value.equals(((DateTimeSyntax) object).value);
  }

  public int hashCode() { return value.hashCode(); }

  public String toString() { return "" + value; }
}
