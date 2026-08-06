/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;

public abstract class EnumSyntax implements Serializable, Cloneable {
  private static final long serialVersionUID = -2739521845085831642L;

  private int value;

  protected EnumSyntax(int value) { this.value = value; }

  public int getValue() { return value; }

  public Object clone() { return this; }

  public int hashCode() { return value; }

  public String toString() {
    String[] table = getStringTable();
    int index = value - getOffset();
    if (table != null && index >= 0 && index < table.length) {
      return table[index];
    }
    return Integer.toString(value);
  }

  protected Object readResolve() throws ObjectStreamException {
    EnumSyntax[] table = getEnumValueTable();
    if (table == null) {
      throw new InvalidObjectException("Null enumeration value table for class " + getClass());
    }
    int offset = getOffset();
    int index = value - offset;
    if (0 > index || index >= table.length) {
      throw new InvalidObjectException("Integer value = " + value + " not in valid range " + offset + ".."
          + (offset + table.length - 1) + "for class " + getClass());
    }
    EnumSyntax result = table[index];
    if (result == null) {
      throw new InvalidObjectException("No enumeration value for integer value = " + value + "for class " + getClass());
    }
    return result;
  }

  protected String[] getStringTable() { return null; }

  protected EnumSyntax[] getEnumValueTable() { return null; }

  protected int getOffset() { return 0; }
}
