/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Objects;

public class PropertyDescriptor extends FeatureDescriptor {
  private Class<?> propertyType;
  private Method readMethod;
  private Method writeMethod;
  private boolean bound;
  private boolean constrained;
  private Class<?> propertyEditorClass;
  private Class<?> beanClass;
  private String readMethodName;
  private String writeMethodName;

  public PropertyDescriptor(String propertyName, Class<?> beanClass) throws IntrospectionException {
    this(propertyName, beanClass, Introspector.IS_PREFIX + capitalize(propertyName),
        Introspector.SET_PREFIX + capitalize(propertyName));
  }

  public PropertyDescriptor(String propertyName, Class<?> beanClass, String readMethodName, String writeMethodName)
      throws IntrospectionException {
    if (beanClass == null) {
      throw new IntrospectionException("Target Bean class is null");
    }
    if (propertyName == null || propertyName.length() == 0) {
      throw new IntrospectionException("bad property name");
    }
    if ("".equals(readMethodName) || "".equals(writeMethodName)) {
      throw new IntrospectionException("read or write method name should not be the empty string");
    }
    setName(propertyName);
    this.beanClass = beanClass;
    this.readMethodName = readMethodName;
    if (readMethodName != null && getReadMethod() == null) {
      throw new IntrospectionException("Method not found: " + readMethodName);
    }
    this.writeMethodName = writeMethodName;
    if (writeMethodName != null && getWriteMethod() == null) {
      throw new IntrospectionException("Method not found: " + writeMethodName);
    }
    this.bound = Introspector.findMethod(beanClass, "addPropertyChangeListener", 1,
        new Class<?>[] {PropertyChangeListener.class}) != null;
  }

  public PropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
      throws IntrospectionException {
    if (propertyName == null || propertyName.length() == 0) {
      throw new IntrospectionException("bad property name");
    }
    setName(propertyName);
    setReadMethod(readMethod);
    setWriteMethod(writeMethod);
  }

  PropertyDescriptor(Class<?> bean, String name, Method read, Method write) {
    setName(name);
    this.beanClass = bean;
    this.readMethod = read;
    this.writeMethod = write;
    this.readMethodName = read == null ? null : read.getName();
    this.writeMethodName = write == null ? null : write.getName();
    this.propertyType = read != null ? read.getReturnType() : write != null ? write.getParameterTypes()[0] : null;
  }

  PropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
    super(x, y);
    if (y.baseName != null) {
      baseName = y.baseName;
    } else {
      baseName = x.baseName;
    }
    if (y.readMethodName != null) {
      readMethodName = y.readMethodName;
    } else {
      readMethodName = x.readMethodName;
    }
    if (y.writeMethodName != null) {
      writeMethodName = y.writeMethodName;
    } else {
      writeMethodName = x.writeMethodName;
    }
    propertyEditorClass = y.propertyEditorClass != null ? y.propertyEditorClass : x.propertyEditorClass;
    bound = x.bound | y.bound;
    constrained = x.constrained | y.constrained;
    beanClass = y.beanClass != null ? y.beanClass : x.beanClass;
    readMethod = y.readMethod != null ? y.readMethod : x.readMethod;
    writeMethod = y.writeMethod != null ? y.writeMethod : x.writeMethod;
    propertyType = y.propertyType != null ? y.propertyType : x.propertyType;
  }

  PropertyDescriptor(PropertyDescriptor old) {
    super(old);
    propertyType = old.propertyType;
    readMethod = old.readMethod;
    writeMethod = old.writeMethod;
    propertyEditorClass = old.propertyEditorClass;
    bound = old.bound;
    constrained = old.constrained;
    beanClass = old.beanClass;
    readMethodName = old.readMethodName;
    writeMethodName = old.writeMethodName;
    baseName = old.baseName;
  }

  private String baseName;

  private static String capitalize(String name) {
    if (name == null || name.length() == 0) {
      return name;
    }
    return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
  }

  String getBaseName() {
    if (baseName == null) {
      baseName = capitalize(getName());
    }
    return baseName;
  }

  public synchronized Class<?> getPropertyType() {
    if (propertyType == null) {
      try {
        propertyType = findPropertyType(getReadMethod(), getWriteMethod());
      } catch (IntrospectionException e) {
      }
    }
    return propertyType;
  }

  public synchronized Method getReadMethod() {
    if (readMethod == null && beanClass != null && readMethodName != null) {
      readMethod = Introspector.findMethod(beanClass, readMethodName, 0);
      if (readMethod == null && !readMethodName.equals(Introspector.GET_PREFIX + getBaseName())) {
        readMethodName = Introspector.GET_PREFIX + getBaseName();
        readMethod = Introspector.findMethod(beanClass, readMethodName, 0);
      }
      if (readMethod != null) {
        try {
          setReadMethod(readMethod);
        } catch (IntrospectionException ex) {
          readMethod = null;
        }
      }
    }
    return readMethod;
  }

  public synchronized void setReadMethod(Method readMethod) throws IntrospectionException {
    this.readMethod = readMethod;
    if (readMethod == null) {
      readMethodName = null;
      return;
    }
    propertyType = findPropertyType(readMethod, writeMethod);
    setClass0(readMethod.getDeclaringClass());
    readMethodName = readMethod.getName();
  }

  public synchronized Method getWriteMethod() {
    if (writeMethod == null && beanClass != null && writeMethodName != null) {
      Class<?> type = propertyType;
      if (type == null && readMethod != null) {
        type = readMethod.getReturnType();
      }
      Class<?>[] args = type == null ? null : new Class<?>[] {type};
      writeMethod = Introspector.findMethod(beanClass, writeMethodName, 1, args);
      if (writeMethod != null && !writeMethod.getReturnType().equals(void.class)) {
        writeMethod = null;
      }
      if (writeMethod != null) {
        try {
          setWriteMethod(writeMethod);
        } catch (IntrospectionException ex) {
          writeMethod = null;
        }
      }
    }
    return writeMethod;
  }

  public synchronized void setWriteMethod(Method writeMethod) throws IntrospectionException {
    this.writeMethod = writeMethod;
    if (writeMethod == null) {
      writeMethodName = null;
      return;
    }
    propertyType = findPropertyType(readMethod, writeMethod);
    setClass0(writeMethod.getDeclaringClass());
    writeMethodName = writeMethod.getName();
  }

  void setTypes(Class<?> plain, Class<?> indexed) {
    propertyType = plain;
  }

  Class<?> getClass0() {
    return beanClass;
  }

  void setClass0(Class<?> clz) {
    if (beanClass != null && clz.isAssignableFrom(beanClass)) {
      return;
    }
    beanClass = clz;
  }

  public boolean isBound() {
    return bound;
  }

  public void setBound(boolean bound) {
    this.bound = bound;
  }

  public boolean isConstrained() {
    return constrained;
  }

  public void setConstrained(boolean constrained) {
    this.constrained = constrained;
  }

  public void setPropertyEditorClass(Class<?> propertyEditorClass) {
    this.propertyEditorClass = propertyEditorClass;
  }

  public Class<?> getPropertyEditorClass() {
    return propertyEditorClass;
  }

  private Class<?> findPropertyType(Method readMethod, Method writeMethod) throws IntrospectionException {
    Class<?> propertyType = null;
    try {
      if (readMethod != null) {
        Class<?>[] params = readMethod.getParameterTypes();
        if (params.length != 0) {
          throw new IntrospectionException("bad read method arg count: " + readMethod);
        }
        propertyType = readMethod.getReturnType();
        if (propertyType == Void.TYPE) {
          throw new IntrospectionException("read method " + readMethod.getName() + " returns void");
        }
      }
      if (writeMethod != null) {
        Class<?>[] params = writeMethod.getParameterTypes();
        if (params.length != 1) {
          throw new IntrospectionException("bad write method arg count: " + writeMethod);
        }
        if (propertyType != null && !params[0].isAssignableFrom(propertyType)) {
          throw new IntrospectionException("type mismatch between read and write methods");
        }
        propertyType = params[0];
      }
    } catch (IntrospectionException ex) {
      throw ex;
    }
    return propertyType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && obj instanceof PropertyDescriptor) {
      PropertyDescriptor other = (PropertyDescriptor) obj;
      Method otherReadMethod = other.getReadMethod();
      Method otherWriteMethod = other.getWriteMethod();
      if (!compareMethods(getReadMethod(), otherReadMethod)) {
        return false;
      }
      if (!compareMethods(getWriteMethod(), otherWriteMethod)) {
        return false;
      }
      if (getPropertyType() == other.getPropertyType() && getPropertyEditorClass() == other.getPropertyEditorClass()
          && bound == other.isBound() && constrained == other.isConstrained()
          && Objects.equals(writeMethodName, other.writeMethodName)
          && Objects.equals(readMethodName, other.readMethodName)) {
        return true;
      }
    }
    return false;
  }

  void copyFrom(PropertyDescriptor source) {
    setBound(source.isBound());
    setConstrained(source.isConstrained());
    setExpert(source.isExpert());
    setHidden(source.isHidden());
    setPreferred(source.isPreferred());
    setDisplayName(source.getDisplayName());
    setShortDescription(source.getShortDescription());
    setPropertyEditorClass(source.getPropertyEditorClass());
    copyValuesFrom(source);
    Method write = getWriteMethod();
    if (write != null) {
      for (Class<?> ex : write.getExceptionTypes()) {
        if (ex == PropertyVetoException.class) {
          setConstrained(true);
        }
      }
    }
  }

  boolean compareMethods(Method a, Method b) {
    if ((a == null) != (b == null)) {
      return false;
    }
    if (a != null && b != null) {
      return a.equals(b);
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 7;
    result = 37 * result + ((getPropertyType() == null) ? 0 : getPropertyType().hashCode());
    result = 37 * result + ((getReadMethod() == null) ? 0 : getReadMethod().hashCode());
    result = 37 * result + ((getWriteMethod() == null) ? 0 : getWriteMethod().hashCode());
    result = 37 * result + ((getPropertyEditorClass() == null) ? 0 : getPropertyEditorClass().hashCode());
    result = 37 * result + ((writeMethodName == null) ? 0 : writeMethodName.hashCode());
    result = 37 * result + ((readMethodName == null) ? 0 : readMethodName.hashCode());
    result = 37 * result + getName().hashCode();
    result = 37 * result + ((bound == false) ? 0 : 1);
    result = 37 * result + ((constrained == false) ? 0 : 1);
    return result;
  }

  @Override
  void appendTo(StringBuilder sb) {
    appendTo(sb, "bound", bound);
    appendTo(sb, "constrained", constrained);
    appendTo(sb, "propertyEditorClass", propertyEditorClass);
    appendTo(sb, "propertyType", getPropertyType());
    appendTo(sb, "readMethod", getReadMethod());
    appendTo(sb, "writeMethod", getWriteMethod());
  }
}
