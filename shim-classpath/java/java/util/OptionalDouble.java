/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class OptionalDouble {
  private static final OptionalDouble EMPTY = new OptionalDouble();

  private final boolean isPresent;
  private final double value;

  private OptionalDouble() {
    this.isPresent = false;
    this.value = Double.NaN;
  }

  public static OptionalDouble empty() {
    return EMPTY;
  }

  private OptionalDouble(double value) {
    this.isPresent = true;
    this.value = value;
  }

  public static OptionalDouble of(double value) {
    return new OptionalDouble(value);
  }

  public double getAsDouble() {
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

  public void ifPresent(DoubleConsumer action) {
    if (isPresent) {
      action.accept(value);
    }
  }

  public void ifPresentOrElse(DoubleConsumer action, Runnable emptyAction) {
    if (isPresent) {
      action.accept(value);
    } else {
      emptyAction.run();
    }
  }

  public double orElse(double other) {
    return isPresent ? value : other;
  }

  public double orElseGet(DoubleSupplier supplier) {
    return isPresent ? value : supplier.getAsDouble();
  }

  public double orElseThrow() {
    if (!isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public <X extends Throwable> double orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
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
    if (!(obj instanceof OptionalDouble)) {
      return false;
    }
    OptionalDouble other = (OptionalDouble) obj;
    return (isPresent && other.isPresent)
        ? Double.compare(value, other.value) == 0
        : isPresent == other.isPresent;
  }

  @Override
  public int hashCode() {
    return isPresent ? Double.hashCode(value) : 0;
  }

  @Override
  public String toString() {
    return isPresent ? ("OptionalDouble[" + value + "]") : "OptionalDouble.empty";
  }
}
