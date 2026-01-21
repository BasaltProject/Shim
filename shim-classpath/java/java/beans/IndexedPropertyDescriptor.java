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

public class IndexedPropertyDescriptor extends PropertyDescriptor {
  private Class<?> indexedPropertyType;
  private Method indexedReadMethod;
  private Method indexedWriteMethod;
  private String indexedReadMethodName;
  private String indexedWriteMethodName;

  public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass) throws IntrospectionException {
    this(propertyName, beanClass, Introspector.GET_PREFIX + cap(propertyName),
        Introspector.SET_PREFIX + cap(propertyName), Introspector.GET_PREFIX + cap(propertyName),
        Introspector.SET_PREFIX + cap(propertyName));
  }

  private static String cap(String name) {
    if (name == null || name.length() == 0) {
      return name;
    }
    return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
  }

  public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass, String readMethodName,
      String writeMethodName, String indexedReadMethodName, String indexedWriteMethodName)
      throws IntrospectionException {
    super(propertyName, beanClass, readMethodName, writeMethodName);
    this.indexedReadMethodName = indexedReadMethodName;
    if (indexedReadMethodName != null && getIndexedReadMethod() == null) {
      throw new IntrospectionException("Method not found: " + indexedReadMethodName);
    }
    this.indexedWriteMethodName = indexedWriteMethodName;
    if (indexedWriteMethodName != null && getIndexedWriteMethod() == null) {
      throw new IntrospectionException("Method not found: " + indexedWriteMethodName);
    }
    indexedPropertyType = findIndexedPropertyType(getIndexedReadMethod(), getIndexedWriteMethod());
  }

  public IndexedPropertyDescriptor(String propertyName, Method readMethod, Method writeMethod,
      Method indexedReadMethod, Method indexedWriteMethod) throws IntrospectionException {
    super(propertyName, readMethod, writeMethod);
    setIndexedReadMethod0(indexedReadMethod);
    setIndexedWriteMethod0(indexedWriteMethod);
    setIndexedPropertyType(findIndexedPropertyType(indexedReadMethod, indexedWriteMethod));
  }

  IndexedPropertyDescriptor(Class<?> bean, String name, Method read, Method write, Method readIndexed,
      Method writeIndexed) {
    super(bean, name, read, write);
    this.indexedReadMethod = readIndexed;
    this.indexedWriteMethod = writeIndexed;
    this.indexedReadMethodName = readIndexed == null ? null : readIndexed.getName();
    this.indexedWriteMethodName = writeIndexed == null ? null : writeIndexed.getName();
    this.indexedPropertyType = readIndexed != null ? readIndexed.getReturnType()
        : writeIndexed != null ? writeIndexed.getParameterTypes()[1] : null;
  }

  IndexedPropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
    super(x, y);
    if (x instanceof IndexedPropertyDescriptor) {
      IndexedPropertyDescriptor ix = (IndexedPropertyDescriptor) x;
      indexedReadMethod = ix.indexedReadMethod;
      indexedWriteMethod = ix.indexedWriteMethod;
      indexedPropertyType = ix.indexedPropertyType;
      indexedReadMethodName = ix.indexedReadMethodName;
      indexedWriteMethodName = ix.indexedWriteMethodName;
    }
    if (y instanceof IndexedPropertyDescriptor) {
      IndexedPropertyDescriptor iy = (IndexedPropertyDescriptor) y;
      if (iy.indexedReadMethod != null) {
        indexedReadMethod = iy.indexedReadMethod;
        indexedReadMethodName = iy.indexedReadMethodName;
      }
      if (iy.indexedWriteMethod != null) {
        indexedWriteMethod = iy.indexedWriteMethod;
        indexedWriteMethodName = iy.indexedWriteMethodName;
      }
      if (iy.indexedPropertyType != null) {
        indexedPropertyType = iy.indexedPropertyType;
      }
    }
  }

  IndexedPropertyDescriptor(IndexedPropertyDescriptor old) {
    super(old);
    indexedReadMethod = old.indexedReadMethod;
    indexedWriteMethod = old.indexedWriteMethod;
    indexedPropertyType = old.indexedPropertyType;
    indexedReadMethodName = old.indexedReadMethodName;
    indexedWriteMethodName = old.indexedWriteMethodName;
  }

  @Override
  void setTypes(Class<?> plain, Class<?> indexed) {
    super.setTypes(plain, indexed);
    indexedPropertyType = indexed;
  }

  public synchronized Method getIndexedReadMethod() {
    if (indexedReadMethod == null) {
      Class<?> cls = beanClass0();
      if (cls == null || indexedReadMethodName == null) {
        return null;
      }
      indexedReadMethod = Introspector.findMethod(cls, indexedReadMethodName, 1, new Class<?>[] {int.class});
      if (indexedReadMethod != null && !indexedReadMethod.getReturnType().equals(indexedPropertyType)
          && indexedPropertyType != null) {
        indexedReadMethod = null;
      }
    }
    return indexedReadMethod;
  }

  private Class<?> beanClass0() {
    return getClass0();
  }

  public synchronized void setIndexedReadMethod(Method readMethod) throws IntrospectionException {
    setIndexedReadMethod0(readMethod);
    setIndexedPropertyType(findIndexedPropertyType(indexedReadMethod, indexedWriteMethod));
  }

  private void setIndexedReadMethod0(Method readMethod) throws IntrospectionException {
    indexedReadMethod = readMethod;
    if (readMethod == null) {
      indexedReadMethodName = null;
      return;
    }
    setClass0(readMethod.getDeclaringClass());
    indexedReadMethodName = readMethod.getName();
  }

  public synchronized Method getIndexedWriteMethod() {
    if (indexedWriteMethod == null) {
      Class<?> cls = beanClass0();
      if (cls == null || indexedWriteMethodName == null) {
        return null;
      }
      Class<?> type = indexedPropertyType;
      if (type == null) {
        try {
          type = findIndexedPropertyType(getIndexedReadMethod(), null);
        } catch (IntrospectionException ex) {
          return null;
        }
      }
      indexedWriteMethod = Introspector.findMethod(cls, indexedWriteMethodName, 2, new Class<?>[] {int.class, type});
      if (indexedWriteMethod != null && !indexedWriteMethod.getReturnType().equals(void.class)) {
        indexedWriteMethod = null;
      }
    }
    return indexedWriteMethod;
  }

  public synchronized void setIndexedWriteMethod(Method writeMethod) throws IntrospectionException {
    setIndexedWriteMethod0(writeMethod);
    setIndexedPropertyType(findIndexedPropertyType(getIndexedReadMethod(), indexedWriteMethod));
  }

  private void setIndexedWriteMethod0(Method writeMethod) throws IntrospectionException {
    indexedWriteMethod = writeMethod;
    if (writeMethod == null) {
      indexedWriteMethodName = null;
      return;
    }
    setClass0(writeMethod.getDeclaringClass());
    indexedWriteMethodName = writeMethod.getName();
  }

  public synchronized Class<?> getIndexedPropertyType() {
    return indexedPropertyType;
  }

  private void setIndexedPropertyType(Class<?> type) {
    indexedPropertyType = type;
  }

  private Class<?> findIndexedPropertyType(Method indexedReadMethod, Method indexedWriteMethod)
      throws IntrospectionException {
    Class<?> indexedPropertyType = null;
    if (indexedReadMethod != null) {
      Class<?>[] params = indexedReadMethod.getParameterTypes();
      if (params.length != 1) {
        throw new IntrospectionException("bad indexed read method arg count");
      }
      if (params[0] != Integer.TYPE) {
        throw new IntrospectionException("non int index to indexed read method");
      }
      indexedPropertyType = indexedReadMethod.getReturnType();
      if (indexedPropertyType == Void.TYPE) {
        throw new IntrospectionException("indexed read method returns void");
      }
    }
    if (indexedWriteMethod != null) {
      Class<?>[] params = indexedWriteMethod.getParameterTypes();
      if (params.length != 2) {
        throw new IntrospectionException("bad indexed write method arg count");
      }
      if (params[0] != Integer.TYPE) {
        throw new IntrospectionException("non int index to indexed write method");
      }
      if (indexedPropertyType != null && indexedPropertyType != params[1]) {
        throw new IntrospectionException("type mismatch between indexed read and indexed write methods: " + getName());
      }
      indexedPropertyType = params[1];
    }
    Class<?> propertyType = getPropertyType();
    if (propertyType != null && (!propertyType.isArray() || propertyType.getComponentType() != indexedPropertyType)) {
      throw new IntrospectionException("type mismatch between indexed and non-indexed methods: " + getName());
    }
    return indexedPropertyType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && obj instanceof IndexedPropertyDescriptor) {
      IndexedPropertyDescriptor other = (IndexedPropertyDescriptor) obj;
      if (!compareMethods(getIndexedReadMethod(), other.getIndexedReadMethod())) {
        return false;
      }
      if (!compareMethods(getIndexedWriteMethod(), other.getIndexedWriteMethod())) {
        return false;
      }
      if (getIndexedPropertyType() != other.getIndexedPropertyType()) {
        return false;
      }
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 37 * result + ((indexedWriteMethodName == null) ? 0 : indexedWriteMethodName.hashCode());
    result = 37 * result + ((indexedReadMethodName == null) ? 0 : indexedReadMethodName.hashCode());
    result = 37 * result + ((getIndexedPropertyType() == null) ? 0 : getIndexedPropertyType().hashCode());
    return result;
  }

  @Override
  void appendTo(StringBuilder sb) {
    super.appendTo(sb);
    appendTo(sb, "indexedPropertyType", getIndexedPropertyType());
    appendTo(sb, "indexedReadMethod", getIndexedReadMethod());
    appendTo(sb, "indexedWriteMethod", getIndexedWriteMethod());
  }
}
