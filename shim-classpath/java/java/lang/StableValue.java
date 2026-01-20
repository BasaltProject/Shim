/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import shim.StableCollections;
import shim.StableValueImpl;

public interface StableValue<T> {
  boolean trySet(T contents);

  T orElse(T other);

  T orElseThrow();

  boolean isSet();

  T orElseSet(Supplier<? extends T> supplier);

  void setOrThrow(T contents);

  @Override
  boolean equals(Object obj);

  @Override
  int hashCode();

  static <T> StableValue<T> of() {
    return StableValueImpl.newInstance();
  }

  static <T> StableValue<T> of(T contents) {
    StableValue<T> value = StableValueImpl.newInstance();
    value.trySet(contents);
    return value;
  }

  static <T> Supplier<T> supplier(Supplier<? extends T> underlying) {
    return StableCollections.supplier(underlying);
  }

  static <R> IntFunction<R> intFunction(int size, IntFunction<? extends R> underlying) {
    return StableCollections.intFunction(size, underlying);
  }

  static <T, R> Function<T, R> function(Set<? extends T> inputs, Function<? super T, ? extends R> underlying) {
    return StableCollections.function(inputs, underlying);
  }

  static <E> List<E> list(int size, IntFunction<? extends E> mapper) {
    return StableCollections.list(size, mapper);
  }

  static <K, V> Map<K, V> map(Set<K> keys, Function<? super K, ? extends V> mapper) {
    return StableCollections.map(keys, mapper);
  }
}
