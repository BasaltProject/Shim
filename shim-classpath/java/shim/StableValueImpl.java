/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.lang.StableValue;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

public final class StableValueImpl<T> implements StableValue<T> {
  private static final Object kUnset = new Object();

  private volatile Object value = kUnset;
  private Thread computing;

  private StableValueImpl() { }

  public static <T> StableValueImpl<T> newInstance() {
    return new StableValueImpl<T>();
  }

  private static IllegalStateException recursive() {
    return new IllegalStateException("Recursive initialization of a stable value is illegal");
  }

  @Override
  public synchronized boolean trySet(T contents) {
    if (computing == Thread.currentThread()) {
      throw recursive();
    }
    if (value != kUnset) {
      return false;
    }
    value = contents;
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T orElse(T other) {
    Object v = value;
    return v == kUnset ? other : (T) v;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T orElseThrow() {
    Object v = value;
    if (v == kUnset) {
      throw new NoSuchElementException("No contents set");
    }
    return (T) v;
  }

  @Override
  public boolean isSet() {
    return value != kUnset;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T orElseSet(Supplier<? extends T> supplier) {
    Objects.requireNonNull(supplier);
    Object v = value;
    if (v != kUnset) {
      return (T) v;
    }
    synchronized (this) {
      v = value;
      if (v != kUnset) {
        return (T) v;
      }
      if (computing == Thread.currentThread()) {
        throw recursive();
      }
      computing = Thread.currentThread();
      try {
        T result = supplier.get();
        value = result;
        return result;
      } finally {
        computing = null;
      }
    }
  }

  @Override
  public void setOrThrow(T contents) {
    if (!trySet(contents)) {
      throw new IllegalStateException("The contents is already set");
    }
  }

  @Override
  public String toString() {
    Object v = value;
    if (v == kUnset) {
      return ".unset";
    }
    return v == this ? "(this StableValue)" : String.valueOf(v);
  }
}
