/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.management;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.ImmutableDescriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import javax.management.ReflectionException;

public class MBeanSupport implements DynamicMBean {
  private static final String kDescription = "Information on the management interface of the MBean";

  private static final class Property {
    final String name;
    Method getter;
    Method setter;
    Class<?> type;

    Property(String name) {
      this.name = name;
    }
  }

  private final Object resource;
  private final Class<?> intf;
  private final boolean mx;
  private final Map<String, Property> properties = new LinkedHashMap<String, Property>();
  private final List<Method> operations = new ArrayList<Method>();
  private MBeanInfo info;

  public MBeanSupport(Object resource, Class<?> intf, boolean mx) throws NotCompliantMBeanException {
    this.resource = resource;
    this.intf = intf;
    this.mx = mx;
    analyze();
  }

  public Object getResource() {
    return resource;
  }

  public Class<?> getInterface() {
    return intf;
  }

  public boolean isMXBean() {
    return mx;
  }

  private void analyze() throws NotCompliantMBeanException {
    for (Method m : intf.getMethods()) {
      if (m.getDeclaringClass() == Object.class || Modifier.isStatic(m.getModifiers())) {
        continue;
      }
      String n = m.getName();
      Class<?>[] params = m.getParameterTypes();
      Class<?> ret = m.getReturnType();
      if (n.startsWith("get") && n.length() > 3 && params.length == 0 && ret != Void.TYPE) {
        property(n.substring(3)).getter = m;
        property(n.substring(3)).type = ret;
      } else if (n.startsWith("is") && n.length() > 2 && params.length == 0 && ret == boolean.class) {
        property(n.substring(2)).getter = m;
        property(n.substring(2)).type = ret;
      } else if (n.startsWith("set") && n.length() > 3 && params.length == 1 && ret == Void.TYPE) {
        Property p = property(n.substring(3));
        if (p.type != null && p.type != params[0] && p.getter != null) {
          throw new NotCompliantMBeanException("Getter and setter for " + p.name + " have inconsistent types");
        }
        p.setter = m;
        if (p.type == null) {
          p.type = params[0];
        }
      } else {
        operations.add(m);
      }
    }
  }

  private Property property(String name) {
    Property p = properties.get(name);
    if (p == null) {
      p = new Property(name);
      properties.put(name, p);
    }
    return p;
  }

  private String typeName(Class<?> c) {
    if (!mx) {
      return c.getName();
    }
    return OpenMapper.openClassName(c);
  }

  @Override
  public synchronized MBeanInfo getMBeanInfo() {
    if (info == null) {
      info = buildInfo();
    }
    return info;
  }

  private MBeanInfo buildInfo() {
    List<MBeanAttributeInfo> attrs = new ArrayList<MBeanAttributeInfo>();
    for (Property p : properties.values()) {
      boolean isIs = p.getter != null && p.getter.getName().startsWith("is");
      String type = typeName(p.type);
      Descriptor d = mx ? OpenMapper.attributeDescriptor(p.type) : null;
      attrs.add(new MBeanAttributeInfo(p.name, type, mx ? p.name : "Attribute exposed for management",
          p.getter != null, p.setter != null, isIs, d));
    }
    List<MBeanOperationInfo> ops = new ArrayList<MBeanOperationInfo>();
    for (Method m : operations) {
      Class<?>[] pt = m.getParameterTypes();
      MBeanParameterInfo[] sig = new MBeanParameterInfo[pt.length];
      for (int i = 0; i < pt.length; i++) {
        sig[i] = new MBeanParameterInfo("p" + (i + 1), typeName(pt[i]), "",
            mx ? OpenMapper.attributeDescriptor(pt[i]) : null);
      }
      ops.add(new MBeanOperationInfo(m.getName(), mx ? m.getName() : "Operation exposed for management", sig,
          typeName(m.getReturnType()),
          MBeanOperationInfo.UNKNOWN, mx ? OpenMapper.attributeDescriptor(m.getReturnType()) : null));
    }
    List<MBeanConstructorInfo> ctors = new ArrayList<MBeanConstructorInfo>();
    for (java.lang.reflect.Constructor<?> c : resource.getClass().getConstructors()) {
      ctors.add(new MBeanConstructorInfo("Public constructor of the MBean", c));
    }
    MBeanNotificationInfo[] notifs = resource instanceof NotificationBroadcaster
        ? ((NotificationBroadcaster) resource).getNotificationInfo() : null;
    return new MBeanInfo(resource.getClass().getName(), kDescription, attrs.toArray(new MBeanAttributeInfo[0]),
        ctors.toArray(new MBeanConstructorInfo[0]), ops.toArray(new MBeanOperationInfo[0]), notifs,
        new ImmutableDescriptor(new String[] {"immutableInfo", "interfaceClassName", "mxbean"},
            new Object[] {"true", intf.getName(), mx ? "true" : "false"}));
  }

  @Override
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException,
      ReflectionException {
    Property p = properties.get(attribute);
    if (p == null) {
      throw new AttributeNotFoundException("No such attribute: " + attribute);
    }
    if (p.getter == null) {
      throw new AttributeNotFoundException("Write-only attribute: " + attribute);
    }
    Object value = call(p.getter, new Object[0]);
    return mx ? OpenMapper.toOpen(value) : value;
  }

  @Override
  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
      MBeanException, ReflectionException {
    Property p = properties.get(attribute.getName());
    if (p == null) {
      throw new AttributeNotFoundException("No such attribute: " + attribute.getName());
    }
    if (p.setter == null) {
      throw new AttributeNotFoundException("Read-only attribute: " + attribute.getName());
    }
    Object v = attribute.getValue();
    Class<?> t = p.setter.getParameterTypes()[0];
    if (v == null ? t.isPrimitive() : !wrap(t).isInstance(v)) {
      throw new InvalidAttributeValueException("Invalid value for attribute " + attribute.getName() + ": " + v);
    }
    call(p.setter, new Object[] {v});
  }

  private static Class<?> wrap(Class<?> c) {
    if (!c.isPrimitive()) {
      return c;
    }
    if (c == int.class) {
      return Integer.class;
    }
    if (c == long.class) {
      return Long.class;
    }
    if (c == boolean.class) {
      return Boolean.class;
    }
    if (c == double.class) {
      return Double.class;
    }
    if (c == float.class) {
      return Float.class;
    }
    if (c == short.class) {
      return Short.class;
    }
    if (c == byte.class) {
      return Byte.class;
    }
    return Character.class;
  }

  @Override
  public AttributeList getAttributes(String[] attributes) {
    AttributeList list = new AttributeList();
    for (String a : attributes) {
      try {
        list.add(new Attribute(a, getAttribute(a)));
      } catch (Exception e) {
        continue;
      }
    }
    return list;
  }

  @Override
  public AttributeList setAttributes(AttributeList attributes) {
    AttributeList list = new AttributeList();
    for (Attribute a : attributes.asList()) {
      try {
        setAttribute(a);
        list.add(new Attribute(a.getName(), a.getValue()));
      } catch (Exception e) {
        continue;
      }
    }
    return list;
  }

  @Override
  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
      ReflectionException {
    if (params == null) {
      params = new Object[0];
    }
    if (signature == null) {
      signature = new String[0];
    }
    for (Method m : operations) {
      if (!m.getName().equals(actionName) || m.getParameterTypes().length != signature.length) {
        continue;
      }
      boolean same = true;
      Class<?>[] pt = m.getParameterTypes();
      for (int i = 0; i < pt.length; i++) {
        if (!pt[i].getName().equals(signature[i])) {
          same = false;
        }
      }
      if (same) {
        return call(m, params);
      }
    }
    StringBuilder sig = new StringBuilder("(");
    for (int i = 0; i < signature.length; i++) {
      sig.append(i > 0 ? ", " : "").append(signature[i]);
    }
    sig.append(")");
    for (Method m : operations) {
      if (m.getName().equals(actionName) && m.getParameterTypes().length == signature.length) {
        StringBuilder want = new StringBuilder("(");
        Class<?>[] pt = m.getParameterTypes();
        for (int i = 0; i < pt.length; i++) {
          want.append(i > 0 ? ", " : "").append(pt[i].getName());
        }
        want.append(")");
        throw new ReflectionException(new NoSuchMethodException(actionName + sig), "Signature mismatch for "
            + "operation " + actionName + ": " + sig + " should be " + want);
      }
    }
    throw new ReflectionException(new NoSuchMethodException(actionName + sig), "No such operation: "
        + actionName);
  }

  private Object call(Method m, Object[] args) throws MBeanException, ReflectionException {
    try {
      Method target = resource.getClass().getMethod(m.getName(), m.getParameterTypes());
      if (!target.isAccessible()) {
        try {
          target.setAccessible(true);
        } catch (RuntimeException ignored) {
          // inaccessible implementations fall through to the interface method
        }
      }
      return target.invoke(resource, args);
    } catch (InvocationTargetException e) {
      Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
      if (t instanceof Error) {
        throw (Error) t;
      }
      throw new MBeanException((Exception) t, t.toString());
    } catch (NoSuchMethodException e) {
      throw new ReflectionException(e, e.toString());
    } catch (IllegalAccessException e) {
      throw new ReflectionException(e, e.toString());
    }
  }
}
