/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.RuntimeOperationsException;

public class ModelMBeanInfoSupport extends MBeanInfo implements ModelMBeanInfo {
  private static final long serialVersionUID = -1935722590756516193L;

  private ModelMBeanAttributeInfo[] modelMBeanAttributes;
  private ModelMBeanConstructorInfo[] modelMBeanConstructors;
  private ModelMBeanOperationInfo[] modelMBeanOperations;
  private ModelMBeanNotificationInfo[] modelMBeanNotifications;
  private Descriptor modelMBeanDescriptor;

  public ModelMBeanInfoSupport(ModelMBeanInfo mbi) {
    super(mbi.getClassName(), mbi.getDescription(), mbi.getAttributes(), mbi.getConstructors(), mbi.getOperations(),
        mbi.getNotifications());
    modelMBeanAttributes = copyAttrs(mbi.getAttributes());
    modelMBeanConstructors = copyCtors(mbi.getConstructors());
    modelMBeanOperations = copyOps(mbi.getOperations());
    modelMBeanNotifications = copyNotifs(mbi.getNotifications());
    try {
      modelMBeanDescriptor = mbi.getMBeanDescriptor();
    } catch (MBeanException e) {
      modelMBeanDescriptor = null;
    }
    if (modelMBeanDescriptor == null) {
      modelMBeanDescriptor = defaultDescriptor(mbi.getClassName());
    }
  }

  public ModelMBeanInfoSupport(String className, String description, ModelMBeanAttributeInfo[] attributes,
      ModelMBeanConstructorInfo[] constructors, ModelMBeanOperationInfo[] operations,
      ModelMBeanNotificationInfo[] notifications) {
    this(className, description, attributes, constructors, operations, notifications, null);
  }

  public ModelMBeanInfoSupport(String className, String description, ModelMBeanAttributeInfo[] attributes,
      ModelMBeanConstructorInfo[] constructors, ModelMBeanOperationInfo[] operations,
      ModelMBeanNotificationInfo[] notifications, Descriptor mbeanDescriptor) {
    super(className, description, attributes == null ? new ModelMBeanAttributeInfo[0] : attributes,
        constructors == null ? new ModelMBeanConstructorInfo[0] : constructors,
        operations == null ? new ModelMBeanOperationInfo[0] : operations,
        notifications == null ? new ModelMBeanNotificationInfo[0] : notifications);
    modelMBeanAttributes = copyAttrs(attributes);
    modelMBeanConstructors = copyCtors(constructors);
    modelMBeanOperations = copyOps(operations);
    modelMBeanNotifications = copyNotifs(notifications);
    if (mbeanDescriptor == null) {
      modelMBeanDescriptor = defaultDescriptor(className);
    } else {
      modelMBeanDescriptor = (Descriptor) mbeanDescriptor.clone();
    }
  }

  private static Descriptor defaultDescriptor(String className) {
    return new DescriptorSupport(new String[] {"name=" + className, "descriptorType=mbean", "displayName=" + className,
        "persistPolicy=never", "log=F", "visibility=1"});
  }

  private static ModelMBeanAttributeInfo[] copyAttrs(MBeanAttributeInfo[] in) {
    if (in == null) {
      return new ModelMBeanAttributeInfo[0];
    }
    ModelMBeanAttributeInfo[] out = new ModelMBeanAttributeInfo[in.length];
    for (int i = 0; i < in.length; i++) {
      out[i] = (ModelMBeanAttributeInfo) ((ModelMBeanAttributeInfo) in[i]).clone();
    }
    return out;
  }

  private static ModelMBeanConstructorInfo[] copyCtors(MBeanConstructorInfo[] in) {
    if (in == null) {
      return new ModelMBeanConstructorInfo[0];
    }
    ModelMBeanConstructorInfo[] out = new ModelMBeanConstructorInfo[in.length];
    for (int i = 0; i < in.length; i++) {
      out[i] = (ModelMBeanConstructorInfo) ((ModelMBeanConstructorInfo) in[i]).clone();
    }
    return out;
  }

  private static ModelMBeanOperationInfo[] copyOps(MBeanOperationInfo[] in) {
    if (in == null) {
      return new ModelMBeanOperationInfo[0];
    }
    ModelMBeanOperationInfo[] out = new ModelMBeanOperationInfo[in.length];
    for (int i = 0; i < in.length; i++) {
      out[i] = (ModelMBeanOperationInfo) ((ModelMBeanOperationInfo) in[i]).clone();
    }
    return out;
  }

  private static ModelMBeanNotificationInfo[] copyNotifs(MBeanNotificationInfo[] in) {
    if (in == null) {
      return new ModelMBeanNotificationInfo[0];
    }
    ModelMBeanNotificationInfo[] out = new ModelMBeanNotificationInfo[in.length];
    for (int i = 0; i < in.length; i++) {
      out[i] = (ModelMBeanNotificationInfo) ((ModelMBeanNotificationInfo) in[i]).clone();
    }
    return out;
  }

  @Override
  public MBeanAttributeInfo[] getAttributes() {
    return copyAttrs(modelMBeanAttributes);
  }

  @Override
  public MBeanConstructorInfo[] getConstructors() {
    return copyCtors(modelMBeanConstructors);
  }

  @Override
  public MBeanOperationInfo[] getOperations() {
    return copyOps(modelMBeanOperations);
  }

  @Override
  public MBeanNotificationInfo[] getNotifications() {
    return copyNotifs(modelMBeanNotifications);
  }

  @Override
  public Object clone() {
    return new ModelMBeanInfoSupport(this);
  }

  @Override
  public Descriptor getDescriptor() {
    return (Descriptor) modelMBeanDescriptor.clone();
  }

  public Descriptor[] getDescriptors(String inDescriptorType) throws MBeanException, RuntimeOperationsException {
    java.util.List<Descriptor> out = new java.util.ArrayList<Descriptor>();
    if (inDescriptorType == null || inDescriptorType.equals("")) {
      inDescriptorType = "all";
    }
    boolean all = inDescriptorType.equalsIgnoreCase("all");
    if (all || inDescriptorType.equalsIgnoreCase("mbean")) {
      out.add(modelMBeanDescriptor);
    }
    if (all || inDescriptorType.equalsIgnoreCase("attribute")) {
      for (ModelMBeanAttributeInfo a : modelMBeanAttributes) {
        out.add(a.getDescriptor());
      }
    }
    if (all || inDescriptorType.equalsIgnoreCase("operation")) {
      for (ModelMBeanOperationInfo o : modelMBeanOperations) {
        out.add(o.getDescriptor());
      }
    }
    if (all || inDescriptorType.equalsIgnoreCase("constructor")) {
      for (ModelMBeanConstructorInfo c : modelMBeanConstructors) {
        out.add(c.getDescriptor());
      }
    }
    if (all || inDescriptorType.equalsIgnoreCase("notification")) {
      for (ModelMBeanNotificationInfo n : modelMBeanNotifications) {
        out.add(n.getDescriptor());
      }
    }
    if (!all && out.isEmpty() && !isKnownType(inDescriptorType)) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor Type is invalid"),
          "Exception occurred trying to find the descriptors of the MBean");
    }
    return out.toArray(new Descriptor[0]);
  }

  private static boolean isKnownType(String t) {
    return t.equalsIgnoreCase("mbean") || t.equalsIgnoreCase("attribute") || t.equalsIgnoreCase("operation")
        || t.equalsIgnoreCase("constructor") || t.equalsIgnoreCase("notification");
  }

  public void setDescriptors(Descriptor[] inDescriptors) throws MBeanException, RuntimeOperationsException {
    if (inDescriptors == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor list is invalid"),
          "Exception occurred trying to set the descriptors of the MBean.");
    }
    if (inDescriptors.length == 0) {
      return;
    }
    for (Descriptor d : inDescriptors) {
      setDescriptor(d, null);
    }
  }

  public Descriptor getDescriptor(String inDescriptorName) throws MBeanException, RuntimeOperationsException {
    return getDescriptor(inDescriptorName, null);
  }

  public Descriptor getDescriptor(String inDescriptorName, String inDescriptorType) throws MBeanException,
      RuntimeOperationsException {
    if (inDescriptorName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor is invalid"),
          "Exception occurred trying to get the descriptor of the MBean.");
    }
    if ("mbean".equalsIgnoreCase(inDescriptorType)) {
      return (Descriptor) modelMBeanDescriptor.clone();
    }
    if (inDescriptorType == null || "attribute".equalsIgnoreCase(inDescriptorType)) {
      ModelMBeanAttributeInfo a = getAttribute(inDescriptorName);
      if (a != null) {
        return a.getDescriptor();
      }
    }
    if (inDescriptorType == null || "operation".equalsIgnoreCase(inDescriptorType)) {
      ModelMBeanOperationInfo o = getOperation(inDescriptorName);
      if (o != null) {
        return o.getDescriptor();
      }
    }
    if (inDescriptorType == null || "constructor".equalsIgnoreCase(inDescriptorType)) {
      ModelMBeanConstructorInfo c = getConstructor(inDescriptorName);
      if (c != null) {
        return c.getDescriptor();
      }
    }
    if (inDescriptorType == null || "notification".equalsIgnoreCase(inDescriptorType)) {
      ModelMBeanNotificationInfo n = getNotification(inDescriptorName);
      if (n != null) {
        return n.getDescriptor();
      }
    }
    if (inDescriptorType != null && !isKnownType(inDescriptorType)) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor Type is invalid"),
          "Exception occurred trying to get the descriptor of the MBean.");
    }
    return null;
  }

  public void setDescriptor(Descriptor inDescriptor, String inDescriptorType) throws MBeanException,
      RuntimeOperationsException {
    if (inDescriptor == null) {
      inDescriptor = new DescriptorSupport();
    }
    if (inDescriptorType == null) {
      inDescriptorType = (String) inDescriptor.getFieldValue("descriptorType");
    }
    String name = (String) inDescriptor.getFieldValue("name");
    if (inDescriptorType == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor type is null"),
          "Exception occurred trying to set the descriptor of the MBean.");
    }
    if (inDescriptorType.equalsIgnoreCase("mbean")) {
      setMBeanDescriptor(inDescriptor);
      return;
    }
    if (name == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor name is null"),
          "Exception occurred trying to set the descriptor of the MBean.");
    }
    if (inDescriptorType.equalsIgnoreCase("attribute")) {
      ModelMBeanAttributeInfo a = rawAttribute(name);
      if (a != null) {
        a.setDescriptor(inDescriptor);
      }
    } else if (inDescriptorType.equalsIgnoreCase("operation")) {
      ModelMBeanOperationInfo o = rawOperation(name);
      if (o != null) {
        o.setDescriptor(inDescriptor);
      }
    } else if (inDescriptorType.equalsIgnoreCase("constructor")) {
      ModelMBeanConstructorInfo c = rawConstructor(name);
      if (c != null) {
        c.setDescriptor(inDescriptor);
      }
    } else if (inDescriptorType.equalsIgnoreCase("notification")) {
      ModelMBeanNotificationInfo n = rawNotification(name);
      if (n != null) {
        n.setDescriptor(inDescriptor);
      }
    } else {
      throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor Type is invalid"),
          "Exception occurred trying to set the descriptor of the MBean.");
    }
  }

  private ModelMBeanAttributeInfo rawAttribute(String n) {
    for (ModelMBeanAttributeInfo a : modelMBeanAttributes) {
      if (n.equals(a.getName())) {
        return a;
      }
    }
    return null;
  }

  private ModelMBeanOperationInfo rawOperation(String n) {
    for (ModelMBeanOperationInfo o : modelMBeanOperations) {
      if (n.equals(o.getName())) {
        return o;
      }
    }
    return null;
  }

  private ModelMBeanConstructorInfo rawConstructor(String n) {
    for (ModelMBeanConstructorInfo c : modelMBeanConstructors) {
      if (n.equals(c.getName())) {
        return c;
      }
    }
    return null;
  }

  private ModelMBeanNotificationInfo rawNotification(String n) {
    for (ModelMBeanNotificationInfo x : modelMBeanNotifications) {
      if (n.equals(x.getName())) {
        return x;
      }
    }
    return null;
  }

  public ModelMBeanAttributeInfo getAttribute(String inName) throws MBeanException, RuntimeOperationsException {
    if (inName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Attribute Name is null"),
          "Exception occurred trying to get the ModelMBeanAttributeInfo of the MBean.");
    }
    for (ModelMBeanAttributeInfo a : modelMBeanAttributes) {
      if (inName.equals(a.getName())) {
        return (ModelMBeanAttributeInfo) a.clone();
      }
    }
    return null;
  }

  public ModelMBeanOperationInfo getOperation(String inName) throws MBeanException, RuntimeOperationsException {
    if (inName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Operation Name is null"),
          "Exception occurred trying to get the ModelMBeanOperationInfo of the MBean.");
    }
    for (ModelMBeanOperationInfo o : modelMBeanOperations) {
      if (inName.equals(o.getName())) {
        return (ModelMBeanOperationInfo) o.clone();
      }
    }
    return null;
  }

  public ModelMBeanConstructorInfo getConstructor(String inName) throws MBeanException, RuntimeOperationsException {
    if (inName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Constructor Name is null"),
          "Exception occurred trying to get the ModelMBeanConstructorInfo of the MBean.");
    }
    for (ModelMBeanConstructorInfo c : modelMBeanConstructors) {
      if (inName.equals(c.getName())) {
        return (ModelMBeanConstructorInfo) c.clone();
      }
    }
    return null;
  }

  public ModelMBeanNotificationInfo getNotification(String inName) throws MBeanException, RuntimeOperationsException {
    if (inName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Notification Name is null"),
          "Exception occurred trying to get the ModelMBeanNotificationInfo of the MBean.");
    }
    for (ModelMBeanNotificationInfo n : modelMBeanNotifications) {
      if (inName.equals(n.getName())) {
        return (ModelMBeanNotificationInfo) n.clone();
      }
    }
    return null;
  }

  public Descriptor getMBeanDescriptor() throws MBeanException {
    return (Descriptor) modelMBeanDescriptor.clone();
  }

  public void setMBeanDescriptor(Descriptor inMBeanDescriptor) throws MBeanException, RuntimeOperationsException {
    if (inMBeanDescriptor == null) {
      modelMBeanDescriptor = defaultDescriptor(getClassName());
    } else {
      modelMBeanDescriptor = (Descriptor) inMBeanDescriptor.clone();
    }
  }
}
