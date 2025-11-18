/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public interface Gatherer<T, A, R> {
  default Supplier<A> initializer() {
    return GatherImpl.<A>defaultInitializer();
  }

  Integrator<A, T, R> integrator();

  static <A> Supplier<A> defaultInitializer() {
    return GatherImpl.<A>defaultInitializer();
  }

  static <A> BinaryOperator<A> defaultCombiner() {
    return GatherImpl.<A>failingCombiner();
  }

  static <A, R> BiConsumer<A, Downstream<? super R>> defaultFinisher() {
    return GatherImpl.<A, R>defaultFinisher();
  }

  default BinaryOperator<A> combiner() {
    return GatherImpl.<A>failingCombiner();
  }

  default BiConsumer<A, Downstream<? super R>> finisher() {
    return GatherImpl.<A, R>defaultFinisher();
  }

  static <T, R> Gatherer<T, Void, R> ofSequential(Integrator<Void, T, R> integrator) {
    return new GatherImpl<T, Void, R>(null, Objects.requireNonNull(integrator), null, null);
  }

  static <T, R> Gatherer<T, Void, R> ofSequential(
      Integrator<Void, T, R> integrator, BiConsumer<Void, Downstream<? super R>> finisher) {
    return new GatherImpl<T, Void, R>(null, Objects.requireNonNull(integrator), null, Objects.requireNonNull(finisher));
  }

  static <T, A, R> Gatherer<T, A, R> ofSequential(
      Supplier<A> initializer, Integrator<A, T, R> integrator) {
    return new GatherImpl<T, A, R>(
        Objects.requireNonNull(initializer), Objects.requireNonNull(integrator), null, null);
  }

  static <T, A, R> Gatherer<T, A, R> ofSequential(
      Supplier<A> initializer, Integrator<A, T, R> integrator, BiConsumer<A, Downstream<? super R>> finisher) {
    return new GatherImpl<T, A, R>(Objects.requireNonNull(initializer), Objects.requireNonNull(integrator), null,
        Objects.requireNonNull(finisher));
  }

  static <T, R> Gatherer<T, Void, R> of(Integrator<Void, T, R> integrator) {
    return new GatherImpl<T, Void, R>(null, Objects.requireNonNull(integrator), GatherImpl.<Void>failingCombiner(),
        null);
  }

  static <T, R> Gatherer<T, Void, R> of(
      Integrator<Void, T, R> integrator, BiConsumer<Void, Downstream<? super R>> finisher) {
    return new GatherImpl<T, Void, R>(null, Objects.requireNonNull(integrator), GatherImpl.<Void>failingCombiner(),
        Objects.requireNonNull(finisher));
  }

  static <T, A, R> Gatherer<T, A, R> of(Supplier<A> initializer, Integrator<A, T, R> integrator,
      BinaryOperator<A> combiner, BiConsumer<A, Downstream<? super R>> finisher) {
    return new GatherImpl<T, A, R>(Objects.requireNonNull(initializer), Objects.requireNonNull(integrator),
        Objects.requireNonNull(combiner), Objects.requireNonNull(finisher));
  }

  static <T, A, R> Gatherer<T, A, R> of(
      Supplier<A> initializer, Integrator<A, T, R> integrator, BinaryOperator<A> combiner) {
    return new GatherImpl<T, A, R>(Objects.requireNonNull(initializer), Objects.requireNonNull(integrator),
        Objects.requireNonNull(combiner), null);
  }

  default <RR> Gatherer<T, ?, RR> andThen(Gatherer<? super R, ?, ? extends RR> that) {
    Objects.requireNonNull(that);
    return GatherImpl.compose(this, that);
  }

  @FunctionalInterface
  interface Integrator<A, T, R> {
    boolean integrate(A state, T element, Downstream<? super R> downstream);

    static <A, T, R> Greedy<A, T, R> ofGreedy(Greedy<A, T, R> greedy) {
      return Objects.requireNonNull(greedy);
    }

    static <A, T, R> Integrator<A, T, R> of(Integrator<A, T, R> integrator) {
      return Objects.requireNonNull(integrator);
    }

    @FunctionalInterface
    interface Greedy<A, T, R> extends Integrator<A, T, R> { }
  }

  @FunctionalInterface
  interface Downstream<T> {
    boolean push(T element);

    default boolean isRejecting() {
      return false;
    }
  }
}
