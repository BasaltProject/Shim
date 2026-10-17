/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.lang.reflect.Method;
import java.util.Objects;

public class MBeanAttributeInfo extends MBeanFeatureInfo implements Cloneable {
  private static final long serialVersionUID = 8644704819898565848L;

  private final String attributeType;
  private final boolean isWrite;
  private final boolean isRead;
  private final boolean is;

  public MBeanAttributeInfo(String name, String type, String description, boolean isReadable, boolean isWritable,
      boolean isIs) {
    this(name, type, description, isReadable, isWritable, isIs, (Descriptor) null);
  }

  public MBeanAttributeInfo(String name, String type, String description, boolean isReadable, boolean isWritable,
      boolean isIs, Descriptor descriptor) {
    super(name, description, descriptor);
    this.attributeType = type;
    this.isRead = isReadable;
    this.isWrite = isWritable;
    if (isIs && !isReadable) {
      throw new IllegalArgumentException("Cannot have an \"is\" getter for a non-readable attribute");
    }
    if (isIs && !type.equals("java.lang.Boolean") && !type.equals("boolean")) {
      throw new IllegalArgumentException("Cannot have an \"is\" getter for a non-boolean attribute");
    }
    this.is = isIs;
  }

  public MBeanAttributeInfo(String name, String description, Method getter, Method setter)
      throws IntrospectionException {
    this(name, attributeType(getter, setter), description, getter != null, setter != null,
        getter != null && isIs(getter));
  }

  private static boolean isIs(Method getter) {
    return getter.getName().startsWith("is") && getter.getReturnType() == boolean.class;
  }

  private static String attributeType(Method getter, Method setter) throws IntrospectionException {
    Class<?> type = null;
    if (getter != null) {
      if (getter.getParameterTypes().length != 0) {
        throw new IntrospectionException("bad getter arg count");
      }
      type = getter.getReturnType();
      if (type == Void.TYPE) {
        throw new IntrospectionException("getter return type is void");
      }
    }
    if (setter != null) {
      Class<?>[] params = setter.getParameterTypes();
      if (params.length != 1) {
        throw new IntrospectionException("bad setter arg count");
      }
      if (type == null) {
        type = params[0];
      } else if (type != params[0]) {
        throw new IntrospectionException("type mismatch between getter and setter");
      }
    }
    if (type == null) {
      throw new IntrospectionException("getter and setter cannot both be null");
    }
    return type.getName();
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
    return attributeType;
  }

  public boolean isReadable() {
    return isRead;
  }

  public boolean isWritable() {
    return isWrite;
  }

  public boolean isIs() {
    return is;
  }

  @Override
  public String toString() {
    String access;
    if (isReadable()) {
      access = isWritable() ? "read/write" : "read-only";
    } else {
      access = isWritable() ? "write-only" : "no-access";
    }
    return getClass().getName() + "[" + "description=" + getDescription() + ", " + "name=" + getName() + ", "
        + "type=" + getType() + ", " + access + ", " + (isIs() ? "isIs, " : "") + "descriptor=" + getDescriptor()
        + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MBeanAttributeInfo)) {
      return false;
    }
    MBeanAttributeInfo p = (MBeanAttributeInfo) o;
    return Objects.equals(p.getName(), getName()) && Objects.equals(p.getType(), getType())
        && p.isReadable() == isReadable() && p.isWritable() == isWritable() && p.isIs() == isIs()
        && Objects.equals(p.getDescription(), getDescription()) && Objects.equals(p.getDescriptor(), getDescriptor());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getType());
  }
}
