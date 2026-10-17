/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class MBeanInfo implements Cloneable, Serializable, DescriptorRead {
  private static final long serialVersionUID = -6451021435135161911L;

  private static final MBeanAttributeInfo[] NO_ATTRIBUTES = new MBeanAttributeInfo[0];
  private static final MBeanOperationInfo[] NO_OPERATIONS = new MBeanOperationInfo[0];
  private static final MBeanConstructorInfo[] NO_CONSTRUCTORS = new MBeanConstructorInfo[0];
  private static final MBeanNotificationInfo[] NO_NOTIFICATIONS = new MBeanNotificationInfo[0];

  private final String description;
  private final String className;
  private final MBeanAttributeInfo[] attributes;
  private final MBeanOperationInfo[] operations;
  private final MBeanConstructorInfo[] constructors;
  private final MBeanNotificationInfo[] notifications;
  private final transient Descriptor descriptor;

  public MBeanInfo(String className, String description, MBeanAttributeInfo[] attributes,
      MBeanConstructorInfo[] constructors, MBeanOperationInfo[] operations, MBeanNotificationInfo[] notifications)
      throws IllegalArgumentException {
    this(className, description, attributes, constructors, operations, notifications, null);
  }

  public MBeanInfo(String className, String description, MBeanAttributeInfo[] attributes,
      MBeanConstructorInfo[] constructors, MBeanOperationInfo[] operations, MBeanNotificationInfo[] notifications,
      Descriptor descriptor) throws IllegalArgumentException {
    this.className = className;
    this.description = description;
    this.attributes = attributes == null || attributes.length == 0 ? NO_ATTRIBUTES : attributes.clone();
    this.operations = operations == null || operations.length == 0 ? NO_OPERATIONS : operations.clone();
    this.constructors = constructors == null || constructors.length == 0 ? NO_CONSTRUCTORS : constructors.clone();
    this.notifications = notifications == null || notifications.length == 0 ? NO_NOTIFICATIONS
        : notifications.clone();
    this.descriptor = descriptor == null ? ImmutableDescriptor.EMPTY_DESCRIPTOR : descriptor;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public String getClassName() {
    return className;
  }

  public String getDescription() {
    return description;
  }

  public MBeanAttributeInfo[] getAttributes() {
    return attributes.length == 0 ? attributes : attributes.clone();
  }

  public MBeanOperationInfo[] getOperations() {
    return operations.length == 0 ? operations : operations.clone();
  }

  public MBeanConstructorInfo[] getConstructors() {
    return constructors.length == 0 ? constructors : constructors.clone();
  }

  public MBeanNotificationInfo[] getNotifications() {
    return notifications.length == 0 ? notifications : notifications.clone();
  }

  @Override
  public Descriptor getDescriptor() {
    return (Descriptor) ImmutableDescriptor.nonNullDescriptor(descriptor).clone();
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" + "description=" + getDescription() + ", " + "attributes="
        + Arrays.asList(attributes) + ", " + "constructors=" + Arrays.asList(constructors) + ", " + "operations="
        + Arrays.asList(operations) + ", " + "notifications=" + Arrays.asList(notifications) + ", " + "descriptor="
        + getDescriptor() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MBeanInfo)) {
      return false;
    }
    MBeanInfo p = (MBeanInfo) o;
    return Objects.equals(p.getClassName(), getClassName()) && Objects.equals(p.getDescription(), getDescription())
        && Objects.equals(p.getDescriptor(), getDescriptor()) && Arrays.equals(p.attributes, attributes)
        && Arrays.equals(p.operations, operations) && Arrays.equals(p.constructors, constructors)
        && Arrays.equals(p.notifications, notifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClassName(), getDescriptor()) ^ Arrays.hashCode(attributes) ^ Arrays.hashCode(operations)
        ^ Arrays.hashCode(constructors) ^ Arrays.hashCode(notifications);
  }
}
