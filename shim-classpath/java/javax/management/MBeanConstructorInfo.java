/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;

public class MBeanConstructorInfo extends MBeanFeatureInfo implements Cloneable {
  private static final long serialVersionUID = 4433990064191844427L;

  private final MBeanParameterInfo[] signature;

  public MBeanConstructorInfo(String description, Constructor<?> constructor) {
    this(constructor.getName(), description, paramsOf(constructor.getParameterTypes()));
  }

  public MBeanConstructorInfo(String name, String description, MBeanParameterInfo[] signature) {
    this(name, description, signature, null);
  }

  public MBeanConstructorInfo(String name, String description, MBeanParameterInfo[] signature,
      Descriptor descriptor) {
    super(name, description, descriptor);
    this.signature = signature == null || signature.length == 0 ? new MBeanParameterInfo[0] : signature.clone();
  }

  static MBeanParameterInfo[] paramsOf(Class<?>[] types) {
    MBeanParameterInfo[] params = new MBeanParameterInfo[types.length];
    for (int i = 0; i < types.length; i++) {
      params[i] = new MBeanParameterInfo("p" + (i + 1), types[i].getName(), "");
    }
    return params;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public MBeanParameterInfo[] getSignature() {
    return signature.length == 0 ? signature : signature.clone();
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" + "description=" + getDescription() + ", " + "name=" + getName() + ", "
        + "signature=" + Arrays.asList(signature) + ", " + "descriptor=" + getDescriptor() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MBeanConstructorInfo)) {
      return false;
    }
    MBeanConstructorInfo p = (MBeanConstructorInfo) o;
    return Objects.equals(p.getName(), getName()) && Objects.equals(p.getDescription(), getDescription())
        && Arrays.equals(p.signature, signature) && Objects.equals(p.getDescriptor(), getDescriptor());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName()) + Arrays.hashCode(signature);
  }
}
