/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.io.Serializable;
import java.util.Objects;

public class MBeanFeatureInfo implements Serializable, DescriptorRead {
  private static final long serialVersionUID = 3952882688968447265L;

  protected String name;
  protected String description;
  private final transient Descriptor descriptor;

  public MBeanFeatureInfo(String name, String description) {
    this(name, description, null);
  }

  public MBeanFeatureInfo(String name, String description, Descriptor descriptor) {
    this.name = name;
    this.description = description;
    this.descriptor = descriptor;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public Descriptor getDescriptor() {
    return (Descriptor) ImmutableDescriptor.nonNullDescriptor(descriptor).clone();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MBeanFeatureInfo)) {
      return false;
    }
    MBeanFeatureInfo p = (MBeanFeatureInfo) o;
    return Objects.equals(p.getName(), getName()) && Objects.equals(p.getDescription(), getDescription())
        && Objects.equals(p.getDescriptor(), getDescriptor());
  }

  @Override
  public int hashCode() {
    return getName().hashCode() ^ getDescription().hashCode() ^ getDescriptor().hashCode();
  }
}
