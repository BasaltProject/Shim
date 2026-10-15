/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public final class StreamSupport {
  private StreamSupport() {}

  public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
    Objects.requireNonNull(spliterator);
    return new ReferencePipeline<>(spliterator, parallel);
  }

  public static <T> Stream<T> stream(Supplier<? extends Spliterator<T>> supplier, int characteristics, boolean parallel) {
    Objects.requireNonNull(supplier);
    return new ReferencePipeline<>(supplier.get(), parallel);
  }

  public static IntStream intStream(Spliterator.OfInt spliterator, boolean parallel) {
    Objects.requireNonNull(spliterator);
    return new IntPipeline(spliterator, parallel);
  }

  public static IntStream intStream(Supplier<? extends Spliterator.OfInt> supplier, int characteristics, boolean parallel) {
    Objects.requireNonNull(supplier);
    return new IntPipeline(supplier.get(), parallel);
  }

  public static IntStream intStream(PrimitiveIterator.OfInt iterator, int characteristics, boolean parallel) {
    Objects.requireNonNull(iterator);
    return new IntPipeline(Spliterators.spliteratorUnknownSize(iterator, characteristics), parallel);
  }

  public static LongStream longStream(Spliterator.OfLong spliterator, boolean parallel) {
    Objects.requireNonNull(spliterator);
    return new LongPipeline(spliterator, parallel);
  }

  public static LongStream longStream(Supplier<? extends Spliterator.OfLong> supplier, int characteristics, boolean parallel) {
    Objects.requireNonNull(supplier);
    return new LongPipeline(supplier.get(), parallel);
  }

  public static LongStream longStream(PrimitiveIterator.OfLong iterator, int characteristics, boolean parallel) {
    Objects.requireNonNull(iterator);
    return new LongPipeline(Spliterators.spliteratorUnknownSize(iterator, characteristics), parallel);
  }

  public static DoubleStream doubleStream(Spliterator.OfDouble spliterator, boolean parallel) {
    Objects.requireNonNull(spliterator);
    return new DoublePipeline(spliterator, parallel);
  }

  public static DoubleStream doubleStream(Supplier<? extends Spliterator.OfDouble> supplier, int characteristics, boolean parallel) {
    Objects.requireNonNull(supplier);
    return new DoublePipeline(supplier.get(), parallel);
  }

  public static DoubleStream doubleStream(PrimitiveIterator.OfDouble iterator, int characteristics, boolean parallel) {
    Objects.requireNonNull(iterator);
    return new DoublePipeline(Spliterators.spliteratorUnknownSize(iterator, characteristics), parallel);
  }

  static class ReferencePipeline<T> implements Stream<T> {
    private final Spliterator<T> spliterator;
    private final boolean parallel;
    private boolean linkedOrConsumed = false;
    private List<Runnable> closeHandlers = null;

    ReferencePipeline(Spliterator<T> spliterator, boolean parallel) {
      this.spliterator = spliterator;
      this.parallel = parallel;
    }

    ReferencePipeline(Spliterator<T> spliterator, boolean parallel, List<Runnable> closeHandlers) {
      this.spliterator = spliterator;
      this.parallel = parallel;
      if (closeHandlers != null) {
        this.closeHandlers = new ArrayList<>(closeHandlers);
      }
    }

    private void checkConsumed() {
      if (linkedOrConsumed) {
        throw new IllegalStateException("stream has already been operated upon or closed");
      }
    }

    @Override
    public Iterator<T> iterator() {
      return Spliterators.iterator(spliterator());
    }

    @Override
    public Spliterator<T> spliterator() {
      return spliterator;
    }

    @Override
    public boolean isParallel() {
      return parallel;
    }

    @Override
    public Stream<T> sequential() {
      return parallel ? new ReferencePipeline<>(spliterator, false, closeHandlers) : this;
    }

    @Override
    public Stream<T> parallel() {
      return parallel ? this : new ReferencePipeline<>(spliterator, true, closeHandlers);
    }

    @Override
    public Stream<T> unordered() {
      return this;
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
      Objects.requireNonNull(closeHandler);
      if (closeHandlers == null) {
        closeHandlers = new ArrayList<>();
      }
      closeHandlers.add(closeHandler);
      return this;
    }

    @Override
    public void close() {
      linkedOrConsumed = true;
      if (closeHandlers != null) {
        Throwable error = null;
        for (Runnable handler : closeHandlers) {
          try {
            handler.run();
          } catch (Throwable t) {
            if (error == null) {
              error = t;
            } else {
              error.addSuppressed(t);
            }
          }
        }
        closeHandlers = null;
        if (error instanceof RuntimeException) {
          throw (RuntimeException) error;
        } else if (error instanceof Error) {
          throw (Error) error;
        } else if (error != null) {
          throw new RuntimeException(error);
        }
      }
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
      Objects.requireNonNull(predicate);
      checkConsumed();
      linkedOrConsumed = true;
      Iterator<T> src = iterator();
      Iterator<T> it = new Iterator<T>() {
        boolean hasNext = false;
        T next;

        @Override
        public boolean hasNext() {
          if (hasNext) return true;
          while (src.hasNext()) {
            T val = src.next();
            if (predicate.test(val)) {
              next = val;
              hasNext = true;
              return true;
            }
          }
          return false;
        }

        @Override
        public T next() {
          if (!hasNext()) throw new NoSuchElementException();
          hasNext = false;
          return next;
        }
      };
      return new ReferencePipeline<>(
          Spliterators.spliteratorUnknownSize(it, spliterator.characteristics() & ~Spliterator.SIZED),
          parallel,
          closeHandlers);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
      Objects.requireNonNull(mapper);
      checkConsumed();
      linkedOrConsumed = true;
      Iterator<T> src = iterator();
      Iterator<R> it = new Iterator<R>() {
        @Override
        public boolean hasNext() {
          return src.hasNext();
        }

        @Override
        public R next() {
          return mapper.apply(src.next());
        }
      };
      long size = spliterator.getExactSizeIfKnown();
      int flags = spliterator.characteristics() & ~(Spliterator.SORTED | Spliterator.DISTINCT);
      Spliterator<R> sp = size >= 0
          ? Spliterators.spliterator(it, size, flags)
          : Spliterators.spliteratorUnknownSize(it, flags);
      return new ReferencePipeline<>(sp, parallel, closeHandlers);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
      Objects.requireNonNull(mapper);
      checkConsumed();
      linkedOrConsumed = true;
      return new IntPipeline(new Spliterator.OfInt() {
        private final Spliterator<T> sp = spliterator;

        @Override
        public boolean tryAdvance(IntConsumer action) {
          Objects.requireNonNull(action);
          return sp.tryAdvance(t -> action.accept(mapper.applyAsInt(t)));
        }

        @Override
        public OfInt trySplit() {
          return null;
        }

        @Override
        public long estimateSize() {
          return sp.estimateSize();
        }

        @Override
        public int characteristics() {
          return sp.characteristics() & ~(Spliterator.SORTED | Spliterator.DISTINCT);
        }
      }, parallel, closeHandlers);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
      Objects.requireNonNull(mapper);
      checkConsumed();
      linkedOrConsumed = true;
      return new LongPipeline(new Spliterator.OfLong() {
        private final Spliterator<T> sp = spliterator;

        @Override
        public boolean tryAdvance(LongConsumer action) {
          Objects.requireNonNull(action);
          return sp.tryAdvance(t -> action.accept(mapper.applyAsLong(t)));
        }

        @Override
        public OfLong trySplit() {
          return null;
        }

        @Override
        public long estimateSize() {
          return sp.estimateSize();
        }

        @Override
        public int characteristics() {
          return sp.characteristics() & ~(Spliterator.SORTED | Spliterator.DISTINCT);
        }
      }, parallel, closeHandlers);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
      Objects.requireNonNull(mapper);
      checkConsumed();
      linkedOrConsumed = true;
      return new DoublePipeline(new Spliterator.OfDouble() {
        private final Spliterator<T> sp = spliterator;

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
          Objects.requireNonNull(action);
          return sp.tryAdvance(t -> action.accept(mapper.applyAsDouble(t)));
        }

        @Override
        public OfDouble trySplit() {
          return null;
        }

        @Override
        public long estimateSize() {
          return sp.estimateSize();
        }

        @Override
        public int characteristics() {
          return sp.characteristics() & ~(Spliterator.SORTED | Spliterator.DISTINCT);
        }
      }, parallel, closeHandlers);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
      Objects.requireNonNull(mapper);
      checkConsumed();
      linkedOrConsumed = true;
      Iterator<T> src = iterator();
      Iterator<R> it = new Iterator<R>() {
        Iterator<? extends R> cur = Collections.emptyIterator();
        Stream<? extends R> curStream = null;

        @Override
        public boolean hasNext() {
          while (!cur.hasNext()) {
            if (curStream != null) {
              curStream.close();
              curStream = null;
            }
            if (!src.hasNext()) return false;
            T val = src.next();
            Stream<? extends R> st = mapper.apply(val);
            if (st != null) {
              curStream = st;
              cur = st.iterator();
            }
          }
          return true;
        }

        @Override
        public R next() {
          if (!hasNext()) throw new NoSuchElementException();
          return cur.next();
        }
      };
      return new ReferencePipeline<>(Spliterators.spliteratorUnknownSize(it, 0), parallel, closeHandlers);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
      Objects.requireNonNull(mapper);
      return flatMap(t -> {
        IntStream is = mapper.apply(t);
        return is == null ? null : is.boxed();
      }).mapToInt(Integer::intValue);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
      Objects.requireNonNull(mapper);
      return flatMap(t -> {
        LongStream ls = mapper.apply(t);
        return ls == null ? null : ls.boxed();
      }).mapToLong(Long::longValue);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
      Objects.requireNonNull(mapper);
      return flatMap(t -> {
        DoubleStream ds = mapper.apply(t);
        return ds == null ? null : ds.boxed();
      }).mapToDouble(Double::doubleValue);
    }

    @Override
    public Stream<T> distinct() {
      checkConsumed();
      linkedOrConsumed = true;
      Iterator<T> src = iterator();
      Set<T> seen = new HashSet<>();
      Iterator<T> it = new Iterator<T>() {
        boolean hasNext = false;
        T next;

        @Override
        public boolean hasNext() {
          if (hasNext) return true;
          while (src.hasNext()) {
            T val = src.next();
            if (seen.add(val)) {
              next = val;
              hasNext = true;
              return true;
            }
          }
          return false;
        }

        @Override
        public T next() {
          if (!hasNext()) throw new NoSuchElementException();
          hasNext = false;
          return next;
        }
      };
      return new ReferencePipeline<>(
          Spliterators.spliteratorUnknownSize(it, Spliterator.DISTINCT),
          parallel,
          closeHandlers);
    }

    @Override
    public Stream<T> sorted() {
      return sorted(null);
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
      checkConsumed();
      linkedOrConsumed = true;
      List<T> list = new ArrayList<>();
      Iterator<T> it = iterator();
      while (it.hasNext()) {
        list.add(it.next());
      }
      Collections.sort(list, comparator);
      return new ReferencePipeline<>(
          list.spliterator(),
          parallel,
          closeHandlers);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
      Objects.requireNonNull(action);
      checkConsumed();
      linkedOrConsumed = true;
      Iterator<T> src = iterator();
      Iterator<T> it = new Iterator<T>() {
        @Override
        public boolean hasNext() {
          return src.hasNext();
        }

        @Override
        public T next() {
          T val = src.next();
          action.accept(val);
          return val;
        }
      };
      return new ReferencePipeline<>(
          Spliterators.spliterator(it, spliterator.estimateSize(), spliterator.characteristics()),
          parallel,
          closeHandlers);
    }

    @Override
    public Stream<T> limit(long maxSize) {
      if (maxSize < 0) {
        throw new IllegalArgumentException(Long.toString(maxSize));
      }
      checkConsumed();
      linkedOrConsumed = true;
      Iterator<T> src = iterator();
      Iterator<T> it = new Iterator<T>() {
        long count = 0;

        @Override
        public boolean hasNext() {
          return count < maxSize && src.hasNext();
        }

        @Override
        public T next() {
          if (!hasNext()) throw new NoSuchElementException();
          count++;
          return src.next();
        }
      };
      long est = Math.min(spliterator.estimateSize(), maxSize);
      return new ReferencePipeline<>(
          Spliterators.spliterator(it, est, spliterator.characteristics()),
          parallel,
          closeHandlers);
    }

    @Override
    public Stream<T> skip(long n) {
      if (n < 0) {
        throw new IllegalArgumentException(Long.toString(n));
      }
      checkConsumed();
      linkedOrConsumed = true;
      Iterator<T> src = iterator();
      Iterator<T> it = new Iterator<T>() {
        boolean skipped = false;

        private void doSkip() {
          if (!skipped) {
            skipped = true;
            long count = 0;
            while (count < n && src.hasNext()) {
              src.next();
              count++;
            }
          }
        }

        @Override
        public boolean hasNext() {
          doSkip();
          return src.hasNext();
        }

        @Override
        public T next() {
          doSkip();
          return src.next();
        }
      };
      long est = Math.max(0L, spliterator.estimateSize() - n);
      return new ReferencePipeline<>(
          Spliterators.spliterator(it, est, spliterator.characteristics() & ~Spliterator.SIZED),
          parallel,
          closeHandlers);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
      Objects.requireNonNull(action);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          action.accept(it.next());
        }
      } finally {
        close();
      }
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
      forEach(action);
    }

    @Override
    public Object[] toArray() {
      checkConsumed();
      linkedOrConsumed = true;
      try {
        List<T> list = new ArrayList<>();
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          list.add(it.next());
        }
        return list.toArray();
      } finally {
        close();
      }
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
      Objects.requireNonNull(generator);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        List<T> list = new ArrayList<>();
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          list.add(it.next());
        }
        return list.toArray(generator.apply(list.size()));
      } finally {
        close();
      }
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
      Objects.requireNonNull(accumulator);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        T result = identity;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          result = accumulator.apply(result, it.next());
        }
        return result;
      } finally {
        close();
      }
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
      Objects.requireNonNull(accumulator);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        Iterator<T> it = iterator();
        if (!it.hasNext()) {
          return Optional.empty();
        }
        T result = it.next();
        while (it.hasNext()) {
          result = accumulator.apply(result, it.next());
        }
        return Optional.ofNullable(result);
      } finally {
        close();
      }
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
      Objects.requireNonNull(accumulator);
      Objects.requireNonNull(combiner);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        U result = identity;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          result = accumulator.apply(result, it.next());
        }
        return result;
      } finally {
        close();
      }
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
      Objects.requireNonNull(supplier);
      Objects.requireNonNull(accumulator);
      Objects.requireNonNull(combiner);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        R container = supplier.get();
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          accumulator.accept(container, it.next());
        }
        return container;
      } finally {
        close();
      }
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
      Objects.requireNonNull(collector);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        A container = collector.supplier().get();
        BiConsumer<A, ? super T> accumulator = collector.accumulator();
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          accumulator.accept(container, it.next());
        }
        return collector.finisher().apply(container);
      } finally {
        close();
      }
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
      Objects.requireNonNull(comparator);
      return reduce(BinaryOperator.minBy(comparator));
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
      Objects.requireNonNull(comparator);
      return reduce(BinaryOperator.maxBy(comparator));
    }

    @Override
    public long count() {
      checkConsumed();
      linkedOrConsumed = true;
      try {
        long count = 0;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          it.next();
          count++;
        }
        return count;
      } finally {
        close();
      }
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
      Objects.requireNonNull(predicate);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          if (predicate.test(it.next())) return true;
        }
        return false;
      } finally {
        close();
      }
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
      Objects.requireNonNull(predicate);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          if (!predicate.test(it.next())) return false;
        }
        return true;
      } finally {
        close();
      }
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
      Objects.requireNonNull(predicate);
      checkConsumed();
      linkedOrConsumed = true;
      try {
        Iterator<T> it = iterator();
        while (it.hasNext()) {
          if (predicate.test(it.next())) return false;
        }
        return true;
      } finally {
        close();
      }
    }

    @Override
    public Optional<T> findFirst() {
      checkConsumed();
      linkedOrConsumed = true;
      try {
        Iterator<T> it = iterator();
        return it.hasNext() ? Optional.ofNullable(it.next()) : Optional.empty();
      } finally {
        close();
      }
    }

    @Override
    public Optional<T> findAny() {
      return findFirst();
    }
  }

  static class IntPipeline implements IntStream {
    private final Spliterator.OfInt spliterator;
    private final boolean parallel;
    private List<Runnable> closeHandlers = null;

    IntPipeline(Spliterator.OfInt spliterator, boolean parallel) {
      this.spliterator = spliterator;
      this.parallel = parallel;
    }

    IntPipeline(Spliterator.OfInt spliterator, boolean parallel, List<Runnable> closeHandlers) {
      this.spliterator = spliterator;
      this.parallel = parallel;
      if (closeHandlers != null) {
        this.closeHandlers = new ArrayList<>(closeHandlers);
      }
    }

    @Override
    public PrimitiveIterator.OfInt iterator() {
      return Spliterators.iterator(spliterator());
    }

    @Override
    public Spliterator.OfInt spliterator() {
      return spliterator;
    }

    @Override
    public boolean isParallel() {
      return parallel;
    }

    @Override
    public IntStream sequential() {
      return parallel ? new IntPipeline(spliterator, false, closeHandlers) : this;
    }

    @Override
    public IntStream parallel() {
      return parallel ? this : new IntPipeline(spliterator, true, closeHandlers);
    }

    @Override
    public IntStream unordered() {
      return this;
    }

    @Override
    public IntStream onClose(Runnable closeHandler) {
      Objects.requireNonNull(closeHandler);
      if (closeHandlers == null) {
        closeHandlers = new ArrayList<>();
      }
      closeHandlers.add(closeHandler);
      return this;
    }

    @Override
    public void close() {
      if (closeHandlers != null) {
        for (Runnable r : closeHandlers) {
          r.run();
        }
        closeHandlers = null;
      }
    }

    @Override
    public Stream<Integer> boxed() {
      ReferencePipeline<Integer> ref = new ReferencePipeline<>(new Spliterator<Integer>() {
        @Override
        public boolean tryAdvance(Consumer<? super Integer> action) {
          return spliterator.tryAdvance(action);
        }

        @Override
        public Spliterator<Integer> trySplit() {
          return null;
        }

        @Override
        public long estimateSize() {
          return spliterator.estimateSize();
        }

        @Override
        public int characteristics() {
          return spliterator.characteristics();
        }
      }, parallel, closeHandlers);
      return ref;
    }

    @Override
    public IntStream filter(IntPredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().filter(predicate::test).mapToInt(Integer::intValue);
    }

    @Override
    public IntStream map(IntUnaryOperator mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsInt).mapToInt(Integer::intValue);
    }

    @Override
    public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::apply);
    }

    @Override
    public LongStream mapToLong(IntToLongFunction mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsLong).mapToLong(Long::longValue);
    }

    @Override
    public DoubleStream mapToDouble(IntToDoubleFunction mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsDouble).mapToDouble(Double::doubleValue);
    }

    @Override
    public IntStream flatMap(IntFunction<? extends IntStream> mapper) {
      Objects.requireNonNull(mapper);
      return boxed().flatMap(i -> {
        IntStream is = mapper.apply(i);
        return is == null ? null : is.boxed();
      }).mapToInt(Integer::intValue);
    }

    @Override
    public IntStream distinct() {
      return boxed().distinct().mapToInt(Integer::intValue);
    }

    @Override
    public IntStream sorted() {
      return boxed().sorted().mapToInt(Integer::intValue);
    }

    @Override
    public IntStream peek(IntConsumer action) {
      Objects.requireNonNull(action);
      return boxed().peek(action::accept).mapToInt(Integer::intValue);
    }

    @Override
    public IntStream limit(long maxSize) {
      return boxed().limit(maxSize).mapToInt(Integer::intValue);
    }

    @Override
    public IntStream skip(long n) {
      return boxed().skip(n).mapToInt(Integer::intValue);
    }

    @Override
    public void forEach(IntConsumer action) {
      Objects.requireNonNull(action);
      boxed().forEach(action::accept);
    }

    @Override
    public void forEachOrdered(IntConsumer action) {
      forEach(action);
    }

    @Override
    public int[] toArray() {
      Object[] objects = boxed().toArray();
      int[] result = new int[objects.length];
      for (int i = 0; i < objects.length; i++) {
        result[i] = (Integer) objects[i];
      }
      return result;
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) {
      Objects.requireNonNull(op);
      return boxed().reduce(identity, op::applyAsInt);
    }

    @Override
    public OptionalInt reduce(IntBinaryOperator op) {
      Objects.requireNonNull(op);
      Optional<Integer> opt = boxed().reduce(op::applyAsInt);
      return opt.isPresent() ? OptionalInt.of(opt.get()) : OptionalInt.empty();
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
      Objects.requireNonNull(supplier);
      Objects.requireNonNull(accumulator);
      Objects.requireNonNull(combiner);
      return boxed().collect(supplier, (container, item) -> accumulator.accept(container, item), combiner);
    }

    @Override
    public int sum() {
      return reduce(0, Integer::sum);
    }

    @Override
    public OptionalInt min() {
      return reduce(Math::min);
    }

    @Override
    public OptionalInt max() {
      return reduce(Math::max);
    }

    @Override
    public long count() {
      return boxed().count();
    }

    @Override
    public OptionalDouble average() {
      long[] count = new long[1];
      long[] sum = new long[1];
      forEach(i -> {
        count[0]++;
        sum[0] += i;
      });
      return count[0] > 0 ? OptionalDouble.of((double) sum[0] / count[0]) : OptionalDouble.empty();
    }

    @Override
    public boolean anyMatch(IntPredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().anyMatch(predicate::test);
    }

    @Override
    public boolean allMatch(IntPredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().allMatch(predicate::test);
    }

    @Override
    public boolean noneMatch(IntPredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().noneMatch(predicate::test);
    }

    @Override
    public OptionalInt findFirst() {
      Optional<Integer> opt = boxed().findFirst();
      return opt.isPresent() ? OptionalInt.of(opt.get()) : OptionalInt.empty();
    }

    @Override
    public OptionalInt findAny() {
      return findFirst();
    }

    @Override
    public LongStream asLongStream() {
      return mapToLong(i -> (long) i);
    }

    @Override
    public DoubleStream asDoubleStream() {
      return mapToDouble(i -> (double) i);
    }
  }

  static class LongPipeline implements LongStream {
    private final Spliterator.OfLong spliterator;
    private final boolean parallel;
    private List<Runnable> closeHandlers = null;

    LongPipeline(Spliterator.OfLong spliterator, boolean parallel) {
      this.spliterator = spliterator;
      this.parallel = parallel;
    }

    LongPipeline(Spliterator.OfLong spliterator, boolean parallel, List<Runnable> closeHandlers) {
      this.spliterator = spliterator;
      this.parallel = parallel;
      if (closeHandlers != null) {
        this.closeHandlers = new ArrayList<>(closeHandlers);
      }
    }

    @Override
    public PrimitiveIterator.OfLong iterator() {
      return Spliterators.iterator(spliterator());
    }

    @Override
    public Spliterator.OfLong spliterator() {
      return spliterator;
    }

    @Override
    public boolean isParallel() {
      return parallel;
    }

    @Override
    public LongStream sequential() {
      return parallel ? new LongPipeline(spliterator, false, closeHandlers) : this;
    }

    @Override
    public LongStream parallel() {
      return parallel ? this : new LongPipeline(spliterator, true, closeHandlers);
    }

    @Override
    public LongStream unordered() {
      return this;
    }

    @Override
    public LongStream onClose(Runnable closeHandler) {
      Objects.requireNonNull(closeHandler);
      if (closeHandlers == null) {
        closeHandlers = new ArrayList<>();
      }
      closeHandlers.add(closeHandler);
      return this;
    }

    @Override
    public void close() {
      if (closeHandlers != null) {
        for (Runnable r : closeHandlers) {
          r.run();
        }
        closeHandlers = null;
      }
    }

    @Override
    public Stream<Long> boxed() {
      return new ReferencePipeline<>(new Spliterator<Long>() {
        @Override
        public boolean tryAdvance(Consumer<? super Long> action) {
          return spliterator.tryAdvance(action);
        }

        @Override
        public Spliterator<Long> trySplit() {
          return null;
        }

        @Override
        public long estimateSize() {
          return spliterator.estimateSize();
        }

        @Override
        public int characteristics() {
          return spliterator.characteristics();
        }
      }, parallel, closeHandlers);
    }

    @Override
    public LongStream filter(LongPredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().filter(predicate::test).mapToLong(Long::longValue);
    }

    @Override
    public LongStream map(LongUnaryOperator mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsLong).mapToLong(Long::longValue);
    }

    @Override
    public <U> Stream<U> mapToObj(LongFunction<? extends U> mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::apply);
    }

    @Override
    public IntStream mapToInt(LongToIntFunction mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsInt).mapToInt(Integer::intValue);
    }

    @Override
    public DoubleStream mapToDouble(LongToDoubleFunction mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsDouble).mapToDouble(Double::doubleValue);
    }

    @Override
    public LongStream flatMap(LongFunction<? extends LongStream> mapper) {
      Objects.requireNonNull(mapper);
      return boxed().flatMap(l -> {
        LongStream ls = mapper.apply(l);
        return ls == null ? null : ls.boxed();
      }).mapToLong(Long::longValue);
    }

    @Override
    public LongStream distinct() {
      return boxed().distinct().mapToLong(Long::longValue);
    }

    @Override
    public LongStream sorted() {
      return boxed().sorted().mapToLong(Long::longValue);
    }

    @Override
    public LongStream peek(LongConsumer action) {
      Objects.requireNonNull(action);
      return boxed().peek(action::accept).mapToLong(Long::longValue);
    }

    @Override
    public LongStream limit(long maxSize) {
      return boxed().limit(maxSize).mapToLong(Long::longValue);
    }

    @Override
    public LongStream skip(long n) {
      return boxed().skip(n).mapToLong(Long::longValue);
    }

    @Override
    public void forEach(LongConsumer action) {
      Objects.requireNonNull(action);
      boxed().forEach(action::accept);
    }

    @Override
    public void forEachOrdered(LongConsumer action) {
      forEach(action);
    }

    @Override
    public long[] toArray() {
      Object[] objects = boxed().toArray();
      long[] result = new long[objects.length];
      for (int i = 0; i < objects.length; i++) {
        result[i] = (Long) objects[i];
      }
      return result;
    }

    @Override
    public long reduce(long identity, LongBinaryOperator op) {
      Objects.requireNonNull(op);
      return boxed().reduce(identity, op::applyAsLong);
    }

    @Override
    public OptionalLong reduce(LongBinaryOperator op) {
      Objects.requireNonNull(op);
      Optional<Long> opt = boxed().reduce(op::applyAsLong);
      return opt.isPresent() ? OptionalLong.of(opt.get()) : OptionalLong.empty();
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner) {
      Objects.requireNonNull(supplier);
      Objects.requireNonNull(accumulator);
      Objects.requireNonNull(combiner);
      return boxed().collect(supplier, (container, item) -> accumulator.accept(container, item), combiner);
    }

    @Override
    public long sum() {
      return reduce(0L, Long::sum);
    }

    @Override
    public OptionalLong min() {
      return reduce(Math::min);
    }

    @Override
    public OptionalLong max() {
      return reduce(Math::max);
    }

    @Override
    public long count() {
      return boxed().count();
    }

    @Override
    public OptionalDouble average() {
      long[] count = new long[1];
      long[] sum = new long[1];
      forEach(l -> {
        count[0]++;
        sum[0] += l;
      });
      return count[0] > 0 ? OptionalDouble.of((double) sum[0] / count[0]) : OptionalDouble.empty();
    }

    @Override
    public boolean anyMatch(LongPredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().anyMatch(predicate::test);
    }

    @Override
    public boolean allMatch(LongPredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().allMatch(predicate::test);
    }

    @Override
    public boolean noneMatch(LongPredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().noneMatch(predicate::test);
    }

    @Override
    public OptionalLong findFirst() {
      Optional<Long> opt = boxed().findFirst();
      return opt.isPresent() ? OptionalLong.of(opt.get()) : OptionalLong.empty();
    }

    @Override
    public OptionalLong findAny() {
      return findFirst();
    }

    @Override
    public DoubleStream asDoubleStream() {
      return mapToDouble(l -> (double) l);
    }
  }

  static class DoublePipeline implements DoubleStream {
    private final Spliterator.OfDouble spliterator;
    private final boolean parallel;
    private List<Runnable> closeHandlers = null;

    DoublePipeline(Spliterator.OfDouble spliterator, boolean parallel) {
      this.spliterator = spliterator;
      this.parallel = parallel;
    }

    DoublePipeline(Spliterator.OfDouble spliterator, boolean parallel, List<Runnable> closeHandlers) {
      this.spliterator = spliterator;
      this.parallel = parallel;
      if (closeHandlers != null) {
        this.closeHandlers = new ArrayList<>(closeHandlers);
      }
    }

    @Override
    public PrimitiveIterator.OfDouble iterator() {
      return Spliterators.iterator(spliterator());
    }

    @Override
    public Spliterator.OfDouble spliterator() {
      return spliterator;
    }

    @Override
    public boolean isParallel() {
      return parallel;
    }

    @Override
    public DoubleStream sequential() {
      return parallel ? new DoublePipeline(spliterator, false, closeHandlers) : this;
    }

    @Override
    public DoubleStream parallel() {
      return parallel ? this : new DoublePipeline(spliterator, true, closeHandlers);
    }

    @Override
    public DoubleStream unordered() {
      return this;
    }

    @Override
    public DoubleStream onClose(Runnable closeHandler) {
      Objects.requireNonNull(closeHandler);
      if (closeHandlers == null) {
        closeHandlers = new ArrayList<>();
      }
      closeHandlers.add(closeHandler);
      return this;
    }

    @Override
    public void close() {
      if (closeHandlers != null) {
        for (Runnable r : closeHandlers) {
          r.run();
        }
        closeHandlers = null;
      }
    }

    @Override
    public Stream<Double> boxed() {
      return new ReferencePipeline<>(new Spliterator<Double>() {
        @Override
        public boolean tryAdvance(Consumer<? super Double> action) {
          return spliterator.tryAdvance(action);
        }

        @Override
        public Spliterator<Double> trySplit() {
          return null;
        }

        @Override
        public long estimateSize() {
          return spliterator.estimateSize();
        }

        @Override
        public int characteristics() {
          return spliterator.characteristics();
        }
      }, parallel, closeHandlers);
    }

    @Override
    public DoubleStream filter(DoublePredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().filter(predicate::test).mapToDouble(Double::doubleValue);
    }

    @Override
    public DoubleStream map(DoubleUnaryOperator mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsDouble).mapToDouble(Double::doubleValue);
    }

    @Override
    public <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::apply);
    }

    @Override
    public IntStream mapToInt(DoubleToIntFunction mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsInt).mapToInt(Integer::intValue);
    }

    @Override
    public LongStream mapToLong(DoubleToLongFunction mapper) {
      Objects.requireNonNull(mapper);
      return boxed().map(mapper::applyAsLong).mapToLong(Long::longValue);
    }

    @Override
    public DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
      Objects.requireNonNull(mapper);
      return boxed().flatMap(d -> {
        DoubleStream ds = mapper.apply(d);
        return ds == null ? null : ds.boxed();
      }).mapToDouble(Double::doubleValue);
    }

    @Override
    public DoubleStream distinct() {
      return boxed().distinct().mapToDouble(Double::doubleValue);
    }

    @Override
    public DoubleStream sorted() {
      return boxed().sorted().mapToDouble(Double::doubleValue);
    }

    @Override
    public DoubleStream peek(DoubleConsumer action) {
      Objects.requireNonNull(action);
      return boxed().peek(action::accept).mapToDouble(Double::doubleValue);
    }

    @Override
    public DoubleStream limit(long maxSize) {
      return boxed().limit(maxSize).mapToDouble(Double::doubleValue);
    }

    @Override
    public DoubleStream skip(long n) {
      return boxed().skip(n).mapToDouble(Double::doubleValue);
    }

    @Override
    public void forEach(DoubleConsumer action) {
      Objects.requireNonNull(action);
      boxed().forEach(action::accept);
    }

    @Override
    public void forEachOrdered(DoubleConsumer action) {
      forEach(action);
    }

    @Override
    public double[] toArray() {
      Object[] objects = boxed().toArray();
      double[] result = new double[objects.length];
      for (int i = 0; i < objects.length; i++) {
        result[i] = (Double) objects[i];
      }
      return result;
    }

    @Override
    public double reduce(double identity, DoubleBinaryOperator op) {
      Objects.requireNonNull(op);
      return boxed().reduce(identity, op::applyAsDouble);
    }

    @Override
    public OptionalDouble reduce(DoubleBinaryOperator op) {
      Objects.requireNonNull(op);
      Optional<Double> opt = boxed().reduce(op::applyAsDouble);
      return opt.isPresent() ? OptionalDouble.of(opt.get()) : OptionalDouble.empty();
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner) {
      Objects.requireNonNull(supplier);
      Objects.requireNonNull(accumulator);
      Objects.requireNonNull(combiner);
      return boxed().collect(supplier, (container, item) -> accumulator.accept(container, item), combiner);
    }

    @Override
    public double sum() {
      return reduce(0.0, Double::sum);
    }

    @Override
    public OptionalDouble min() {
      return reduce(Math::min);
    }

    @Override
    public OptionalDouble max() {
      return reduce(Math::max);
    }

    @Override
    public long count() {
      return boxed().count();
    }

    @Override
    public OptionalDouble average() {
      long[] count = new long[1];
      double[] sum = new double[1];
      forEach(d -> {
        count[0]++;
        sum[0] += d;
      });
      return count[0] > 0 ? OptionalDouble.of(sum[0] / count[0]) : OptionalDouble.empty();
    }

    @Override
    public boolean anyMatch(DoublePredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().anyMatch(predicate::test);
    }

    @Override
    public boolean allMatch(DoublePredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().allMatch(predicate::test);
    }

    @Override
    public boolean noneMatch(DoublePredicate predicate) {
      Objects.requireNonNull(predicate);
      return boxed().noneMatch(predicate::test);
    }

    @Override
    public OptionalDouble findFirst() {
      Optional<Double> opt = boxed().findFirst();
      return opt.isPresent() ? OptionalDouble.of(opt.get()) : OptionalDouble.empty();
    }

    @Override
    public OptionalDouble findAny() {
      return findFirst();
    }
  }
}
