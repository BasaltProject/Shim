/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.lang.reflect.Method;

public class DefaultPersistenceDelegate extends PersistenceDelegate {
  private final String[] constructor;

  public DefaultPersistenceDelegate() { this.constructor = new String[0]; }

  public DefaultPersistenceDelegate(String[] constructorPropertyNames) {
    this.constructor = constructorPropertyNames == null ? new String[0] : constructorPropertyNames.clone();
  }

  protected boolean mutatesTo(Object oldInstance, Object newInstance) {
    return (constructor.length == 0) || !definesEquals(oldInstance) ? super.mutatesTo(oldInstance, newInstance)
        : oldInstance.equals(newInstance);
  }

  private static boolean definesEquals(Object instance) {
    try {
      return instance.getClass().getMethod("equals", Object.class).getDeclaringClass() != Object.class;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  protected Expression instantiate(Object oldInstance, Encoder out) {
    int nArgs = constructor.length;
    Class<?> type = oldInstance.getClass();
    Object[] constructorArgs = new Object[nArgs];
    for (int i = 0; i < nArgs; i++) {
      try {
        String name = constructor[i];
        String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Method getter;
        try {
          getter = type.getMethod("get" + cap);
        } catch (NoSuchMethodException e) {
          getter = type.getMethod("is" + cap);
        }
        constructorArgs[i] = getter.invoke(oldInstance);
      } catch (Exception e) {
        out.getExceptionListener().exceptionThrown(e);
      }
    }
    return new Expression(oldInstance, oldInstance.getClass(), "new", constructorArgs);
  }

  protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
    super.initialize(type, oldInstance, newInstance, out);
  }
}
