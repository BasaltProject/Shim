/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Collector<T, A, R> {
  Supplier<A> supplier();

  BiConsumer<A, T> accumulator();

  BinaryOperator<A> combiner();

  Function<A, R> finisher();

  Set<Characteristics> characteristics();

  enum Characteristics {
    CONCURRENT,
    UNORDERED,
    IDENTITY_FINISH
  }

  public static <T, R> Collector<T, R, R> of(
      Supplier<R> supplier,
      BiConsumer<R, T> accumulator,
      BinaryOperator<R> combiner,
      Characteristics... characteristics) {
    Objects.requireNonNull(supplier);
    Objects.requireNonNull(accumulator);
    Objects.requireNonNull(combiner);
    Objects.requireNonNull(characteristics);
    Set<Characteristics> cs = (characteristics.length == 0)
        ? Collections.emptySet()
        : Collections.unmodifiableSet(new java.util.HashSet<>(java.util.Arrays.asList(characteristics)));
    return new Collectors.CollectorImpl<>(supplier, accumulator, combiner, cs);
  }

  public static <T, A, R> Collector<T, A, R> of(
      Supplier<A> supplier,
      BiConsumer<A, T> accumulator,
      BinaryOperator<A> combiner,
      Function<A, R> finisher,
      Characteristics... characteristics) {
    Objects.requireNonNull(supplier);
    Objects.requireNonNull(accumulator);
    Objects.requireNonNull(combiner);
    Objects.requireNonNull(finisher);
    Objects.requireNonNull(characteristics);
    Set<Characteristics> cs = (characteristics.length == 0)
        ? Collections.emptySet()
        : Collections.unmodifiableSet(new java.util.HashSet<>(java.util.Arrays.asList(characteristics)));
    return new Collectors.CollectorImpl<>(supplier, accumulator, combiner, finisher, cs);
  }
}
