/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class OptionalInt {
  private static final OptionalInt EMPTY = new OptionalInt();

  private final boolean isPresent;
  private final int value;

  private OptionalInt() {
    this.isPresent = false;
    this.value = 0;
  }

  public static OptionalInt empty() {
    return EMPTY;
  }

  private OptionalInt(int value) {
    this.isPresent = true;
    this.value = value;
  }

  public static OptionalInt of(int value) {
    return new OptionalInt(value);
  }

  public int getAsInt() {
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

  public void ifPresent(IntConsumer action) {
    if (isPresent) {
      action.accept(value);
    }
  }

  public void ifPresentOrElse(IntConsumer action, Runnable emptyAction) {
    if (isPresent) {
      action.accept(value);
    } else {
      emptyAction.run();
    }
  }

  public int orElse(int other) {
    return isPresent ? value : other;
  }

  public int orElseGet(IntSupplier supplier) {
    return isPresent ? value : supplier.getAsInt();
  }

  public int orElseThrow() {
    if (!isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public <X extends Throwable> int orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
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
    if (!(obj instanceof OptionalInt)) {
      return false;
    }
    OptionalInt other = (OptionalInt) obj;
    return (isPresent && other.isPresent)
        ? value == other.value
        : isPresent == other.isPresent;
  }

  @Override
  public int hashCode() {
    return isPresent ? Integer.hashCode(value) : 0;
  }

  @Override
  public String toString() {
    return isPresent ? ("OptionalInt[" + value + "]") : "OptionalInt.empty";
  }

  public java.util.stream.IntStream stream() {
    return isPresent ? java.util.stream.IntStream.of(value) : java.util.stream.IntStream.empty();
  }
}
