/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class OptionalLong {
  private static final OptionalLong EMPTY = new OptionalLong();

  private final boolean isPresent;
  private final long value;

  private OptionalLong() {
    this.isPresent = false;
    this.value = 0L;
  }

  public static OptionalLong empty() {
    return EMPTY;
  }

  private OptionalLong(long value) {
    this.isPresent = true;
    this.value = value;
  }

  public static OptionalLong of(long value) {
    return new OptionalLong(value);
  }

  public long getAsLong() {
    if (!isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public boolean isPresent() {
    return isPresent;
  }

  public boolean isEmpty() {
    return !isPresent;
  }

  public void ifPresent(LongConsumer action) {
    if (isPresent) {
      action.accept(value);
    }
  }

  public void ifPresentOrElse(LongConsumer action, Runnable emptyAction) {
    if (isPresent) {
      action.accept(value);
    } else {
      emptyAction.run();
    }
  }

  public long orElse(long other) {
    return isPresent ? value : other;
  }

  public long orElseGet(LongSupplier supplier) {
    return isPresent ? value : supplier.getAsLong();
  }

  public long orElseThrow() {
    if (!isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public <X extends Throwable> long orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (isPresent) {
      return value;
    }
    throw exceptionSupplier.get();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OptionalLong)) {
      return false;
    }
    OptionalLong other = (OptionalLong) obj;
    return (isPresent && other.isPresent)
        ? value == other.value
        : isPresent == other.isPresent;
  }

  @Override
  public int hashCode() {
    return isPresent ? Long.hashCode(value) : 0;
  }

  @Override
  public String toString() {
    return isPresent ? ("OptionalLong[" + value + "]") : "OptionalLong.empty";
  }

  public java.util.stream.LongStream stream() {
    return isPresent ? java.util.stream.LongStream.of(value) : java.util.stream.LongStream.empty();
  }
}
