/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.io.Serializable;

public class Attribute implements Serializable {
  private static final long serialVersionUID = 2484220110589082382L;

  private String name;
  private Object value;

  public Attribute(String name, Object value) {
    if (name == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null "));
    }
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Attribute)) {
      return false;
    }
    Attribute val = (Attribute) object;
    if (value == null) {
      if (val.getValue() == null) {
        return name.equals(val.getName());
      }
      return false;
    }
    return name.equals(val.getName()) && value.equals(val.getValue());
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ (value == null ? 0 : value.hashCode());
  }

  @Override
  public String toString() {
    return getName() + " = " + getValue();
  }
}
