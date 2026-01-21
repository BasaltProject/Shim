/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventSetDescriptor extends FeatureDescriptor {
  private MethodDescriptor[] listenerMethodDescriptors;
  private Method[] listenerMethods;
  private Method addMethod;
  private Method removeMethod;
  private Method getMethod;
  private Class<?> listenerType;
  private boolean unicast;
  private boolean inDefaultEventSet = true;

  public EventSetDescriptor(Class<?> sourceClass, String eventSetName, Class<?> listenerType,
      String listenerMethodName) throws IntrospectionException {
    this(sourceClass, eventSetName, listenerType, new String[] {listenerMethodName},
        Introspector.ADD_PREFIX + getListenerClassName(listenerType),
        Introspector.REMOVE_PREFIX + getListenerClassName(listenerType),
        Introspector.GET_PREFIX + getListenerClassName(listenerType) + "s");
    listenerMethodDescriptors = new MethodDescriptor[] {new MethodDescriptor(listenerMethods[0])};
  }

  public EventSetDescriptor(Class<?> sourceClass, String eventSetName, Class<?> listenerType,
      String[] listenerMethodNames, String addListenerMethodName, String removeListenerMethodName)
      throws IntrospectionException {
    this(sourceClass, eventSetName, listenerType, listenerMethodNames, addListenerMethodName,
        removeListenerMethodName, null);
  }

  public EventSetDescriptor(Class<?> sourceClass, String eventSetName, Class<?> listenerType,
      String[] listenerMethodNames, String addListenerMethodName, String removeListenerMethodName,
      String getListenerMethodName) throws IntrospectionException {
    if (sourceClass == null || eventSetName == null || listenerType == null) {
      throw new NullPointerException();
    }
    setName(eventSetName);
    this.listenerType = listenerType;
    List<Method> methods = new ArrayList<Method>();
    if (listenerMethodNames != null) {
      for (String listenerMethodName : listenerMethodNames) {
        if (listenerMethodName == null) {
          throw new NullPointerException();
        }
        Method m = Introspector.findMethod(listenerType, listenerMethodName, 1);
        if (m == null) {
          throw new IntrospectionException("Method not found: " + listenerMethodName + " on class "
              + listenerType.getName());
        }
        methods.add(m);
      }
    }
    listenerMethods = methods.toArray(new Method[0]);
    if (addListenerMethodName != null) {
      addMethod = Introspector.findMethod(sourceClass, addListenerMethodName, 1, new Class<?>[] {listenerType});
      if (addMethod == null) {
        throw new IntrospectionException("Method not found: " + addListenerMethodName + " on class "
            + sourceClass.getName());
      }
    }
    if (removeListenerMethodName != null) {
      removeMethod = Introspector.findMethod(sourceClass, removeListenerMethodName, 1,
          new Class<?>[] {listenerType});
      if (removeMethod == null) {
        throw new IntrospectionException("Method not found: " + removeListenerMethodName + " on class "
            + sourceClass.getName());
      }
    }
    if (getListenerMethodName != null) {
      getMethod = Introspector.findMethod(sourceClass, getListenerMethodName, 0);
    }
    if (addMethod != null) {
      for (Class<?> ex : addMethod.getExceptionTypes()) {
        if (ex == java.util.TooManyListenersException.class) {
          unicast = true;
        }
      }
    }
  }

  public EventSetDescriptor(String eventSetName, Class<?> listenerType, Method[] listenerMethods,
      Method addListenerMethod, Method removeListenerMethod) throws IntrospectionException {
    this(eventSetName, listenerType, listenerMethods, addListenerMethod, removeListenerMethod, null);
  }

  public EventSetDescriptor(String eventSetName, Class<?> listenerType, Method[] listenerMethods,
      Method addListenerMethod, Method removeListenerMethod, Method getListenerMethod)
      throws IntrospectionException {
    setName(eventSetName);
    this.listenerType = listenerType;
    this.listenerMethods = listenerMethods == null ? null : listenerMethods.clone();
    addMethod = addListenerMethod;
    removeMethod = removeListenerMethod;
    getMethod = getListenerMethod;
    if (addMethod != null) {
      for (Class<?> ex : addMethod.getExceptionTypes()) {
        if (ex == java.util.TooManyListenersException.class) {
          unicast = true;
        }
      }
    }
  }

  public EventSetDescriptor(String eventSetName, Class<?> listenerType,
      MethodDescriptor[] listenerMethodDescriptors, Method addListenerMethod, Method removeListenerMethod)
      throws IntrospectionException {
    setName(eventSetName);
    this.listenerType = listenerType;
    this.listenerMethodDescriptors = listenerMethodDescriptors == null ? null : listenerMethodDescriptors.clone();
    if (listenerMethodDescriptors != null) {
      listenerMethods = new Method[listenerMethodDescriptors.length];
      for (int i = 0; i < listenerMethods.length; i++) {
        listenerMethods[i] = listenerMethodDescriptors[i].getMethod();
      }
    }
    addMethod = addListenerMethod;
    removeMethod = removeListenerMethod;
    if (addMethod != null) {
      for (Class<?> ex : addMethod.getExceptionTypes()) {
        if (ex == java.util.TooManyListenersException.class) {
          unicast = true;
        }
      }
    }
  }

  EventSetDescriptor(EventSetDescriptor old) {
    super(old);
    listenerMethodDescriptors = old.listenerMethodDescriptors;
    listenerMethods = old.listenerMethods;
    addMethod = old.addMethod;
    removeMethod = old.removeMethod;
    getMethod = old.getMethod;
    listenerType = old.listenerType;
    unicast = old.unicast;
    inDefaultEventSet = old.inDefaultEventSet;
  }

  public Class<?> getListenerType() {
    return listenerType;
  }

  public synchronized Method[] getListenerMethods() {
    return listenerMethods == null ? new Method[0] : listenerMethods.clone();
  }

  public synchronized MethodDescriptor[] getListenerMethodDescriptors() {
    if (listenerMethodDescriptors == null && listenerMethods != null) {
      listenerMethodDescriptors = new MethodDescriptor[listenerMethods.length];
      for (int i = 0; i < listenerMethods.length; i++) {
        listenerMethodDescriptors[i] = new MethodDescriptor(listenerMethods[i]);
      }
    }
    return listenerMethodDescriptors == null ? null : listenerMethodDescriptors.clone();
  }

  public synchronized Method getAddListenerMethod() {
    return addMethod;
  }

  public synchronized Method getRemoveListenerMethod() {
    return removeMethod;
  }

  public synchronized Method getGetListenerMethod() {
    return getMethod;
  }

  public void setUnicast(boolean unicast) {
    this.unicast = unicast;
  }

  public boolean isUnicast() {
    return unicast;
  }

  public void setInDefaultEventSet(boolean inDefaultEventSet) {
    this.inDefaultEventSet = inDefaultEventSet;
  }

  public boolean isInDefaultEventSet() {
    return inDefaultEventSet;
  }

  private static String getListenerClassName(Class<?> cls) {
    String name = cls.getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  @Override
  void appendTo(StringBuilder sb) {
    appendTo(sb, "unicast", unicast);
    appendTo(sb, "inDefaultEventSet", inDefaultEventSet);
    appendTo(sb, "listenerType", listenerType);
    appendTo(sb, "getListenerMethod", getMethod);
    appendTo(sb, "addListenerMethod", addMethod);
    appendTo(sb, "removeListenerMethod", removeMethod);
  }
}
