/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class MBeanOperationInfo extends MBeanFeatureInfo implements Cloneable {
  private static final long serialVersionUID = -6178860474881375330L;

  public static final int INFO = 0;
  public static final int ACTION = 1;
  public static final int ACTION_INFO = 2;
  public static final int UNKNOWN = 3;

  private final String type;
  private final MBeanParameterInfo[] signature;
  private final int impact;

  public MBeanOperationInfo(String description, Method method) {
    this(method.getName(), description, MBeanConstructorInfo.paramsOf(method.getParameterTypes()),
        method.getReturnType().getName(), UNKNOWN);
  }

  public MBeanOperationInfo(String name, String description, MBeanParameterInfo[] signature, String type,
      int impact) {
    this(name, description, signature, type, impact, (Descriptor) null);
  }

  public MBeanOperationInfo(String name, String description, MBeanParameterInfo[] signature, String type, int impact,
      Descriptor descriptor) {
    super(name, description, descriptor);
    if (signature == null || signature.length == 0) {
      signature = new MBeanParameterInfo[0];
    } else {
      signature = signature.clone();
    }
    this.signature = signature;
    this.type = type;
    this.impact = impact;
    if (impact != INFO && impact != ACTION && impact != ACTION_INFO && impact != UNKNOWN) {
      throw new IllegalArgumentException("Argument impact can only be one of ACTION, ACTION_INFO, INFO, or UNKNOWN "
          + "given value is :" + impact);
    }
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public String getReturnType() {
    return type;
  }

  public MBeanParameterInfo[] getSignature() {
    return signature.length == 0 ? signature : signature.clone();
  }

  public int getImpact() {
    return impact;
  }

  @Override
  public String toString() {
    String impactString;
    switch (getImpact()) {
    case ACTION:
      impactString = "action";
      break;
    case ACTION_INFO:
      impactString = "action/info";
      break;
    case INFO:
      impactString = "info";
      break;
    case UNKNOWN:
      impactString = "unknown";
      break;
    default:
      impactString = "(" + getImpact() + ")";
    }
    return getClass().getName() + "[" + "description=" + getDescription() + ", " + "name=" + getName() + ", "
        + "returnType=" + getReturnType() + ", " + "signature=" + Arrays.asList(signature) + ", " + "impact="
        + impactString + ", " + "descriptor=" + getDescriptor() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MBeanOperationInfo)) {
      return false;
    }
    MBeanOperationInfo p = (MBeanOperationInfo) o;
    return Objects.equals(p.getName(), getName()) && Objects.equals(p.getReturnType(), getReturnType())
        && Objects.equals(p.getDescription(), getDescription()) && p.getImpact() == getImpact()
        && Arrays.equals(p.signature, signature) && Objects.equals(p.getDescriptor(), getDescriptor());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getReturnType());
  }
}
