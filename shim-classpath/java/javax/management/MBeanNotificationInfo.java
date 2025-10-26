/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.util.Arrays;
import java.util.Objects;

public class MBeanNotificationInfo extends MBeanFeatureInfo implements Cloneable {
  private static final long serialVersionUID = -3888371564530107064L;

  private static final String[] NO_TYPES = new String[0];

  private final String[] types;

  public MBeanNotificationInfo(String[] notifTypes, String name, String description) {
    this(notifTypes, name, description, null);
  }

  public MBeanNotificationInfo(String[] notifTypes, String name, String description, Descriptor descriptor) {
    super(name, description, descriptor);
    this.types = notifTypes != null && notifTypes.length > 0 ? notifTypes.clone() : NO_TYPES;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public String[] getNotifTypes() {
    return types.length == 0 ? NO_TYPES : types.clone();
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" + "description=" + getDescription() + ", " + "name=" + getName() + ", "
        + "notifTypes=" + Arrays.asList(types) + ", " + "descriptor=" + getDescriptor() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MBeanNotificationInfo)) {
      return false;
    }
    MBeanNotificationInfo p = (MBeanNotificationInfo) o;
    return Objects.equals(p.getName(), getName()) && Objects.equals(p.getDescription(), getDescription())
        && Objects.equals(p.getDescriptor(), getDescriptor()) && Arrays.equals(p.types, types);
  }

  @Override
  public int hashCode() {
    int hash = getName().hashCode();
    for (String type : types) {
      hash ^= type.hashCode();
    }
    return hash;
  }
}
