/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

public interface Stream<T> extends BaseStream<T, Stream<T>> {
  Stream<T> filter(Predicate<? super T> predicate);

  <R> Stream<R> map(Function<? super T, ? extends R> mapper);

  IntStream mapToInt(ToIntFunction<? super T> mapper);

  LongStream mapToLong(ToLongFunction<? super T> mapper);

  DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);

  <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

  IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);

  LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);

  DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);

  Stream<T> distinct();

  Stream<T> sorted();

  Stream<T> sorted(Comparator<? super T> comparator);

  Stream<T> peek(Consumer<? super T> action);

  Stream<T> limit(long maxSize);

  Stream<T> skip(long n);

  default Stream<T> takeWhile(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);
    return StreamSupport.stream(new Spliterators.IteratorSpliterator<T>(new Iterator<T>() {
      private final Iterator<T> it = iterator();
      private boolean hasNext = false;
      private boolean finished = false;
      private T next = null;

      @Override
      public boolean hasNext() {
        if (hasNext) return true;
        if (finished) return false;
        if (it.hasNext()) {
          T item = it.next();
          if (predicate.test(item)) {
            next = item;
            hasNext = true;
            return true;
          }
        }
        finished = true;
        return false;
      }

      @Override
      public T next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        hasNext = false;
        return next;
      }
    }, spliterator().characteristics() & (Spliterator.ORDERED | Spliterator.SORTED | Spliterator.NONNULL)), isParallel());
  }

  default Stream<T> dropWhile(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);
    return StreamSupport.stream(new Spliterators.IteratorSpliterator<T>(new Iterator<T>() {
      private final Iterator<T> it = iterator();
      private boolean dropped = false;
      private boolean hasNext = false;
      private T next = null;

      @Override
      public boolean hasNext() {
        if (hasNext) return true;
        if (!dropped) {
          while (it.hasNext()) {
            T item = it.next();
            if (!predicate.test(item)) {
              next = item;
              hasNext = true;
              dropped = true;
              return true;
            }
          }
          dropped = true;
          return false;
        }
        if (it.hasNext()) {
          next = it.next();
          hasNext = true;
          return true;
        }
        return false;
      }

      @Override
      public T next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        hasNext = false;
        return next;
      }
    }, spliterator().characteristics() & (Spliterator.ORDERED | Spliterator.SORTED | Spliterator.NONNULL)), isParallel());
  }

  void forEach(Consumer<? super T> action);

  void forEachOrdered(Consumer<? super T> action);

  Object[] toArray();

  <A> A[] toArray(IntFunction<A[]> generator);

  T reduce(T identity, BinaryOperator<T> accumulator);

  Optional<T> reduce(BinaryOperator<T> accumulator);

  <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);

  <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner);

  <R, A> R collect(Collector<? super T, A, R> collector);

  Optional<T> min(Comparator<? super T> comparator);

  Optional<T> max(Comparator<? super T> comparator);

  long count();

  boolean anyMatch(Predicate<? super T> predicate);

  boolean allMatch(Predicate<? super T> predicate);

  boolean noneMatch(Predicate<? super T> predicate);

  Optional<T> findFirst();

  Optional<T> findAny();

  @SuppressWarnings("unchecked")
  default List<T> toList() {
    return (List<T>) Collections.unmodifiableList(java.util.Arrays.asList(toArray()));
  }

  public interface Builder<T> extends Consumer<T> {
    @Override
    void accept(T t);

    default Builder<T> add(T t) {
      accept(t);
      return this;
    }

    Stream<T> build();
  }

  public static <T> Builder<T> builder() {
    return new Builder<T>() {
      private final java.util.ArrayList<T> list = new java.util.ArrayList<>();
      private boolean built = false;

      @Override
      public void accept(T t) {
        if (built) throw new IllegalStateException();
        list.add(t);
      }

      @Override
      public Stream<T> build() {
        if (built) throw new IllegalStateException();
        built = true;
        return list.stream();
      }
    };
  }

  public static <T> Stream<T> empty() {
    return StreamSupport.stream(Spliterators.emptySpliterator(), false);
  }

  public static <T> Stream<T> of(T t) {
    return StreamSupport.stream(Spliterators.spliterator(new Object[]{t}, Spliterator.IMMUTABLE | Spliterator.ORDERED), false);
  }

  @SafeVarargs
  public static <T> Stream<T> of(T... values) {
    Objects.requireNonNull(values);
    return StreamSupport.stream(Spliterators.spliterator(values, Spliterator.IMMUTABLE | Spliterator.ORDERED), false);
  }

  public static <T> Stream<T> ofNullable(T t) {
    return t == null ? empty() : of(t);
  }

  public static <T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
    Objects.requireNonNull(f);
    Iterator<T> iterator = new Iterator<T>() {
      T cur = null;
      boolean first = true;

      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public T next() {
        if (first) {
          first = false;
          cur = seed;
        } else {
          cur = f.apply(cur);
        }
        return cur;
      }
    };
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
  }

  public static <T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
    Objects.requireNonNull(hasNext);
    Objects.requireNonNull(next);
    Iterator<T> iterator = new Iterator<T>() {
      T cur = null;
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
      public T next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        T val = cur;
        T n = next.apply(cur);
        if (hasNext.test(n)) {
          cur = n;
        } else {
          hasMore = false;
        }
        return val;
      }
    };
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
  }

  public static <T> Stream<T> generate(Supplier<? extends T> s) {
    Objects.requireNonNull(s);
    Iterator<T> iterator = new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public T next() {
        return s.get();
      }
    };
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.IMMUTABLE), false);
  }

  public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
    Objects.requireNonNull(a);
    Objects.requireNonNull(b);
    @SuppressWarnings("unchecked")
    Spliterator<T> splitA = (Spliterator<T>) a.spliterator();
    @SuppressWarnings("unchecked")
    Spliterator<T> splitB = (Spliterator<T>) b.spliterator();
    Iterator<T> itA = Spliterators.iterator(splitA);
    Iterator<T> itB = Spliterators.iterator(splitB);
    Iterator<T> it = new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return itA.hasNext() || itB.hasNext();
      }

      @Override
      public T next() {
        if (itA.hasNext()) return itA.next();
        return itB.next();
      }
    };
    Stream<T> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(it,
        splitA.characteristics() & splitB.characteristics() & (Spliterator.ORDERED | Spliterator.NONNULL)),
        a.isParallel() || b.isParallel());
    return stream.onClose(a::close).onClose(b::close);
  }
}
