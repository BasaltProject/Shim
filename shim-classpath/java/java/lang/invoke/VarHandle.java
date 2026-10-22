/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class VarHandle implements java.lang.constant.Constable {
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

  private static final Object[] STRIPES = new Object[64];

  static {
    for (int i = 0; i < STRIPES.length; i++) STRIPES[i] = new Object();
  }

  static Object stripe(Object base, int slot) {
    int h = System.identityHashCode(base) * 31 + slot;
    return STRIPES[(h ^ (h >>> 16)) & (STRIPES.length - 1)];
  }

  Object lockFor(Object[] args) {
    return this;
  }

  static boolean sameValue(Class<?> type, Object a, Object b) {
    if (a == b) return true;
    if (a == null || b == null) return false;
    return type.isPrimitive() && a.equals(b);
  }

  public abstract Object getAndSet(Object... args);

  public abstract Object getAndAdd(Object... args);

  public Object compareAndExchange(Object... args) {
    synchronized (lockFor(args)) {
      Object old = get(Arrays.copyOf(args, coordinateTypes.size()));
      compareAndSet(args);
      return old;
    }
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

  private static final int OP_OR = 0;
  private static final int OP_AND = 1;
  private static final int OP_XOR = 2;

  private Object bitwise(int op, String name, Object[] args) {
    Class<?> t = varType;
    if (t != int.class && t != long.class && t != short.class && t != byte.class && t != char.class
        && t != boolean.class) {
      throw new UnsupportedOperationException(name);
    }
    int n = coordinateTypes.size();
    Object[] coords = Arrays.copyOf(args, n);
    Object value = args[n];
    while (true) {
      Object old = get(coords);
      Object next = combine(op, t, old, value);
      Object[] cas = Arrays.copyOf(coords, n + 2);
      cas[n] = old;
      cas[n + 1] = next;
      if (compareAndSet(cas)) {
        return old;
      }
    }
  }

  private static Object combine(int op, Class<?> t, Object a, Object b) {
    if (t == boolean.class) {
      boolean x = (Boolean) a;
      boolean y = (Boolean) b;
      return op == OP_OR ? (x | y) : op == OP_AND ? (x & y) : (x ^ y);
    }
    long x = a instanceof Character ? (Character) a : ((Number) a).longValue();
    long y = b instanceof Character ? (Character) b : ((Number) b).longValue();
    long r = op == OP_OR ? (x | y) : op == OP_AND ? (x & y) : (x ^ y);
    if (t == int.class) {
      return (int) r;
    } else if (t == long.class) {
      return r;
    } else if (t == short.class) {
      return (short) r;
    } else if (t == byte.class) {
      return (byte) r;
    }
    return (char) r;
  }

  public Object getAndBitwiseOr(Object... args) {
    return bitwise(OP_OR, "getAndBitwiseOr", args);
  }

  public Object getAndBitwiseOrAcquire(Object... args) {
    return bitwise(OP_OR, "getAndBitwiseOrAcquire", args);
  }

  public Object getAndBitwiseOrRelease(Object... args) {
    return bitwise(OP_OR, "getAndBitwiseOrRelease", args);
  }

  public Object getAndBitwiseAnd(Object... args) {
    return bitwise(OP_AND, "getAndBitwiseAnd", args);
  }

  public Object getAndBitwiseAndAcquire(Object... args) {
    return bitwise(OP_AND, "getAndBitwiseAndAcquire", args);
  }

  public Object getAndBitwiseAndRelease(Object... args) {
    return bitwise(OP_AND, "getAndBitwiseAndRelease", args);
  }

  public Object getAndBitwiseXor(Object... args) {
    return bitwise(OP_XOR, "getAndBitwiseXor", args);
  }

  public Object getAndBitwiseXorAcquire(Object... args) {
    return bitwise(OP_XOR, "getAndBitwiseXorAcquire", args);
  }

  public Object getAndBitwiseXorRelease(Object... args) {
    return bitwise(OP_XOR, "getAndBitwiseXorRelease", args);
  }

  private static final Object FENCE = new Object();

  public static void fullFence() {
    synchronized (FENCE) {
    }
  }

  public static void acquireFence() {
    fullFence();
  }

  public static void releaseFence() {
    fullFence();
  }

  public static void loadLoadFence() {
    fullFence();
  }

  public static void storeStoreFence() {
    fullFence();
  }

  public boolean hasInvokeExactBehavior() {
    return false;
  }

  public VarHandle withInvokeExactBehavior() {
    return new ExactView(this);
  }

  public VarHandle withInvokeBehavior() {
    return this;
  }

  private static final class ExactView extends VarHandle {
    private final VarHandle target;

    ExactView(VarHandle target) {
      super(target.varType(), target.coordinateTypes());
      this.target = target;
    }

    @Override
    public boolean hasInvokeExactBehavior() {
      return true;
    }

    @Override
    public VarHandle withInvokeExactBehavior() {
      return this;
    }

    @Override
    public VarHandle withInvokeBehavior() {
      return target;
    }

    @Override
    public Object get(Object... args) {
      return target.get(args);
    }

    @Override
    public void set(Object... args) {
      target.set(args);
    }

    @Override
    public Object getVolatile(Object... args) {
      return target.getVolatile(args);
    }

    @Override
    public void setVolatile(Object... args) {
      target.setVolatile(args);
    }

    @Override
    public Object getOpaque(Object... args) {
      return target.getOpaque(args);
    }

    @Override
    public void setOpaque(Object... args) {
      target.setOpaque(args);
    }

    @Override
    public Object getAcquire(Object... args) {
      return target.getAcquire(args);
    }

    @Override
    public void setRelease(Object... args) {
      target.setRelease(args);
    }

    @Override
    public boolean compareAndSet(Object... args) {
      return target.compareAndSet(args);
    }

    @Override
    public Object getAndSet(Object... args) {
      return target.getAndSet(args);
    }

    @Override
    public Object getAndAdd(Object... args) {
      return target.getAndAdd(args);
    }

    @Override
    public Object compareAndExchange(Object... args) {
      return target.compareAndExchange(args);
    }

    @Override
    public Object compareAndExchangeAcquire(Object... args) {
      return target.compareAndExchangeAcquire(args);
    }

    @Override
    public Object compareAndExchangeRelease(Object... args) {
      return target.compareAndExchangeRelease(args);
    }

    @Override
    public boolean weakCompareAndSetPlain(Object... args) {
      return target.weakCompareAndSetPlain(args);
    }

    @Override
    public boolean weakCompareAndSet(Object... args) {
      return target.weakCompareAndSet(args);
    }

    @Override
    public boolean weakCompareAndSetAcquire(Object... args) {
      return target.weakCompareAndSetAcquire(args);
    }

    @Override
    public boolean weakCompareAndSetRelease(Object... args) {
      return target.weakCompareAndSetRelease(args);
    }

    @Override
    public Object getAndSetAcquire(Object... args) {
      return target.getAndSetAcquire(args);
    }

    @Override
    public Object getAndSetRelease(Object... args) {
      return target.getAndSetRelease(args);
    }

    @Override
    public Object getAndAddAcquire(Object... args) {
      return target.getAndAddAcquire(args);
    }

    @Override
    public Object getAndAddRelease(Object... args) {
      return target.getAndAddRelease(args);
    }

    @Override
    public Object getAndBitwiseOr(Object... args) {
      return target.getAndBitwiseOr(args);
    }

    @Override
    public Object getAndBitwiseOrAcquire(Object... args) {
      return target.getAndBitwiseOrAcquire(args);
    }

    @Override
    public Object getAndBitwiseOrRelease(Object... args) {
      return target.getAndBitwiseOrRelease(args);
    }

    @Override
    public Object getAndBitwiseAnd(Object... args) {
      return target.getAndBitwiseAnd(args);
    }

    @Override
    public Object getAndBitwiseAndAcquire(Object... args) {
      return target.getAndBitwiseAndAcquire(args);
    }

    @Override
    public Object getAndBitwiseAndRelease(Object... args) {
      return target.getAndBitwiseAndRelease(args);
    }

    @Override
    public Object getAndBitwiseXor(Object... args) {
      return target.getAndBitwiseXor(args);
    }

    @Override
    public Object getAndBitwiseXorAcquire(Object... args) {
      return target.getAndBitwiseXorAcquire(args);
    }

    @Override
    public Object getAndBitwiseXorRelease(Object... args) {
      return target.getAndBitwiseXorRelease(args);
    }

    @Override
    public boolean isAccessModeSupported(AccessMode accessMode) {
      return target.isAccessModeSupported(accessMode);
    }

    @Override
    public String toString() {
      return target.toString();
    }
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

  public boolean isAccessModeSupported(AccessMode accessMode) {
    Objects.requireNonNull(accessMode);
    String n = accessMode.methodName();
    boolean integral = varType == int.class || varType == long.class || varType == short.class
        || varType == byte.class || varType == char.class;
    if (n.startsWith("getAndAdd")) {
      return integral || varType == float.class || varType == double.class;
    }
    if (n.startsWith("getAndBitwise")) {
      return integral || varType == boolean.class;
    }
    return true;
  }

  public Optional<VarHandleDesc> describeConstable() {
    return Optional.empty();
  }

  public static VarHandle makeFieldHandle(Field field, boolean isStatic) {
    return isStatic ? new StaticFieldVarHandle(field) : new InstanceFieldVarHandle(field);
  }

  public static VarHandle makeArrayHandle(Class<?> arrayClass) {
    return new ArrayVarHandle(arrayClass);
  }

  public static VarHandle makeByteArrayViewHandle(Class<?> viewArrayClass, ByteOrder byteOrder) {
    return new ByteArrayViewVarHandle(viewArrayClass, byteOrder);
  }

  public static VarHandle makeByteBufferViewHandle(Class<?> viewArrayClass, ByteOrder byteOrder) {
    return new ByteBufferViewVarHandle(viewArrayClass, byteOrder);
  }

  public static VarHandle makeFilterValue(VarHandle target, MethodHandle filterToTarget,
      MethodHandle filterFromTarget) {
    return new FilterValueVarHandle(target, filterToTarget, filterFromTarget);
  }

  public static VarHandle makeFilterCoordinates(VarHandle target, int pos, MethodHandle... filters) {
    return new FilterCoordinatesVarHandle(target, pos, filters);
  }

  public static VarHandle makeInsertCoordinates(VarHandle target, int pos, Object... values) {
    return new InsertCoordinatesVarHandle(target, pos, values);
  }

  public static VarHandle makeDropCoordinates(VarHandle target, int pos, List<Class<?>> valueTypes) {
    return new DropCoordinatesVarHandle(target, pos, valueTypes);
  }

  public static VarHandle makePermuteCoordinates(VarHandle target, List<Class<?>> newCoordinates, int... reorder) {
    return new PermuteCoordinatesVarHandle(target, newCoordinates, reorder);
  }

  public static VarHandle makeCollectCoordinates(VarHandle target, int pos, MethodHandle filter) {
    return new CollectCoordinatesVarHandle(target, pos, filter);
  }

  private static class InstanceFieldVarHandle extends VarHandle {
    private final Field field;

    InstanceFieldVarHandle(Field field) {
      super(field.getType(), Collections.<Class<?>>singletonList(field.getDeclaringClass()));
      this.field = field;
    }

    @Override
    public Optional<VarHandleDesc> describeConstable() {
      return Optional.of(VarHandleDesc.ofField(
          (ClassDesc) field.getDeclaringClass().describeConstable().orElse(null),
          field.getName(),
          (ClassDesc) varType().describeConstable().orElse(null)));
    }

    private Object get0(Object[] args) {
      try {
        return field.get(args[0]);
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    private void set0(Object[] args) {
      try {
        field.set(args[0], args[1]);
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    private boolean compareAndSet0(Object[] args) {
      Object recv = args[0];
      Object expected = args[1];
      Object update = args[2];
      try {
        Object current = field.get(recv);
        if (sameValue(varType(), current, expected)) {
          field.set(recv, update);
          return true;
        }
        return false;
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    private Object getAndSet0(Object[] args) {
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

    private Object getAndAdd0(Object[] args) {
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
    public Object get(Object... args) {
      synchronized (lockFor(args)) {
        return get0(args);
      }
    }

    @Override
    public void set(Object... args) {
      synchronized (lockFor(args)) {
        set0(args);
      }
    }

    @Override
    public boolean compareAndSet(Object... args) {
      synchronized (lockFor(args)) {
        return compareAndSet0(args);
      }
    }

    @Override
    public Object getAndSet(Object... args) {
      synchronized (lockFor(args)) {
        return getAndSet0(args);
      }
    }

    @Override
    public Object getAndAdd(Object... args) {
      synchronized (lockFor(args)) {
        return getAndAdd0(args);
      }
    }
    @Override
    Object lockFor(Object[] args) {
      return stripe(args[0], field.hashCode());
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
    public Optional<VarHandleDesc> describeConstable() {
      return Optional.of(VarHandleDesc.ofStaticField(
          (ClassDesc) field.getDeclaringClass().describeConstable().orElse(null),
          field.getName(),
          (ClassDesc) varType().describeConstable().orElse(null)));
    }

    private Object get0(Object[] args) {
      try {
        return field.get(null);
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    private void set0(Object[] args) {
      try {
        field.set(null, args[0]);
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    private boolean compareAndSet0(Object[] args) {
      Object expected = args[0];
      Object update = args[1];
      try {
        Object current = field.get(null);
        if (sameValue(varType(), current, expected)) {
          field.set(null, update);
          return true;
        }
        return false;
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    private Object getAndSet0(Object[] args) {
      Object update = args[0];
      try {
        Object current = field.get(null);
        field.set(null, update);
        return current;
      } catch (IllegalAccessException e) {
        throw new IllegalAccessError(e.getMessage());
      }
    }

    private Object getAndAdd0(Object[] args) {
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
    public Object get(Object... args) {
      synchronized (lockFor(args)) {
        return get0(args);
      }
    }

    @Override
    public void set(Object... args) {
      synchronized (lockFor(args)) {
        set0(args);
      }
    }

    @Override
    public boolean compareAndSet(Object... args) {
      synchronized (lockFor(args)) {
        return compareAndSet0(args);
      }
    }

    @Override
    public Object getAndSet(Object... args) {
      synchronized (lockFor(args)) {
        return getAndSet0(args);
      }
    }

    @Override
    public Object getAndAdd(Object... args) {
      synchronized (lockFor(args)) {
        return getAndAdd0(args);
      }
    }
    @Override
    Object lockFor(Object[] args) {
      return stripe(field.getDeclaringClass(), field.hashCode());
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
    public Optional<VarHandleDesc> describeConstable() {
      return Optional.of(VarHandleDesc.ofArray(
          coordinateTypes().get(0).describeConstable().orElse(null)));
    }

    private Object get0(Object[] args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      return Array.get(array, index);
    }

    private void set0(Object[] args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      Object val = args[2];
      Array.set(array, index, val);
    }

    private boolean compareAndSet0(Object[] args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      Object expected = args[2];
      Object update = args[3];
      Object current = Array.get(array, index);
      if (sameValue(varType(), current, expected)) {
        Array.set(array, index, update);
        return true;
      }
      return false;
    }

    private Object getAndSet0(Object[] args) {
      Object array = args[0];
      int index = ((Number) args[1]).intValue();
      Object update = args[2];
      Object current = Array.get(array, index);
      Array.set(array, index, update);
      return current;
    }

    private Object getAndAdd0(Object[] args) {
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
    public Object get(Object... args) {
      synchronized (lockFor(args)) {
        return get0(args);
      }
    }

    @Override
    public void set(Object... args) {
      synchronized (lockFor(args)) {
        set0(args);
      }
    }

    @Override
    public boolean compareAndSet(Object... args) {
      synchronized (lockFor(args)) {
        return compareAndSet0(args);
      }
    }

    @Override
    public Object getAndSet(Object... args) {
      synchronized (lockFor(args)) {
        return getAndSet0(args);
      }
    }

    @Override
    public Object getAndAdd(Object... args) {
      synchronized (lockFor(args)) {
        return getAndAdd0(args);
      }
    }
    @Override
    Object lockFor(Object[] args) {
      return stripe(args[0], ((Number) args[1]).intValue());
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

  private static class ByteArrayViewVarHandle extends VarHandle {
    private final Class<?> componentType;
    private final ByteOrder byteOrder;
    private final int alignment;

    ByteArrayViewVarHandle(Class<?> viewArrayClass, ByteOrder byteOrder) {
      super(viewArrayClass.getComponentType(), Arrays.<Class<?>>asList(byte[].class, int.class));
      this.componentType = viewArrayClass.getComponentType();
      this.byteOrder = byteOrder;
      if (componentType == short.class || componentType == char.class) {
        this.alignment = 2;
      } else if (componentType == int.class || componentType == float.class) {
        this.alignment = 4;
      } else if (componentType == long.class || componentType == double.class) {
        this.alignment = 8;
      } else {
        throw new IllegalArgumentException("Unsupported view type: " + componentType);
      }
    }

    private void checkBounds(byte[] array, int offset) {
      Objects.requireNonNull(array);
      if (offset < 0 || offset > array.length - alignment) {
        throw new IndexOutOfBoundsException("Index " + offset + " out of bounds for length " + array.length);
      }
    }

    @Override
    public Object get(Object... args) {
      byte[] array = (byte[]) args[0];
      int offset = ((Number) args[1]).intValue();
      checkBounds(array, offset);
      boolean be = (byteOrder == ByteOrder.BIG_ENDIAN);
      if (componentType == int.class) {
        int b0 = array[offset] & 0xFF;
        int b1 = array[offset + 1] & 0xFF;
        int b2 = array[offset + 2] & 0xFF;
        int b3 = array[offset + 3] & 0xFF;
        return Integer.valueOf(be ? ((b0 << 24) | (b1 << 16) | (b2 << 8) | b3)
                                  : (b0 | (b1 << 8) | (b2 << 16) | (b3 << 24)));
      } else if (componentType == long.class) {
        long res = 0;
        if (be) {
          for (int i = 0; i < 8; i++) {
            res = (res << 8) | (array[offset + i] & 0xFFL);
          }
        } else {
          for (int i = 7; i >= 0; i--) {
            res = (res << 8) | (array[offset + i] & 0xFFL);
          }
        }
        return Long.valueOf(res);
      } else if (componentType == short.class) {
        int b0 = array[offset] & 0xFF;
        int b1 = array[offset + 1] & 0xFF;
        int val = be ? ((b0 << 8) | b1) : (b0 | (b1 << 8));
        return Short.valueOf((short) val);
      } else if (componentType == char.class) {
        int b0 = array[offset] & 0xFF;
        int b1 = array[offset + 1] & 0xFF;
        int val = be ? ((b0 << 8) | b1) : (b0 | (b1 << 8));
        return Character.valueOf((char) val);
      } else if (componentType == float.class) {
        int bits = ((Integer) get(array, offset)).intValue();
        return Float.valueOf(Float.intBitsToFloat(bits));
      } else if (componentType == double.class) {
        long bits = ((Long) get(array, offset)).longValue();
        return Double.valueOf(Double.longBitsToDouble(bits));
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public void set(Object... args) {
      byte[] array = (byte[]) args[0];
      int offset = ((Number) args[1]).intValue();
      Object val = args[2];
      checkBounds(array, offset);
      boolean be = (byteOrder == ByteOrder.BIG_ENDIAN);
      if (componentType == int.class) {
        int v = ((Number) val).intValue();
        if (be) {
          array[offset] = (byte) (v >> 24);
          array[offset + 1] = (byte) (v >> 16);
          array[offset + 2] = (byte) (v >> 8);
          array[offset + 3] = (byte) v;
        } else {
          array[offset] = (byte) v;
          array[offset + 1] = (byte) (v >> 8);
          array[offset + 2] = (byte) (v >> 16);
          array[offset + 3] = (byte) (v >> 24);
        }
      } else if (componentType == long.class) {
        long v = ((Number) val).longValue();
        if (be) {
          for (int i = 7; i >= 0; i--) {
            array[offset + i] = (byte) v;
            v >>>= 8;
          }
        } else {
          for (int i = 0; i < 8; i++) {
            array[offset + i] = (byte) v;
            v >>>= 8;
          }
        }
      } else if (componentType == short.class) {
        short v = ((Number) val).shortValue();
        if (be) {
          array[offset] = (byte) (v >> 8);
          array[offset + 1] = (byte) v;
        } else {
          array[offset] = (byte) v;
          array[offset + 1] = (byte) (v >> 8);
        }
      } else if (componentType == char.class) {
        char v = ((Character) val).charValue();
        if (be) {
          array[offset] = (byte) (v >> 8);
          array[offset + 1] = (byte) v;
        } else {
          array[offset] = (byte) v;
          array[offset + 1] = (byte) (v >> 8);
        }
      } else if (componentType == float.class) {
        float f = ((Number) val).floatValue();
        set(array, offset, Integer.valueOf(Float.floatToRawIntBits(f)));
      } else if (componentType == double.class) {
        double d = ((Number) val).doubleValue();
        set(array, offset, Long.valueOf(Double.doubleToRawLongBits(d)));
      }
    }

    @Override
    public boolean isAccessModeSupported(AccessMode accessMode) {
      Objects.requireNonNull(accessMode);
      return accessMode == AccessMode.GET || accessMode == AccessMode.SET;
    }

    @Override
    public boolean compareAndSet(Object... args) {
      throw new UnsupportedOperationException("compareAndSet");
    }

    @Override
    public Object getAndSet(Object... args) {
      throw new UnsupportedOperationException("getAndSet");
    }

    @Override
    public Object getAndAdd(Object... args) {
      throw new UnsupportedOperationException("getAndAdd");
    }

    @Override
    public String toString() {
      return "VarHandle[varType=" + varType().getName() + ", coord=[byte[], int]]";
    }
  }

  private static class ByteBufferViewVarHandle extends VarHandle {
    private final Class<?> componentType;
    private final ByteOrder byteOrder;
    private final int alignment;

    ByteBufferViewVarHandle(Class<?> viewArrayClass, ByteOrder byteOrder) {
      super(viewArrayClass.getComponentType(), Arrays.<Class<?>>asList(ByteBuffer.class, int.class));
      this.componentType = viewArrayClass.getComponentType();
      this.byteOrder = byteOrder;
      if (componentType == short.class || componentType == char.class) {
        this.alignment = 2;
      } else if (componentType == int.class || componentType == float.class) {
        this.alignment = 4;
      } else if (componentType == long.class || componentType == double.class) {
        this.alignment = 8;
      } else {
        throw new IllegalArgumentException("Unsupported view type: " + componentType);
      }
    }

    private void checkBounds(ByteBuffer buf, int offset) {
      Objects.requireNonNull(buf);
      if (offset < 0 || offset > buf.limit() - alignment) {
        throw new IndexOutOfBoundsException("Index " + offset + " out of bounds for limit " + buf.limit());
      }
    }

    @Override
    public Object get(Object... args) {
      ByteBuffer buf = (ByteBuffer) args[0];
      int offset = ((Number) args[1]).intValue();
      checkBounds(buf, offset);
      ByteOrder prev = buf.order();
      try {
        buf.order(byteOrder);
        if (componentType == int.class) {
          return Integer.valueOf(buf.getInt(offset));
        } else if (componentType == long.class) {
          return Long.valueOf(buf.getLong(offset));
        } else if (componentType == short.class) {
          return Short.valueOf(buf.getShort(offset));
        } else if (componentType == char.class) {
          return Character.valueOf(buf.getChar(offset));
        } else if (componentType == float.class) {
          return Float.valueOf(buf.getFloat(offset));
        } else if (componentType == double.class) {
          return Double.valueOf(buf.getDouble(offset));
        }
        throw new UnsupportedOperationException();
      } finally {
        buf.order(prev);
      }
    }

    @Override
    public void set(Object... args) {
      ByteBuffer buf = (ByteBuffer) args[0];
      int offset = ((Number) args[1]).intValue();
      Object val = args[2];
      checkBounds(buf, offset);
      ByteOrder prev = buf.order();
      try {
        buf.order(byteOrder);
        if (componentType == int.class) {
          buf.putInt(offset, ((Number) val).intValue());
        } else if (componentType == long.class) {
          buf.putLong(offset, ((Number) val).longValue());
        } else if (componentType == short.class) {
          buf.putShort(offset, ((Number) val).shortValue());
        } else if (componentType == char.class) {
          buf.putChar(offset, ((Character) val).charValue());
        } else if (componentType == float.class) {
          buf.putFloat(offset, ((Number) val).floatValue());
        } else if (componentType == double.class) {
          buf.putDouble(offset, ((Number) val).doubleValue());
        }
      } finally {
        buf.order(prev);
      }
    }

    private void checkAtomicAccess(ByteBuffer buf) {
      if (!buf.isDirect()) {
        throw new IllegalStateException("Atomic access not supported for heap buffer");
      }
    }

    @Override
    public synchronized boolean compareAndSet(Object... args) {
      ByteBuffer buf = (ByteBuffer) args[0];
      checkAtomicAccess(buf);
      int offset = ((Number) args[1]).intValue();
      Object expected = args[2];
      Object update = args[3];
      Object current = get(buf, offset);
      if (sameValue(varType(), current, expected)) {
        set(buf, offset, update);
        return true;
      }
      return false;
    }

    @Override
    public synchronized Object getAndSet(Object... args) {
      ByteBuffer buf = (ByteBuffer) args[0];
      checkAtomicAccess(buf);
      int offset = ((Number) args[1]).intValue();
      Object update = args[2];
      Object current = get(buf, offset);
      set(buf, offset, update);
      return current;
    }

    @Override
    public synchronized Object getAndAdd(Object... args) {
      ByteBuffer buf = (ByteBuffer) args[0];
      checkAtomicAccess(buf);
      int offset = ((Number) args[1]).intValue();
      Object delta = args[2];
      Object current = get(buf, offset);
      if (current instanceof Integer) {
        int cur = ((Integer) current).intValue();
        int d = ((Number) delta).intValue();
        set(buf, offset, Integer.valueOf(cur + d));
        return Integer.valueOf(cur);
      } else if (current instanceof Long) {
        long cur = ((Long) current).longValue();
        long d = ((Number) delta).longValue();
        set(buf, offset, Long.valueOf(cur + d));
        return Long.valueOf(cur);
      }
      throw new UnsupportedOperationException("getAndAdd not supported on " + varType());
    }

    @Override
    public String toString() {
      return "VarHandle[varType=" + varType().getName() + ", coord=[java.nio.ByteBuffer, int]]";
    }
  }

  public static final class VarHandleDesc extends DynamicConstantDesc<VarHandle> {
    private enum Kind {
      FIELD, STATIC_FIELD, ARRAY_ELEMENT
    }

    private final Kind kind;
    private final ClassDesc declaringClass;
    private final ClassDesc varType;

    private VarHandleDesc(Kind kind,
                          String name,
                          ClassDesc declaringClass,
                          ClassDesc varType) {
      super(kind == Kind.ARRAY_ELEMENT ? ConstantDescs.BSM_VARHANDLE_ARRAY
            : (kind == Kind.STATIC_FIELD ? ConstantDescs.BSM_VARHANDLE_STATIC_FIELD
                                        : ConstantDescs.BSM_VARHANDLE_FIELD),
            name,
            ConstantDescs.CD_VarHandle,
            kind == Kind.ARRAY_ELEMENT ? new ClassDesc[] { declaringClass }
                                       : new ClassDesc[] { declaringClass, varType });
      this.kind = kind;
      this.declaringClass = declaringClass;
      this.varType = varType;
    }

    public static VarHandleDesc ofField(ClassDesc declaringClass,
                                        String name,
                                        ClassDesc fieldType) {
      Objects.requireNonNull(declaringClass, "declaringClass");
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(fieldType, "fieldType");
      return new VarHandleDesc(Kind.FIELD, name, declaringClass, fieldType);
    }

    public static VarHandleDesc ofStaticField(ClassDesc declaringClass,
                                              String name,
                                              ClassDesc fieldType) {
      Objects.requireNonNull(declaringClass, "declaringClass");
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(fieldType, "fieldType");
      return new VarHandleDesc(Kind.STATIC_FIELD, name, declaringClass, fieldType);
    }

    public static VarHandleDesc ofArray(ClassDesc arrayClass) {
      Objects.requireNonNull(arrayClass, "arrayClass");
      if (!arrayClass.isArray()) {
        throw new IllegalArgumentException("not an array type: " + arrayClass);
      }
      return new VarHandleDesc(Kind.ARRAY_ELEMENT,
                               ConstantDescs.DEFAULT_NAME,
                               arrayClass,
                               arrayClass.componentType());
    }

    public ClassDesc varType() {
      return varType;
    }

    @Override
    public VarHandle resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
      switch (kind) {
        case FIELD:
          return lookup.findVarHandle(
              declaringClass.resolveConstantDesc(lookup),
              constantName(),
              varType.resolveConstantDesc(lookup));
        case STATIC_FIELD:
          return lookup.findStaticVarHandle(
              declaringClass.resolveConstantDesc(lookup),
              constantName(),
              varType.resolveConstantDesc(lookup));
        case ARRAY_ELEMENT:
          return MethodHandles.arrayElementVarHandle(
              declaringClass.resolveConstantDesc(lookup));
        default:
          throw new InternalError("Cannot reach here");
      }
    }

    @Override
    public String toString() {
      switch (kind) {
        case FIELD:
          return String.format("VarHandleDesc[%s.%s:%s]",
              declaringClass.displayName(), constantName(), varType.displayName());
        case STATIC_FIELD:
          return String.format("VarHandleDesc[static %s.%s:%s]",
              declaringClass.displayName(), constantName(), varType.displayName());
        case ARRAY_ELEMENT:
          return String.format("VarHandleDesc[%s[]]", declaringClass.displayName());
        default:
          throw new InternalError("Cannot reach here");
      }
    }
  }

  private static abstract class AdaptedVarHandle extends VarHandle {
    protected final VarHandle target;

    protected AdaptedVarHandle(VarHandle target, Class<?> varType, List<Class<?>> coordinates) {
      super(varType, coordinates);
      this.target = target;
    }

    @Override
    public boolean isAccessModeSupported(AccessMode accessMode) {
      return target.isAccessModeSupported(accessMode);
    }

    @Override
    public Object get(Object... args) {
      try {
        return toMethodHandle(AccessMode.GET).invokeWithArguments(args);
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }

    @Override
    public void set(Object... args) {
      try {
        toMethodHandle(AccessMode.SET).invokeWithArguments(args);
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }

    @Override
    public boolean compareAndSet(Object... args) {
      try {
        return (Boolean) toMethodHandle(AccessMode.COMPARE_AND_SET).invokeWithArguments(args);
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }

    @Override
    public Object getAndSet(Object... args) {
      try {
        return toMethodHandle(AccessMode.GET_AND_SET).invokeWithArguments(args);
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }

    @Override
    public Object getAndAdd(Object... args) {
      try {
        return toMethodHandle(AccessMode.GET_AND_ADD).invokeWithArguments(args);
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }

  private static class FilterValueVarHandle extends AdaptedVarHandle {
    private final MethodHandle filterToTarget;
    private final MethodHandle filterFromTarget;

    FilterValueVarHandle(VarHandle target, MethodHandle filterToTarget, MethodHandle filterFromTarget) {
      super(target, filterToTarget.type().parameterType(0), target.coordinateTypes());
      this.filterToTarget = filterToTarget;
      this.filterFromTarget = filterFromTarget;
    }

    @Override
    public MethodHandle toMethodHandle(AccessMode mode) {
      MethodHandle mh = target.toMethodHandle(mode);
      int coords = target.coordinateTypes().size();
      switch (mode.atype) {
        case GET:
          return MethodHandles.filterReturnValue(mh, filterFromTarget);
        case SET:
          return MethodHandles.filterArguments(mh, coords, filterToTarget);
        case COMPARE_AND_SWAP:
          return MethodHandles.filterArguments(mh, coords, filterToTarget, filterToTarget);
        case COMPARE_AND_EXCHANGE:
          mh = MethodHandles.filterArguments(mh, coords, filterToTarget, filterToTarget);
          return MethodHandles.filterReturnValue(mh, filterFromTarget);
        case GET_AND_UPDATE:
          if (mode.methodName().startsWith("getAndSet")) {
            mh = MethodHandles.filterArguments(mh, coords, filterToTarget);
            return MethodHandles.filterReturnValue(mh, filterFromTarget);
          }
          throw new UnsupportedOperationException(mode.methodName());
        default:
          throw new UnsupportedOperationException();
      }
    }
  }

  private static class FilterCoordinatesVarHandle extends AdaptedVarHandle {
    private final int pos;
    private final MethodHandle[] filters;

    private static List<Class<?>> computeCoords(VarHandle target, int pos, MethodHandle... filters) {
      List<Class<?>> coords = new ArrayList<Class<?>>(target.coordinateTypes());
      for (int i = 0; i < filters.length; i++) {
        coords.set(pos + i, filters[i].type().parameterType(0));
      }
      return coords;
    }

    FilterCoordinatesVarHandle(VarHandle target, int pos, MethodHandle... filters) {
      super(target, target.varType(), computeCoords(target, pos, filters));
      this.pos = pos;
      this.filters = filters.clone();
    }

    @Override
    public MethodHandle toMethodHandle(AccessMode mode) {
      MethodHandle mh = target.toMethodHandle(mode);
      return MethodHandles.filterArguments(mh, pos, filters);
    }
  }

  private static class InsertCoordinatesVarHandle extends AdaptedVarHandle {
    private final int pos;
    private final Object[] values;

    private static List<Class<?>> computeCoords(VarHandle target, int pos, Object... values) {
      List<Class<?>> coords = new ArrayList<Class<?>>(target.coordinateTypes());
      for (int i = 0; i < values.length; i++) {
        coords.remove(pos);
      }
      return coords;
    }

    InsertCoordinatesVarHandle(VarHandle target, int pos, Object... values) {
      super(target, target.varType(), computeCoords(target, pos, values));
      this.pos = pos;
      this.values = values.clone();
    }

    @Override
    public MethodHandle toMethodHandle(AccessMode mode) {
      MethodHandle mh = target.toMethodHandle(mode);
      return MethodHandles.insertArguments(mh, pos, values);
    }
  }

  private static class DropCoordinatesVarHandle extends AdaptedVarHandle {
    private final int pos;
    private final List<Class<?>> valueTypes;

    private static List<Class<?>> computeCoords(VarHandle target, int pos, List<Class<?>> valueTypes) {
      List<Class<?>> coords = new ArrayList<Class<?>>(target.coordinateTypes());
      coords.addAll(pos, valueTypes);
      return coords;
    }

    DropCoordinatesVarHandle(VarHandle target, int pos, List<Class<?>> valueTypes) {
      super(target, target.varType(), computeCoords(target, pos, valueTypes));
      this.pos = pos;
      this.valueTypes = new ArrayList<Class<?>>(valueTypes);
    }

    @Override
    public MethodHandle toMethodHandle(AccessMode mode) {
      MethodHandle mh = target.toMethodHandle(mode);
      return MethodHandles.dropArguments(mh, pos, valueTypes);
    }
  }

  private static class PermuteCoordinatesVarHandle extends AdaptedVarHandle {
    private final int[] reorder;

    PermuteCoordinatesVarHandle(VarHandle target, List<Class<?>> newCoordinates, int... reorder) {
      super(target, target.varType(), newCoordinates);
      this.reorder = reorder.clone();
    }

    @Override
    public MethodHandle toMethodHandle(AccessMode mode) {
      MethodHandle mh = target.toMethodHandle(mode);
      MethodType targetType = mh.type();
      int targetCoords = target.coordinateTypes().size();
      int extra = targetType.parameterCount() - targetCoords;
      List<Class<?>> newParams = new ArrayList<Class<?>>(coordinateTypes());
      for (int i = 0; i < extra; i++) {
        newParams.add(targetType.parameterType(targetCoords + i));
      }
      MethodType newType = MethodType.methodType(targetType.returnType(), newParams);
      int[] newReorder = new int[targetType.parameterCount()];
      for (int i = 0; i < targetCoords; i++) {
        newReorder[i] = reorder[i];
      }
      for (int i = 0; i < extra; i++) {
        newReorder[targetCoords + i] = coordinateTypes().size() + i;
      }
      return MethodHandles.permuteArguments(mh, newType, newReorder);
    }
  }

  private static class CollectCoordinatesVarHandle extends AdaptedVarHandle {
    private final int pos;
    private final MethodHandle filter;

    private static List<Class<?>> computeCoords(VarHandle target, int pos, MethodHandle filter) {
      List<Class<?>> coords = new ArrayList<Class<?>>(target.coordinateTypes());
      coords.remove(pos);
      coords.addAll(pos, filter.type().parameterList());
      return coords;
    }

    CollectCoordinatesVarHandle(VarHandle target, int pos, MethodHandle filter) {
      super(target, target.varType(), computeCoords(target, pos, filter));
      this.pos = pos;
      this.filter = filter;
    }

    @Override
    public MethodHandle toMethodHandle(AccessMode mode) {
      MethodHandle mh = target.toMethodHandle(mode);
      return MethodHandles.collectArguments(mh, pos, filter);
    }
  }
}
