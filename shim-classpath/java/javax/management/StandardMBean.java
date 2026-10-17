/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.lang.reflect.Method;
import shim.management.MBeanSupport;

public class StandardMBean implements DynamicMBean, MBeanRegistration {
  private volatile MBeanSupport support;
  private final boolean isMXBean;
  private volatile Class<?> mbeanInterface;
  private volatile MBeanInfo cachedMBeanInfo;

  public <T> StandardMBean(T implementation, Class<T> mbeanInterface) throws NotCompliantMBeanException {
    this.isMXBean = false;
    construct(implementation, mbeanInterface);
  }

  protected StandardMBean(Class<?> mbeanInterface) throws NotCompliantMBeanException {
    this.isMXBean = false;
    construct(this, mbeanInterface);
  }

  public <T> StandardMBean(T implementation, Class<T> mbeanInterface, boolean isMXBean) {
    try {
      this.isMXBean = isMXBean;
      construct(implementation, mbeanInterface);
    } catch (NotCompliantMBeanException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected StandardMBean(Class<?> mbeanInterface, boolean isMXBean) {
    try {
      this.isMXBean = isMXBean;
      construct(this, mbeanInterface);
    } catch (NotCompliantMBeanException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private void construct(Object implementation, Class<?> intf) throws NotCompliantMBeanException {
    if (implementation == null) {
      if (this.getClass() == StandardMBean.class) {
        throw new IllegalArgumentException("implementation is null");
      }
      implementation = this;
    }
    if (intf == null) {
      intf = findInterface(implementation.getClass());
    }
    if (!intf.isInterface()) {
      throw new NotCompliantMBeanException("Not an interface: " + intf.getName());
    }
    if (!intf.isInstance(implementation)) {
      throw new NotCompliantMBeanException("Resource class " + implementation.getClass().getName()
          + " is not an instance of " + intf.getName());
    }
    this.mbeanInterface = intf;
    this.support = new MBeanSupport(implementation, intf, isMXBean);
    this.cachedMBeanInfo = null;
  }

  private Class<?> findInterface(Class<?> c) throws NotCompliantMBeanException {
    for (Class<?> k = c; k != null; k = k.getSuperclass()) {
      String want = k.getName() + (isMXBean ? "MXBean" : "MBean");
      for (Class<?> i : k.getInterfaces()) {
        if (i.getName().equals(want) || (isMXBean && JMX.isMXBeanInterface(i))) {
          return i;
        }
      }
    }
    throw new NotCompliantMBeanException("Class " + c.getName() + " is not a JMX compliant "
        + (isMXBean ? "MXBean" : "Standard MBean"));
  }

  public void setImplementation(Object implementation) throws NotCompliantMBeanException {
    if (implementation == null) {
      throw new IllegalArgumentException("implementation is null");
    }
    construct(implementation, mbeanInterface);
  }

  public Object getImplementation() {
    return support.getResource();
  }

  public final Class<?> getMBeanInterface() {
    return mbeanInterface;
  }

  public Class<?> getImplementationClass() {
    return support.getResource().getClass();
  }

  @Override
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException,
      ReflectionException {
    return support.getAttribute(attribute);
  }

  @Override
  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
      MBeanException, ReflectionException {
    support.setAttribute(attribute);
  }

  @Override
  public AttributeList getAttributes(String[] attributes) {
    return support.getAttributes(attributes);
  }

  @Override
  public AttributeList setAttributes(AttributeList attributes) {
    return support.setAttributes(attributes);
  }

  @Override
  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
      ReflectionException {
    return support.invoke(actionName, params, signature);
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    MBeanInfo cached = getCachedMBeanInfo();
    if (cached != null) {
      return cached;
    }
    MBeanInfo base = support.getMBeanInfo();
    MBeanAttributeInfo[] attrs = base.getAttributes();
    for (int i = 0; i < attrs.length; i++) {
      MBeanAttributeInfo a = attrs[i];
      attrs[i] = new MBeanAttributeInfo(a.getName(), a.getType(), getDescription(a), a.isReadable(), a.isWritable(),
          a.isIs(), a.getDescriptor());
    }
    MBeanOperationInfo[] ops = base.getOperations();
    for (int i = 0; i < ops.length; i++) {
      MBeanOperationInfo o = ops[i];
      MBeanParameterInfo[] sig = o.getSignature();
      for (int j = 0; j < sig.length; j++) {
        sig[j] = new MBeanParameterInfo(getParameterName(o, sig[j], j), sig[j].getType(),
            getDescription(o, sig[j], j), sig[j].getDescriptor());
      }
      ops[i] = new MBeanOperationInfo(o.getName(), getDescription(o), sig, o.getReturnType(), getImpact(o),
          o.getDescriptor());
    }
    MBeanConstructorInfo[] ctors = getConstructors(base.getConstructors(), support.getResource());
    if (ctors != null) {
      for (int i = 0; i < ctors.length; i++) {
        MBeanConstructorInfo c = ctors[i];
        MBeanParameterInfo[] sig = c.getSignature();
        for (int j = 0; j < sig.length; j++) {
          sig[j] = new MBeanParameterInfo(getParameterName(c, sig[j], j), sig[j].getType(),
              getDescription(c, sig[j], j), sig[j].getDescriptor());
        }
        ctors[i] = new MBeanConstructorInfo(c.getName(), getDescription(c), sig, c.getDescriptor());
      }
    }
    MBeanInfo result = new MBeanInfo(getClassName(base), getDescription(base), attrs, ctors, ops,
        base.getNotifications(), base.getDescriptor());
    cacheMBeanInfo(result);
    return result;
  }

  protected String getClassName(MBeanInfo info) {
    if (info == null) {
      return getImplementationClass().getName();
    }
    return info.getClassName();
  }

  protected String getDescription(MBeanInfo info) {
    if (info == null) {
      return null;
    }
    return info.getDescription();
  }

  protected String getDescription(MBeanFeatureInfo info) {
    if (info == null) {
      return null;
    }
    return info.getDescription();
  }

  protected String getDescription(MBeanAttributeInfo info) {
    return getDescription((MBeanFeatureInfo) info);
  }

  protected String getDescription(MBeanConstructorInfo info) {
    return getDescription((MBeanFeatureInfo) info);
  }

  protected String getDescription(MBeanConstructorInfo ctor, MBeanParameterInfo param, int sequence) {
    if (param == null) {
      return null;
    }
    return param.getDescription();
  }

  protected String getParameterName(MBeanConstructorInfo ctor, MBeanParameterInfo param, int sequence) {
    if (param == null) {
      return null;
    }
    return param.getName();
  }

  protected String getDescription(MBeanOperationInfo info) {
    return getDescription((MBeanFeatureInfo) info);
  }

  protected int getImpact(MBeanOperationInfo info) {
    if (info == null) {
      return MBeanOperationInfo.UNKNOWN;
    }
    return info.getImpact();
  }

  protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
    if (param == null) {
      return null;
    }
    return param.getName();
  }

  protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
    if (param == null) {
      return null;
    }
    return param.getDescription();
  }

  protected MBeanConstructorInfo[] getConstructors(MBeanConstructorInfo[] ctors, Object impl) {
    if (ctors == null) {
      return null;
    }
    if (impl != null && impl != this) {
      return null;
    }
    return ctors;
  }

  protected MBeanInfo getCachedMBeanInfo() {
    return cachedMBeanInfo;
  }

  protected void cacheMBeanInfo(MBeanInfo info) {
    cachedMBeanInfo = info;
  }

  @Override
  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    return name;
  }

  @Override
  public void postRegister(Boolean registrationDone) {
  }

  @Override
  public void preDeregister() throws Exception {
  }

  @Override
  public void postDeregister() {
  }
}
