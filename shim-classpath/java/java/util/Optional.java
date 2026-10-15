/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Optional<T> {
  private static final Optional<?> EMPTY = new Optional<>(null);

  private final T value;

  private Optional(T value) {
    this.value = value;
  }

  public static <T> Optional<T> empty() {
    @SuppressWarnings("unchecked")
    Optional<T> t = (Optional<T>) EMPTY;
    return t;
  }

  public static <T> Optional<T> of(T value) {
    return new Optional<>(Objects.requireNonNull(value));
  }

  public static <T> Optional<T> ofNullable(T value) {
    return value == null ? empty() : new Optional<>(value);
  }

  public T get() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public boolean isPresent() {
    return value != null;
  }

  public boolean isEmpty() {
    return value == null;
  }

  public void ifPresent(Consumer<? super T> action) {
    if (value != null) {
      action.accept(value);
    }
  }

  public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
    if (value != null) {
      action.accept(value);
    } else {
      emptyAction.run();
    }
  }

  public Optional<T> filter(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);
    if (isEmpty()) {
      return this;
    } else {
      return predicate.test(value) ? this : empty();
    }
  }

  public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper);
    if (isEmpty()) {
      return empty();
    } else {
      return Optional.ofNullable(mapper.apply(value));
    }
  }

  public <U> Optional<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper) {
    Objects.requireNonNull(mapper);
    if (isEmpty()) {
      return empty();
    } else {
      @SuppressWarnings("unchecked")
      Optional<U> r = (Optional<U>) mapper.apply(value);
      return Objects.requireNonNull(r);
    }
  }

  public Optional<T> or(Supplier<? extends Optional<? extends T>> supplier) {
    Objects.requireNonNull(supplier);
    if (isPresent()) {
      return this;
    } else {
      @SuppressWarnings("unchecked")
      Optional<T> r = (Optional<T>) supplier.get();
      return Objects.requireNonNull(r);
    }
  }

  public T orElse(T other) {
    return value != null ? value : other;
  }

  public T orElseGet(Supplier<? extends T> supplier) {
    return value != null ? value : supplier.get();
  }

  public T orElseThrow() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (value != null) {
      return value;
    }
    throw exceptionSupplier.get();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Optional)) {
      return false;
    }
    Optional<?> other = (Optional<?>) obj;
    return Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return value != null ? ("Optional[" + value + "]") : "Optional.empty";
  }
}
