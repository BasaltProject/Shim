/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import sun.misc.Unsafe;

public abstract class AtomicIntegerFieldUpdater<T> {

  protected AtomicIntegerFieldUpdater() {
  }

  public static <U> AtomicIntegerFieldUpdater<U> newUpdater(Class<U> tclass, String fieldName) {
    return new AtomicIntegerFieldUpdaterImpl<>(tclass, fieldName);
  }

  public abstract boolean compareAndSet(T obj, int expect, int update);

  public abstract boolean weakCompareAndSet(T obj, int expect, int update);

  public abstract void set(T obj, int newValue);

  public abstract void lazySet(T obj, int newValue);

  public abstract int get(T obj);

  public int getAndSet(T obj, int newValue) {
    int prev;
    do {
      prev = get(obj);
    } while (!compareAndSet(obj, prev, newValue));
    return prev;
  }

  public int getAndIncrement(T obj) {
    return getAndAdd(obj, 1);
  }

  public int getAndDecrement(T obj) {
    return getAndAdd(obj, -1);
  }

  public int getAndAdd(T obj, int delta) {
    int prev;
    do {
      prev = get(obj);
    } while (!compareAndSet(obj, prev, prev + delta));
    return prev;
  }

  public int incrementAndGet(T obj) {
    return addAndGet(obj, 1);
  }

  public int decrementAndGet(T obj) {
    return addAndGet(obj, -1);
  }

  public int addAndGet(T obj, int delta) {
    int prev, next;
    do {
      prev = get(obj);
      next = prev + delta;
    } while (!compareAndSet(obj, prev, next));
    return next;
  }

  public final int getAndUpdate(T obj, IntUnaryOperator updateFunction) {
    int prev, next;
    do {
      prev = get(obj);
      next = updateFunction.applyAsInt(prev);
    } while (!compareAndSet(obj, prev, next));
    return prev;
  }

  public final int updateAndGet(T obj, IntUnaryOperator updateFunction) {
    int prev, next;
    do {
      prev = get(obj);
      next = updateFunction.applyAsInt(prev);
    } while (!compareAndSet(obj, prev, next));
    return next;
  }

  public final int getAndAccumulate(T obj, int x, IntBinaryOperator accumulatorFunction) {
    int prev, next;
    do {
      prev = get(obj);
      next = accumulatorFunction.applyAsInt(prev, x);
    } while (!compareAndSet(obj, prev, next));
    return prev;
  }

  public final int accumulateAndGet(T obj, int x, IntBinaryOperator accumulatorFunction) {
    int prev, next;
    do {
      prev = get(obj);
      next = accumulatorFunction.applyAsInt(prev, x);
    } while (!compareAndSet(obj, prev, next));
    return next;
  }

  private static final class AtomicIntegerFieldUpdaterImpl<T> extends AtomicIntegerFieldUpdater<T> {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long offset;
    private final Class<T> tclass;

    AtomicIntegerFieldUpdaterImpl(Class<T> tclass, String fieldName) {
      Objects.requireNonNull(tclass, "tclass");
      Objects.requireNonNull(fieldName, "fieldName");

      Field field = null;
      for (Class<?> c = tclass; c != null; c = c.getSuperclass()) {
        try {
          field = c.getDeclaredField(fieldName);
          break;
        } catch (NoSuchFieldException ignored) {
        }
      }

      if (field == null) {
        throw new RuntimeException(new NoSuchFieldException(fieldName));
      }

      field.setAccessible(true);
      int modifiers = field.getModifiers();
      if (field.getType() != int.class) {
        throw new IllegalArgumentException("Must be integer type");
      }
      if (!Modifier.isVolatile(modifiers)) {
        throw new IllegalArgumentException("Must be volatile type");
      }
      if (Modifier.isStatic(modifiers)) {
        throw new IllegalArgumentException("Must not be static");
      }
      if (Modifier.isFinal(modifiers)) {
        throw new IllegalArgumentException("Must not be final");
      }

      this.tclass = tclass;
      this.offset = unsafe.objectFieldOffset(field);
    }

    private void check(T obj) {
      if (obj == null) {
        throw new NullPointerException();
      }
      if (!tclass.isInstance(obj)) {
        throw new ClassCastException();
      }
    }

    @Override
    public boolean compareAndSet(T obj, int expect, int update) {
      check(obj);
      return unsafe.compareAndSwapInt(obj, offset, expect, update);
    }

    @Override
    public boolean weakCompareAndSet(T obj, int expect, int update) {
      return compareAndSet(obj, expect, update);
    }

    @Override
    public void set(T obj, int newValue) {
      check(obj);
      unsafe.putIntVolatile(obj, offset, newValue);
    }

    @Override
    public void lazySet(T obj, int newValue) {
      check(obj);
      unsafe.putOrderedInt(obj, offset, newValue);
    }

    @Override
    public int get(T obj) {
      check(obj);
      return unsafe.getIntVolatile(obj, offset);
    }
  }
}
