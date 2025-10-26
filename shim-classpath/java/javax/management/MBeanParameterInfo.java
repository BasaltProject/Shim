/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.util.Objects;

public class MBeanParameterInfo extends MBeanFeatureInfo implements Cloneable {
  private static final long serialVersionUID = 7432616882776782338L;

  private final String type;

  public MBeanParameterInfo(String name, String type, String description) {
    this(name, type, description, null);
  }

  public MBeanParameterInfo(String name, String type, String description, Descriptor descriptor) {
    super(name, description, descriptor);
    this.type = type;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" + "description=" + getDescription() + ", " + "name=" + getName() + ", "
        + "type=" + getType() + ", " + "descriptor=" + getDescriptor() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MBeanParameterInfo)) {
      return false;
    }
    MBeanParameterInfo p = (MBeanParameterInfo) o;
    return Objects.equals(p.getName(), getName()) && Objects.equals(p.getType(), getType())
        && Objects.equals(p.getDescription(), getDescription()) && Objects.equals(p.getDescriptor(), getDescriptor());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getType());
  }
}
