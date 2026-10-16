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
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.IntSummaryStatistics;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

public interface IntStream extends BaseStream<Integer, IntStream> {
  IntStream filter(IntPredicate predicate);

  IntStream map(IntUnaryOperator mapper);

  <U> Stream<U> mapToObj(IntFunction<? extends U> mapper);

  LongStream mapToLong(IntToLongFunction mapper);

  DoubleStream mapToDouble(IntToDoubleFunction mapper);

  IntStream flatMap(IntFunction<? extends IntStream> mapper);

  IntStream distinct();

  IntStream sorted();

  IntStream peek(IntConsumer action);

  IntStream limit(long maxSize);

  IntStream skip(long n);

  default IntStream takeWhile(IntPredicate predicate) {
    Objects.requireNonNull(predicate);
    return boxed().takeWhile(predicate::test).mapToInt(Integer::intValue);
  }

  default IntStream dropWhile(IntPredicate predicate) {
    Objects.requireNonNull(predicate);
    return boxed().dropWhile(predicate::test).mapToInt(Integer::intValue);
  }

  void forEach(IntConsumer action);

  void forEachOrdered(IntConsumer action);

  int[] toArray();

  int reduce(int identity, IntBinaryOperator op);

  OptionalInt reduce(IntBinaryOperator op);

  <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner);

  int sum();

  OptionalInt min();

  OptionalInt max();

  long count();

  OptionalDouble average();

  boolean anyMatch(IntPredicate predicate);

  boolean allMatch(IntPredicate predicate);

  boolean noneMatch(IntPredicate predicate);

  OptionalInt findFirst();

  OptionalInt findAny();

  LongStream asLongStream();

  DoubleStream asDoubleStream();

  Stream<Integer> boxed();

  @Override
  PrimitiveIterator.OfInt iterator();

  @Override
  Spliterator.OfInt spliterator();

  public static IntStream empty() {
    return StreamSupport.intStream(Spliterators.emptyIntSpliterator(), false);
  }

  public static IntStream of(int t) {
    return of(new int[]{t});
  }

  public static IntStream of(int... values) {
    Objects.requireNonNull(values);
    return StreamSupport.intStream(Spliterators.spliterator(values, Spliterator.IMMUTABLE | Spliterator.ORDERED), false);
  }

  public static IntStream range(int startInclusive, int endExclusive) {
    if (startInclusive >= endExclusive) {
      return empty();
    }
    return iterate(startInclusive, i -> i < endExclusive, i -> i + 1);
  }

  public static IntStream rangeClosed(int startInclusive, int endInclusive) {
    if (startInclusive > endInclusive) {
      return empty();
    }
    return iterate(startInclusive, i -> i <= endInclusive, i -> i + 1);
  }

  public static IntStream iterate(final int seed, final IntUnaryOperator f) {
    Objects.requireNonNull(f);
    PrimitiveIterator.OfInt iterator = new PrimitiveIterator.OfInt() {
      int cur = 0;
      boolean first = true;

      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public int nextInt() {
        if (first) {
          first = false;
          cur = seed;
        } else {
          cur = f.applyAsInt(cur);
        }
        return cur;
      }
    };
    return StreamSupport.intStream(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE, false);
  }

  public static IntStream iterate(int seed, IntPredicate hasNext, IntUnaryOperator next) {
    Objects.requireNonNull(hasNext);
    Objects.requireNonNull(next);
    PrimitiveIterator.OfInt iterator = new PrimitiveIterator.OfInt() {
      int cur = 0;
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
      public int nextInt() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        int val = cur;
        int n = next.applyAsInt(cur);
        if (hasNext.test(n)) {
          cur = n;
        } else {
          hasMore = false;
        }
        return val;
      }
    };
    return StreamSupport.intStream(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE, false);
  }

  public static IntStream generate(IntSupplier s) {
    Objects.requireNonNull(s);
    PrimitiveIterator.OfInt iterator = new PrimitiveIterator.OfInt() {
      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public int nextInt() {
        return s.getAsInt();
      }
    };
    return StreamSupport.intStream(iterator, Spliterator.IMMUTABLE, false);
  }

  public static IntStream concat(IntStream a, IntStream b) {
    Objects.requireNonNull(a);
    Objects.requireNonNull(b);
    return Stream.concat(a.boxed(), b.boxed()).mapToInt(Integer::intValue).onClose(a::close).onClose(b::close);
  }

  default IntSummaryStatistics summaryStatistics() {
    return collect(IntSummaryStatistics::new, IntSummaryStatistics::accept, IntSummaryStatistics::combine);
  }

  interface Builder extends IntConsumer {
    @Override
    void accept(int t);

    default Builder add(int t) {
      accept(t);
      return this;
    }

    IntStream build();
  }

  public static Builder builder() {
    return new Builder() {
      private final java.util.ArrayList<Integer> list = new java.util.ArrayList<>();
      private boolean built;

      @Override
      public void accept(int t) {
        if (built) throw new IllegalStateException();
        list.add(t);
      }

      @Override
      public IntStream build() {
        if (built) throw new IllegalStateException();
        built = true;
        return list.stream().mapToInt(Integer::intValue);
      }
    };
  }

  @FunctionalInterface
  interface IntMapMultiConsumer {
    void accept(int value, IntConsumer dc);
  }

  default IntStream mapMulti(IntMapMultiConsumer mapper) {
    java.util.Objects.requireNonNull(mapper);
    return flatMap(e -> {
      Builder buffer = IntStream.builder();
      mapper.accept(e, buffer);
      return buffer.build();
    });
  }
}
