/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

interface Sink<T> extends Consumer<T> {
  default void begin(long size) {}

  default void end() {}

  default boolean cancellationRequested() {
    return false;
  }

  default void accept(int value) {
    throw new IllegalStateException("called wrong accept method");
  }

  default void accept(long value) {
    throw new IllegalStateException("called wrong accept method");
  }

  default void accept(double value) {
    throw new IllegalStateException("called wrong accept method");
  }

  interface OfInt extends Sink<Integer>, IntConsumer {
    @Override
    void accept(int value);

    @Override
    default void accept(Integer i) {
      accept(i.intValue());
    }
  }

  interface OfLong extends Sink<Long>, LongConsumer {
    @Override
    void accept(long value);

    @Override
    default void accept(Long i) {
      accept(i.longValue());
    }
  }

  interface OfDouble extends Sink<Double>, DoubleConsumer {
    @Override
    void accept(double value);

    @Override
    default void accept(Double i) {
      accept(i.doubleValue());
    }
  }

  abstract static class ChainedReference<T, E_OUT> implements Sink<T> {
    protected final Sink<? super E_OUT> downstream;

    public ChainedReference(Sink<? super E_OUT> downstream) {
      this.downstream = Objects.requireNonNull(downstream);
    }

    @Override
    public void begin(long size) {
      downstream.begin(size);
    }

    @Override
    public void end() {
      downstream.end();
    }

    @Override
    public boolean cancellationRequested() {
      return downstream.cancellationRequested();
    }
  }

  abstract static class ChainedInt<E_OUT> implements Sink.OfInt {
    protected final Sink<? super E_OUT> downstream;

    public ChainedInt(Sink<? super E_OUT> downstream) {
      this.downstream = Objects.requireNonNull(downstream);
    }

    @Override
    public void begin(long size) {
      downstream.begin(size);
    }

    @Override
    public void end() {
      downstream.end();
    }

    @Override
    public boolean cancellationRequested() {
      return downstream.cancellationRequested();
    }
  }

  abstract static class ChainedLong<E_OUT> implements Sink.OfLong {
    protected final Sink<? super E_OUT> downstream;

    public ChainedLong(Sink<? super E_OUT> downstream) {
      this.downstream = Objects.requireNonNull(downstream);
    }

    @Override
    public void begin(long size) {
      downstream.begin(size);
    }

    @Override
    public void end() {
      downstream.end();
    }

    @Override
    public boolean cancellationRequested() {
      return downstream.cancellationRequested();
    }
  }

  abstract static class ChainedDouble<E_OUT> implements Sink.OfDouble {
    protected final Sink<? super E_OUT> downstream;

    public ChainedDouble(Sink<? super E_OUT> downstream) {
      this.downstream = Objects.requireNonNull(downstream);
    }

    @Override
    public void begin(long size) {
      downstream.begin(size);
    }

    @Override
    public void end() {
      downstream.end();
    }

    @Override
    public boolean cancellationRequested() {
      return downstream.cancellationRequested();
    }
  }
}
