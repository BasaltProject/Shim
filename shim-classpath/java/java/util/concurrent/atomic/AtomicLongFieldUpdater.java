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
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

import sun.misc.Unsafe;

public abstract class AtomicLongFieldUpdater<T> {

  protected AtomicLongFieldUpdater() {
  }

  public static <U> AtomicLongFieldUpdater<U> newUpdater(Class<U> tclass, String fieldName) {
    return new AtomicLongFieldUpdaterImpl<>(tclass, fieldName);
  }

  public abstract boolean compareAndSet(T obj, long expect, long update);

  public abstract boolean weakCompareAndSet(T obj, long expect, long update);

  public abstract void set(T obj, long newValue);

  public abstract void lazySet(T obj, long newValue);

  public abstract long get(T obj);

  public long getAndSet(T obj, long newValue) {
    long prev;
    do {
      prev = get(obj);
    } while (!compareAndSet(obj, prev, newValue));
    return prev;
  }

  public long getAndIncrement(T obj) {
    return getAndAdd(obj, 1L);
  }

  public long getAndDecrement(T obj) {
    return getAndAdd(obj, -1L);
  }

  public long getAndAdd(T obj, long delta) {
    long prev;
    do {
      prev = get(obj);
    } while (!compareAndSet(obj, prev, prev + delta));
    return prev;
  }

  public long incrementAndGet(T obj) {
    return addAndGet(obj, 1L);
  }

  public long decrementAndGet(T obj) {
    return addAndGet(obj, -1L);
  }

  public long addAndGet(T obj, long delta) {
    long prev, next;
    do {
      prev = get(obj);
      next = prev + delta;
    } while (!compareAndSet(obj, prev, next));
    return next;
  }

  public final long getAndUpdate(T obj, LongUnaryOperator updateFunction) {
    long prev, next;
    do {
      prev = get(obj);
      next = updateFunction.applyAsLong(prev);
    } while (!compareAndSet(obj, prev, next));
    return prev;
  }

  public final long updateAndGet(T obj, LongUnaryOperator updateFunction) {
    long prev, next;
    do {
      prev = get(obj);
      next = updateFunction.applyAsLong(prev);
    } while (!compareAndSet(obj, prev, next));
    return next;
  }

  public final long getAndAccumulate(T obj, long x, LongBinaryOperator accumulatorFunction) {
    long prev, next;
    do {
      prev = get(obj);
      next = accumulatorFunction.applyAsLong(prev, x);
    } while (!compareAndSet(obj, prev, next));
    return prev;
  }

  public final long accumulateAndGet(T obj, long x, LongBinaryOperator accumulatorFunction) {
    long prev, next;
    do {
      prev = get(obj);
      next = accumulatorFunction.applyAsLong(prev, x);
    } while (!compareAndSet(obj, prev, next));
    return next;
  }

  private static final class AtomicLongFieldUpdaterImpl<T> extends AtomicLongFieldUpdater<T> {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long offset;
    private final Class<T> tclass;

    AtomicLongFieldUpdaterImpl(Class<T> tclass, String fieldName) {
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
      if (field.getType() != long.class) {
        throw new IllegalArgumentException("Must be long type");
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
    public boolean compareAndSet(T obj, long expect, long update) {
      check(obj);
      return unsafe.compareAndSwapLong(obj, offset, expect, update);
    }

    @Override
    public boolean weakCompareAndSet(T obj, long expect, long update) {
      return compareAndSet(obj, expect, update);
    }

    @Override
    public void set(T obj, long newValue) {
      check(obj);
      unsafe.putLongVolatile(obj, offset, newValue);
    }

    @Override
    public void lazySet(T obj, long newValue) {
      check(obj);
      unsafe.putOrderedLong(obj, offset, newValue);
    }

    @Override
    public long get(T obj) {
      check(obj);
      return unsafe.getLongVolatile(obj, offset);
    }
  }
}
