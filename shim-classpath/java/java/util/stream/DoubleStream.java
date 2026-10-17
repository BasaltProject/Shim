/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.Iterator;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.DoubleSummaryStatistics;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

public interface DoubleStream extends BaseStream<Double, DoubleStream> {
  @Override
  DoubleStream sequential();

  @Override
  DoubleStream parallel();

  DoubleStream filter(DoublePredicate predicate);

  DoubleStream map(DoubleUnaryOperator mapper);

  <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper);

  IntStream mapToInt(DoubleToIntFunction mapper);

  LongStream mapToLong(DoubleToLongFunction mapper);

  DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper);

  DoubleStream distinct();

  DoubleStream sorted();

  DoubleStream peek(DoubleConsumer action);

  DoubleStream limit(long maxSize);

  DoubleStream skip(long n);

  default DoubleStream takeWhile(DoublePredicate predicate) {
    Objects.requireNonNull(predicate);
    return boxed().takeWhile(predicate::test).mapToDouble(Double::doubleValue);
  }

  default DoubleStream dropWhile(DoublePredicate predicate) {
    Objects.requireNonNull(predicate);
    return boxed().dropWhile(predicate::test).mapToDouble(Double::doubleValue);
  }

  void forEach(DoubleConsumer action);

  void forEachOrdered(DoubleConsumer action);

  double[] toArray();

  double reduce(double identity, DoubleBinaryOperator op);

  OptionalDouble reduce(DoubleBinaryOperator op);

  <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner);

  double sum();

  OptionalDouble min();

  OptionalDouble max();

  long count();

  OptionalDouble average();

  boolean anyMatch(DoublePredicate predicate);

  boolean allMatch(DoublePredicate predicate);

  boolean noneMatch(DoublePredicate predicate);

  OptionalDouble findFirst();

  OptionalDouble findAny();

  Stream<Double> boxed();

  @Override
  PrimitiveIterator.OfDouble iterator();

  @Override
  Spliterator.OfDouble spliterator();

  public static DoubleStream empty() {
    return StreamSupport.doubleStream(Spliterators.emptyDoubleSpliterator(), false);
  }

  public static DoubleStream of(double t) {
    return of(new double[]{t});
  }

  public static DoubleStream of(double... values) {
    Objects.requireNonNull(values);
    return StreamSupport.doubleStream(Spliterators.spliterator(values, Spliterator.IMMUTABLE | Spliterator.ORDERED), false);
  }

  public static DoubleStream iterate(final double seed, final DoubleUnaryOperator f) {
    Objects.requireNonNull(f);
    PrimitiveIterator.OfDouble iterator = new PrimitiveIterator.OfDouble() {
      double cur = 0;
      boolean first = true;

      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public double nextDouble() {
        if (first) {
          first = false;
          cur = seed;
        } else {
          cur = f.applyAsDouble(cur);
        }
        return cur;
      }
    };
    return StreamSupport.doubleStream(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE, false);
  }

  public static DoubleStream iterate(double seed, DoublePredicate hasNext, DoubleUnaryOperator next) {
    Objects.requireNonNull(hasNext);
    Objects.requireNonNull(next);
    PrimitiveIterator.OfDouble iterator = new PrimitiveIterator.OfDouble() {
      double cur = 0;
      boolean started = false;
      boolean hasMore = true;

      @Override
      public boolean hasNext() {
        if (!started) {
          started = true;
          if (hasNext.test(seed)) {
            cur = seed;
            hasMore = true;
          } else {
            hasMore = false;
          }
        }
        return hasMore;
      }

      @Override
      public double nextDouble() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        double val = cur;
        double n = next.applyAsDouble(cur);
        if (hasNext.test(n)) {
          cur = n;
        } else {
          hasMore = false;
        }
        return val;
      }
    };
    return StreamSupport.doubleStream(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE, false);
  }

  public static DoubleStream generate(DoubleSupplier s) {
    Objects.requireNonNull(s);
    PrimitiveIterator.OfDouble iterator = new PrimitiveIterator.OfDouble() {
      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public double nextDouble() {
        return s.getAsDouble();
      }
    };
    return StreamSupport.doubleStream(iterator, Spliterator.IMMUTABLE, false);
  }

  public static DoubleStream concat(DoubleStream a, DoubleStream b) {
    Objects.requireNonNull(a);
    Objects.requireNonNull(b);
    return Stream.concat(a.boxed(), b.boxed()).mapToDouble(Double::doubleValue).onClose(a::close).onClose(b::close);
  }

  default DoubleSummaryStatistics summaryStatistics() {
    return collect(DoubleSummaryStatistics::new, DoubleSummaryStatistics::accept, DoubleSummaryStatistics::combine);
  }

  interface Builder extends DoubleConsumer {
    @Override
    void accept(double t);

    default Builder add(double t) {
      accept(t);
      return this;
    }

    DoubleStream build();
  }

  public static Builder builder() {
    return new Builder() {
      private final java.util.ArrayList<Double> list = new java.util.ArrayList<>();
      private boolean built;

      @Override
      public void accept(double t) {
        if (built) throw new IllegalStateException();
        list.add(t);
      }

      @Override
      public DoubleStream build() {
        if (built) throw new IllegalStateException();
        built = true;
        return list.stream().mapToDouble(Double::doubleValue);
      }
    };
  }

  @FunctionalInterface
  interface DoubleMapMultiConsumer {
    void accept(double value, DoubleConsumer dc);
  }

  default DoubleStream mapMulti(DoubleMapMultiConsumer mapper) {
    java.util.Objects.requireNonNull(mapper);
    return flatMap(e -> {
      Builder buffer = DoubleStream.builder();
      mapper.accept(e, buffer);
      return buffer.build();
    });
  }
}
