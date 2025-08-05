/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class VarHandle {
  public enum AccessMode {
    GET("get", AccessType.GET),
    SET("set", AccessType.SET),
    GET_VOLATILE("getVolatile", AccessType.GET),
    SET_VOLATILE("setVolatile", AccessType.SET),
    GET_ACQUIRE("getAcquire", AccessType.GET),
    SET_RELEASE("setRelease", AccessType.SET),
    GET_OPAQUE("getOpaque", AccessType.GET),
    SET_OPAQUE("setOpaque", AccessType.SET),
    COMPARE_AND_SET("compareAndSet", AccessType.COMPARE_AND_SWAP),
    COMPARE_AND_EXCHANGE("compareAndExchange", AccessType.COMPARE_AND_EXCHANGE),
    COMPARE_AND_EXCHANGE_ACQUIRE("compareAndExchangeAcquire", AccessType.COMPARE_AND_EXCHANGE),
    COMPARE_AND_EXCHANGE_RELEASE("compareAndExchangeRelease", AccessType.COMPARE_AND_EXCHANGE),
    WEAK_COMPARE_AND_SET_PLAIN("weakCompareAndSetPlain", AccessType.COMPARE_AND_SWAP),
    WEAK_COMPARE_AND_SET("weakCompareAndSet", AccessType.COMPARE_AND_SWAP),
    WEAK_COMPARE_AND_SET_ACQUIRE("weakCompareAndSetAcquire", AccessType.COMPARE_AND_SWAP),
    WEAK_COMPARE_AND_SET_RELEASE("weakCompareAndSetRelease", AccessType.COMPARE_AND_SWAP),
    GET_AND_SET("getAndSet", AccessType.GET_AND_UPDATE),
    GET_AND_SET_ACQUIRE("getAndSetAcquire", AccessType.GET_AND_UPDATE),
    GET_AND_SET_RELEASE("getAndSetRelease", AccessType.GET_AND_UPDATE),
    GET_AND_ADD("getAndAdd", AccessType.GET_AND_UPDATE),
    GET_AND_ADD_ACQUIRE("getAndAddAcquire", AccessType.GET_AND_UPDATE),
    GET_AND_ADD_RELEASE("getAndAddRelease", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_OR("getAndBitwiseOr", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_OR_ACQUIRE("getAndBitwiseOrAcquire", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_OR_RELEASE("getAndBitwiseOrRelease", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_AND("getAndBitwiseAnd", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_AND_ACQUIRE("getAndBitwiseAndAcquire", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_AND_RELEASE("getAndBitwiseAndRelease", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_XOR("getAndBitwiseXor", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_XOR_ACQUIRE("getAndBitwiseXorAcquire", AccessType.GET_AND_UPDATE),
    GET_AND_BITWISE_XOR_RELEASE("getAndBitwiseXorRelease", AccessType.GET_AND_UPDATE);

    private final String methodName;
    private final AccessType atype;

    AccessMode(String methodName, AccessType atype) {
      this.methodName = methodName;
      this.atype = atype;
    }

    public String methodName() {
      return methodName;
    }

    public static AccessMode valueFromMethodName(String methodName) {
      for (AccessMode am : values()) {
        if (am.methodName.equals(methodName)) {
          return am;
        }
      }
      throw new IllegalArgumentException("No AccessMode with method name: " + methodName);
    }
  }

  public enum AccessType {
    GET, SET, COMPARE_AND_SWAP, COMPARE_AND_EXCHANGE, GET_AND_UPDATE
  }

  private final Class<?> varType;
  private final List<Class<?>> coordinateTypes;

  protected VarHandle(Class<?> varType, List<Class<?>> coordinateTypes) {
    this.varType = Objects.requireNonNull(varType);
    this.coordinateTypes = coordinateTypes != null
        ? Collections.unmodifiableList(coordinateTypes)
        : Collections.<Class<?>>emptyList();
  }

  public Class<?> varType() {
    return varType;
  }

  public List<Class<?>> coordinateTypes() {
    return coordinateTypes;
  }

  public abstract Object get(Object... args);
  public abstract void set(Object... args);

  public Object getVolatile(Object... args) {
    return get(args);
  }

  public void setVolatile(Object... args) {
    set(args);
  }

  public Object getOpaque(Object... args) {
    return get(args);
  }

  public void setOpaque(Object... args) {
    set(args);
  }

  public Object getAcquire(Object... args) {
    return get(args);
  }

  public void setRelease(Object... args) {
    set(args);
  }

  public abstract boolean compareAndSet(Object... args);

  public abstract Object getAndSet(Object... args);

  public abstract Object getAndAdd(Object... args);

  public Object compareAndExchange(Object... args) {
    Object old = get(Arrays.copyOf(args, coordinateTypes.size()));
    compareAndSet(args);
    return old;
  }

  public Object compareAndExchangeAcquire(Object... args) {
    return compareAndExchange(args);
  }

  public Object compareAndExchangeRelease(Object... args) {
    return compareAndExchange(args);
  }

  public boolean weakCompareAndSetPlain(Object... args) {
    return compareAndSet(args);
  }

  public boolean weakCompareAndSet(Object... args) {
    return compareAndSet(args);
  }

  public boolean weakCompareAndSetAcquire(Object... args) {
    return compareAndSet(args);
  }

  public boolean weakCompareAndSetRelease(Object... args) {
    return compareAndSet(args);
  }

  public Object getAndSetAcquire(Object... args) {
    return getAndSet(args);
  }

  public Object getAndSetRelease(Object... args) {
    return getAndSet(args);
  }

  public Object getAndAddAcquire(Object... args) {
    return getAndAdd(args);
  }

  public Object getAndAddRelease(Object... args) {
    return getAndAdd(args);
  }

  public MethodType accessModeType(AccessMode accessMode) {
    switch (accessMode.atype) {
    case GET:
      return MethodType.methodType(varType, coordinateTypes);
    case SET: {
      List<Class<?>> params = new ArrayList<Class<?>>(coordinateTypes);
      params.add(varType);
      return MethodType.methodType(void.class, params);
    }
    case COMPARE_AND_SWAP: {
      List<Class<?>> params = new ArrayList<Class<?>>(coordinateTypes);
      params.add(varType);
      params.add(varType);
      return MethodType.methodType(boolean.class, params);
    }
    case COMPARE_AND_EXCHANGE: {
      List<Class<?>> params = new ArrayList<Class<?>>(coordinateTypes);
      params.add(varType);
      params.add(varType);
      return MethodType.methodType(varType, params);
    }
    case GET_AND_UPDATE: {
      List<Class<?>> params = new ArrayList<Class<?>>(coordinateTypes);
      params.add(varType);
      return MethodType.methodType(varType, params);
    }
    default:
      throw new InternalError();
    }
  }

  public MethodHandle toMethodHandle(AccessMode accessMode) {
    return new VarHandleMethodHandle(this, accessMode);
  }

  public static VarHandle makeFieldHandle(Field field, boolean isStatic) {
    return isStatic ? new StaticFieldVarHandle(field) : new InstanceFieldVarHandle(field);
  }

  public static VarHandle makeArrayHandle(Class<?> arrayClass) {
    return new ArrayVarHandle(arrayClass);
  }

  private static class InstanceFieldVarHandle extends VarHandle {
    private final Field field;

    InstanceFieldVarHandle(Field field) {
      super(field.getType(), Collections.<Class<?>>singletonList(field.getDeclaringClass()));
      this.field = field;
    }

    @Override
    public Object get(Object... args) {
      try {
        return field.get(args[0]);
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public void set(Object... args) {
      try {
        field.set(args[0], args[1]);
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public synchronized boolean compareAndSet(Object... args) {
      Object recv = args[0];
      Object expected = args[1];
      Object update = args[2];
      try {
        Object current = field.get(recv);
        if (Objects.equals(current, expected)) {
          field.set(recv, update);
          return true;
        }
        return false;
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public synchronized Object getAndSet(Object... args) {
      Object recv = args[0];
      Object update = args[1];
      try {
        Object current = field.get(recv);
        field.set(recv, update);
        return current;
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public synchronized Object getAndAdd(Object... args) {
      Object recv = args[0];
      Object delta = args[1];
      try {
        Object current = field.get(recv);
        if (current instanceof Integer) {
          int cur = ((Integer) current).intValue();
          int d = ((Number) delta).intValue();
          field.set(recv, Integer.valueOf(cur + d));
          return Integer.valueOf(cur);
        } else if (current instanceof Long) {
          long cur = ((Long) current).longValue();
          long d = ((Number) delta).longValue();
          field.set(recv, Long.valueOf(cur + d));
          return Long.valueOf(cur);
        }
        throw new UnsupportedOperationException("getAndAdd not supported on " + varType());
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public String toString() {
      return "VarHandle[varType=" + varType().getName() + ", coord=[" + field.getDeclaringClass().getName() + "]]";
    }
  }

  private static class StaticFieldVarHandle extends VarHandle {
    private final Field field;

    StaticFieldVarHandle(Field field) {
      super(field.getType(), Collections.<Class<?>>emptyList());
      this.field = field;
    }

    @Override
    public Object get(Object... args) {
      try {
        return field.get(null);
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public void set(Object... args) {
      try {
        field.set(null, args[0]);
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public synchronized boolean compareAndSet(Object... args) {
      Object expected = args[0];
      Object update = args[1];
      try {
        Object current = field.get(null);
        if (Objects.equals(current, expected)) {
          field.set(null, update);
          return true;
        }
        return false;
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public synchronized Object getAndSet(Object... args) {
      Object update = args[0];
      try {
        Object current = field.get(null);
        field.set(null, update);
        return current;
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public synchronized Object getAndAdd(Object... args) {
      Object delta = args[0];
      try {
        Object current = field.get(null);
        if (current instanceof Integer) {
          int cur = ((Integer) current).intValue();
          int d = ((Number) delta).intValue();
          field.set(null, Integer.valueOf(cur + d));
          return Integer.valueOf(cur);
        } else if (current instanceof Long) {
          long cur = ((Long) current).longValue();
          long d = ((Number) delta).longValue();
          field.set(null, Long.valueOf(cur + d));
          return Long.valueOf(cur);
        }
        throw new UnsupportedOperationException("getAndAdd not supported on " + varType());
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    @Override
    public String toString() {
      return "VarHandle[varType=" + varType().getName() + ", coord=[]]";
    }
  }

  private static class ArrayVarHandle extends VarHandle {
    ArrayVarHandle(Class<?> arrayClass) {
      super(arrayClass.getComponentType(), Arrays.<Class<?>>asList(arrayClass, int.class));
    }

    @Override
    public Object get(Object... args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      return Array.get(array, index);
    }

    @Override
    public void set(Object... args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      Object val = args[2];
      Array.set(array, index, val);
    }

    @Override
    public synchronized boolean compareAndSet(Object... args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      Object expected = args[2];
      Object update = args[3];
      Object current = Array.get(array, index);
      if (Objects.equals(current, expected)) {
        Array.set(array, index, update);
        return true;
      }
      return false;
    }

    @Override
    public synchronized Object getAndSet(Object... args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      Object update = args[2];
      Object current = Array.get(array, index);
      Array.set(array, index, update);
      return current;
    }

    @Override
    public synchronized Object getAndAdd(Object... args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      Object delta = args[2];
      Object current = Array.get(array, index);
      if (current instanceof Integer) {
        int cur = ((Integer) current).intValue();
        int d = ((Number) delta).intValue();
        Array.set(array, index, Integer.valueOf(cur + d));
        return Integer.valueOf(cur);
      } else if (current instanceof Long) {
        long cur = ((Long) current).longValue();
        long d = ((Number) delta).longValue();
        Array.set(array, index, Long.valueOf(cur + d));
        return Long.valueOf(cur);
      }
      throw new UnsupportedOperationException("getAndAdd not supported on " + varType());
    }

    @Override
    public String toString() {
      return "VarHandle[varType=" + varType().getName() + ", coord=[" + coordinateTypes().get(0).getName() + ", int]]";
    }
  }

  private static class VarHandleMethodHandle extends MethodHandle {
    private final VarHandle handle;
    private final AccessMode mode;

    VarHandleMethodHandle(VarHandle handle, AccessMode mode) {
      super(handle.accessModeType(mode));
      this.handle = handle;
      this.mode = mode;
    }

    @Override
    public Object invokeWithArguments(Object... arguments) throws Throwable {
      switch (mode.atype) {
      case GET:
        return handle.get(arguments);
      case SET:
        handle.set(arguments);
        return null;
      case COMPARE_AND_SWAP:
        return Boolean.valueOf(handle.compareAndSet(arguments));
      case COMPARE_AND_EXCHANGE:
        return handle.compareAndExchange(arguments);
      case GET_AND_UPDATE:
        if (mode.methodName().startsWith("getAndSet")) {
          return handle.getAndSet(arguments);
        } else if (mode.methodName().startsWith("getAndAdd")) {
          return handle.getAndAdd(arguments);
        }
        return handle.getAndSet(arguments);
      default:
        throw new UnsupportedOperationException();
      }
    }
  }
}
