/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class Encoder {
  private final IdentityHashMap<Object, Object> bindings = new IdentityHashMap<Object, Object>();
  private ExceptionListener exceptionListener;
  private final Map<Class<?>, PersistenceDelegate> delegates = new HashMap<Class<?>, PersistenceDelegate>();

  public Encoder() { }

  public void setExceptionListener(ExceptionListener exceptionListener) {
    this.exceptionListener = exceptionListener;
  }

  public ExceptionListener getExceptionListener() {
    return (exceptionListener != null) ? exceptionListener : Statement.defaultExceptionListener;
  }

  public PersistenceDelegate getPersistenceDelegate(Class<?> type) {
    PersistenceDelegate d = delegates.get(type);
    if (d != null) {
      return d;
    }
    return new DefaultPersistenceDelegate();
  }

  public void setPersistenceDelegate(Class<?> type, PersistenceDelegate delegate) {
    if (delegate == null) {
      delegates.remove(type);
    } else {
      delegates.put(type, delegate);
    }
  }

  public Object remove(Object oldInstance) { return bindings.remove(oldInstance); }

  public Object get(Object oldInstance) {
    if (oldInstance == null || oldInstance == this || oldInstance.getClass() == String.class) {
      return oldInstance;
    }
    return bindings.get(oldInstance);
  }

  protected void writeObject(Object o) {
    if (o == this) {
      return;
    }
    PersistenceDelegate info = getPersistenceDelegate(o == null ? null : o.getClass());
    info.writeObject(o, this);
  }

  public void writeStatement(Statement oldStm) {
    try {
      oldStm.execute();
    } catch (Exception e) {
      getExceptionListener().exceptionThrown(new Exception("Encoder: discarding statement " + oldStm, e));
    }
  }

  public void writeExpression(Expression oldExp) {
    try {
      Object oldValue = oldExp.getValue();
      if (get(oldValue) != null) {
        return;
      }
      bindings.put(oldValue, oldExp.getValue());
      writeObject(oldValue);
    } catch (Exception e) {
      getExceptionListener().exceptionThrown(new Exception("Encoder: discarding statement " + oldExp, e));
    }
  }
}
