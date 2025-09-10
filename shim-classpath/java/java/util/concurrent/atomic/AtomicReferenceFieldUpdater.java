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
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import sun.misc.Unsafe;

public abstract class AtomicReferenceFieldUpdater<T, V> {

  protected AtomicReferenceFieldUpdater() {
  }

  public static <U, W> AtomicReferenceFieldUpdater<U, W> newUpdater(
      Class<U> tclass, Class<W> vclass, String fieldName) {
    return new AtomicReferenceFieldUpdaterImpl<>(tclass, vclass, fieldName);
  }

  public abstract boolean compareAndSet(T obj, V expect, V update);

  public abstract boolean weakCompareAndSet(T obj, V expect, V update);

  public abstract void set(T obj, V newValue);

  public abstract void lazySet(T obj, V newValue);

  public abstract V get(T obj);

  public V getAndSet(T obj, V newValue) {
    V prev;
    do {
      prev = get(obj);
    } while (!compareAndSet(obj, prev, newValue));
    return prev;
  }

  public final V getAndUpdate(T obj, UnaryOperator<V> updateFunction) {
    V prev, next;
    do {
      prev = get(obj);
      next = updateFunction.apply(prev);
    } while (!compareAndSet(obj, prev, next));
    return prev;
  }

  public final V updateAndGet(T obj, UnaryOperator<V> updateFunction) {
    V prev, next;
    do {
      prev = get(obj);
      next = updateFunction.apply(prev);
    } while (!compareAndSet(obj, prev, next));
    return next;
  }

  public final V getAndAccumulate(T obj, V x, BinaryOperator<V> accumulatorFunction) {
    V prev, next;
    do {
      prev = get(obj);
      next = accumulatorFunction.apply(prev, x);
    } while (!compareAndSet(obj, prev, next));
    return prev;
  }

  public final V accumulateAndGet(T obj, V x, BinaryOperator<V> accumulatorFunction) {
    V prev, next;
    do {
      prev = get(obj);
      next = accumulatorFunction.apply(prev, x);
    } while (!compareAndSet(obj, prev, next));
    return next;
  }

  private static final class AtomicReferenceFieldUpdaterImpl<T, V>
      extends AtomicReferenceFieldUpdater<T, V> {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long offset;
    private final Class<T> tclass;
    private final Class<V> vclass;

    AtomicReferenceFieldUpdaterImpl(Class<T> tclass, Class<V> vclass, String fieldName) {
      Objects.requireNonNull(tclass, "tclass");
      Objects.requireNonNull(vclass, "vclass");
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
      if (!vclass.isAssignableFrom(field.getType())) {
        throw new IllegalArgumentException(
            "Must be reference type compatible with " + vclass.getName());
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
      this.vclass = vclass;
      this.offset = unsafe.objectFieldOffset(field);
    }

    private void check(T obj, V v) {
      if (obj == null) {
        throw new NullPointerException();
      }
      if (!tclass.isInstance(obj)) {
        throw new ClassCastException();
      }
      if (v != null && !vclass.isInstance(v)) {
        throw new ClassCastException();
      }
    }

    @Override
    public boolean compareAndSet(T obj, V expect, V update) {
      check(obj, update);
      return unsafe.compareAndSwapObject(obj, offset, expect, update);
    }

    @Override
    public boolean weakCompareAndSet(T obj, V expect, V update) {
      return compareAndSet(obj, expect, update);
    }

    @Override
    public void set(T obj, V newValue) {
      check(obj, newValue);
      unsafe.putObjectVolatile(obj, offset, newValue);
    }

    @Override
    public void lazySet(T obj, V newValue) {
      check(obj, newValue);
      unsafe.putOrderedObject(obj, offset, newValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(T obj) {
      if (obj == null) {
        throw new NullPointerException();
      }
      if (!tclass.isInstance(obj)) {
        throw new ClassCastException();
      }
      return (V) unsafe.getObjectVolatile(obj, offset);
    }
  }
}
