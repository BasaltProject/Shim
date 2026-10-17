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
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.LongSummaryStatistics;
import java.util.function.BiConsumer;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

public interface LongStream extends BaseStream<Long, LongStream> {
  @Override
  LongStream sequential();

  @Override
  LongStream parallel();

  LongStream filter(LongPredicate predicate);

  LongStream map(LongUnaryOperator mapper);

  <U> Stream<U> mapToObj(LongFunction<? extends U> mapper);

  IntStream mapToInt(LongToIntFunction mapper);

  DoubleStream mapToDouble(LongToDoubleFunction mapper);

  LongStream flatMap(LongFunction<? extends LongStream> mapper);

  LongStream distinct();

  LongStream sorted();

  LongStream peek(LongConsumer action);

  LongStream limit(long maxSize);

  LongStream skip(long n);

  default LongStream takeWhile(LongPredicate predicate) {
    Objects.requireNonNull(predicate);
    return boxed().takeWhile(predicate::test).mapToLong(Long::longValue);
  }

  default LongStream dropWhile(LongPredicate predicate) {
    Objects.requireNonNull(predicate);
    return boxed().dropWhile(predicate::test).mapToLong(Long::longValue);
  }

  void forEach(LongConsumer action);

  void forEachOrdered(LongConsumer action);

  long[] toArray();

  long reduce(long identity, LongBinaryOperator op);

  OptionalLong reduce(LongBinaryOperator op);

  <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner);

  long sum();

  OptionalLong min();

  OptionalLong max();

  long count();

  OptionalDouble average();

  boolean anyMatch(LongPredicate predicate);

  boolean allMatch(LongPredicate predicate);

  boolean noneMatch(LongPredicate predicate);

  OptionalLong findFirst();

  OptionalLong findAny();

  DoubleStream asDoubleStream();

  Stream<Long> boxed();

  @Override
  PrimitiveIterator.OfLong iterator();

  @Override
  Spliterator.OfLong spliterator();

  public static LongStream empty() {
    return StreamSupport.longStream(Spliterators.emptyLongSpliterator(), false);
  }

  public static LongStream of(long t) {
    return of(new long[]{t});
  }

  public static LongStream of(long... values) {
    Objects.requireNonNull(values);
    return StreamSupport.longStream(Spliterators.spliterator(values, Spliterator.IMMUTABLE | Spliterator.ORDERED), false);
  }

  public static LongStream range(long startInclusive, long endExclusive) {
    if (startInclusive >= endExclusive) {
      return empty();
    }
    return iterate(startInclusive, i -> i < endExclusive, i -> i + 1);
  }

  public static LongStream rangeClosed(long startInclusive, long endInclusive) {
    if (startInclusive > endInclusive) {
      return empty();
    }
    return iterate(startInclusive, i -> i <= endInclusive, i -> i + 1);
  }

  public static LongStream iterate(final long seed, final LongUnaryOperator f) {
    Objects.requireNonNull(f);
    PrimitiveIterator.OfLong iterator = new PrimitiveIterator.OfLong() {
      long cur = 0;
      boolean first = true;

      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public long nextLong() {
        if (first) {
          first = false;
          cur = seed;
        } else {
          cur = f.applyAsLong(cur);
        }
        return cur;
      }
    };
    return StreamSupport.longStream(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE, false);
  }

  public static LongStream iterate(long seed, LongPredicate hasNext, LongUnaryOperator next) {
    Objects.requireNonNull(hasNext);
    Objects.requireNonNull(next);
    PrimitiveIterator.OfLong iterator = new PrimitiveIterator.OfLong() {
      long cur = 0;
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
      public long nextLong() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        long val = cur;
        long n = next.applyAsLong(cur);
        if (hasNext.test(n)) {
          cur = n;
        } else {
          hasMore = false;
        }
        return val;
      }
    };
    return StreamSupport.longStream(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE, false);
  }

  public static LongStream generate(LongSupplier s) {
    Objects.requireNonNull(s);
    PrimitiveIterator.OfLong iterator = new PrimitiveIterator.OfLong() {
      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public long nextLong() {
        return s.getAsLong();
      }
    };
    return StreamSupport.longStream(iterator, Spliterator.IMMUTABLE, false);
  }

  public static LongStream concat(LongStream a, LongStream b) {
    Objects.requireNonNull(a);
    Objects.requireNonNull(b);
    return Stream.concat(a.boxed(), b.boxed()).mapToLong(Long::longValue).onClose(a::close).onClose(b::close);
  }

  default LongSummaryStatistics summaryStatistics() {
    return collect(LongSummaryStatistics::new, LongSummaryStatistics::accept, LongSummaryStatistics::combine);
  }

  interface Builder extends LongConsumer {
    @Override
    void accept(long t);

    default Builder add(long t) {
      accept(t);
      return this;
    }

    LongStream build();
  }

  public static Builder builder() {
    return new Builder() {
      private final java.util.ArrayList<Long> list = new java.util.ArrayList<>();
      private boolean built;

      @Override
      public void accept(long t) {
        if (built) throw new IllegalStateException();
        list.add(t);
      }

      @Override
      public LongStream build() {
        if (built) throw new IllegalStateException();
        built = true;
        return list.stream().mapToLong(Long::longValue);
      }
    };
  }

  @FunctionalInterface
  interface LongMapMultiConsumer {
    void accept(long value, LongConsumer dc);
  }

  default LongStream mapMulti(LongMapMultiConsumer mapper) {
    java.util.Objects.requireNonNull(mapper);
    return flatMap(e -> {
      Builder buffer = LongStream.builder();
      mapper.accept(e, buffer);
      return buffer.build();
    });
  }
}
