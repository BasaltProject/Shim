/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public final class Spliterators {
  private Spliterators() {}

  @SuppressWarnings("rawtypes")
  private static final Spliterator EMPTY_SPLITERATOR = new EmptySpliterator.OfRef();
  private static final Spliterator.OfInt EMPTY_INT_SPLITERATOR = new EmptySpliterator.OfInt();
  private static final Spliterator.OfLong EMPTY_LONG_SPLITERATOR = new EmptySpliterator.OfLong();
  private static final Spliterator.OfDouble EMPTY_DOUBLE_SPLITERATOR = new EmptySpliterator.OfDouble();

  @SuppressWarnings("unchecked")
  public static <T> Spliterator<T> emptySpliterator() {
    return (Spliterator<T>) EMPTY_SPLITERATOR;
  }

  public static Spliterator.OfInt emptyIntSpliterator() {
    return EMPTY_INT_SPLITERATOR;
  }

  public static Spliterator.OfLong emptyLongSpliterator() {
    return EMPTY_LONG_SPLITERATOR;
  }

  public static Spliterator.OfDouble emptyDoubleSpliterator() {
    return EMPTY_DOUBLE_SPLITERATOR;
  }

  public static <T> Spliterator<T> spliterator(Object[] array, int additionalCharacteristics) {
    return spliterator(array, 0, array.length, additionalCharacteristics);
  }

  public static <T> Spliterator<T> spliterator(Object[] array, int fromIndex, int toIndex, int additionalCharacteristics) {
    Objects.requireNonNull(array);
    checkFromToBounds(array.length, fromIndex, toIndex);
    return new ArraySpliterator<>(array, fromIndex, toIndex, additionalCharacteristics);
  }

  public static Spliterator.OfInt spliterator(int[] array, int additionalCharacteristics) {
    return spliterator(array, 0, array.length, additionalCharacteristics);
  }

  public static Spliterator.OfInt spliterator(int[] array, int fromIndex, int toIndex, int additionalCharacteristics) {
    Objects.requireNonNull(array);
    checkFromToBounds(array.length, fromIndex, toIndex);
    return new IntArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
  }

  public static Spliterator.OfLong spliterator(long[] array, int additionalCharacteristics) {
    return spliterator(array, 0, array.length, additionalCharacteristics);
  }

  public static Spliterator.OfLong spliterator(long[] array, int fromIndex, int toIndex, int additionalCharacteristics) {
    Objects.requireNonNull(array);
    checkFromToBounds(array.length, fromIndex, toIndex);
    return new LongArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
  }

  public static Spliterator.OfDouble spliterator(double[] array, int additionalCharacteristics) {
    return spliterator(array, 0, array.length, additionalCharacteristics);
  }

  public static Spliterator.OfDouble spliterator(double[] array, int fromIndex, int toIndex, int additionalCharacteristics) {
    Objects.requireNonNull(array);
    checkFromToBounds(array.length, fromIndex, toIndex);
    return new DoubleArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
  }

  public static <T> Spliterator<T> spliterator(Collection<? extends T> c, int additionalCharacteristics) {
    return new IteratorSpliterator<>(Objects.requireNonNull(c), additionalCharacteristics);
  }

  public static <T> Spliterator<T> spliterator(Iterator<? extends T> iterator, long size, int characteristics) {
    return new IteratorSpliterator<>(Objects.requireNonNull(iterator), size, characteristics);
  }

  public static <T> Spliterator<T> spliteratorUnknownSize(Iterator<? extends T> iterator, int characteristics) {
    return new IteratorSpliterator<>(Objects.requireNonNull(iterator), characteristics);
  }

  public static Spliterator.OfInt spliterator(PrimitiveIterator.OfInt iterator, long size, int characteristics) {
    return new PrimitiveIteratorSpliterator.OfInt(Objects.requireNonNull(iterator), size, characteristics);
  }

  public static Spliterator.OfInt spliteratorUnknownSize(PrimitiveIterator.OfInt iterator, int characteristics) {
    return new PrimitiveIteratorSpliterator.OfInt(Objects.requireNonNull(iterator), characteristics);
  }

  public static Spliterator.OfLong spliterator(PrimitiveIterator.OfLong iterator, long size, int characteristics) {
    return new PrimitiveIteratorSpliterator.OfLong(Objects.requireNonNull(iterator), size, characteristics);
  }

  public static Spliterator.OfLong spliteratorUnknownSize(PrimitiveIterator.OfLong iterator, int characteristics) {
    return new PrimitiveIteratorSpliterator.OfLong(Objects.requireNonNull(iterator), characteristics);
  }

  public static Spliterator.OfDouble spliterator(PrimitiveIterator.OfDouble iterator, long size, int characteristics) {
    return new PrimitiveIteratorSpliterator.OfDouble(Objects.requireNonNull(iterator), size, characteristics);
  }

  public static Spliterator.OfDouble spliteratorUnknownSize(PrimitiveIterator.OfDouble iterator, int characteristics) {
    return new PrimitiveIteratorSpliterator.OfDouble(Objects.requireNonNull(iterator), characteristics);
  }

  public static <T> Iterator<T> iterator(Spliterator<? extends T> spliterator) {
    Objects.requireNonNull(spliterator);
    class Adapter implements Iterator<T>, Consumer<T> {
      boolean valueReady = false;
      T nextElement;

      @Override
      public void accept(T t) {
        valueReady = true;
        nextElement = t;
      }

      @Override
      public boolean hasNext() {
        if (!valueReady) {
          spliterator.tryAdvance(this);
        }
        return valueReady;
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        valueReady = false;
        return nextElement;
      }

      @Override
      public void forEachRemaining(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (valueReady) {
          valueReady = false;
          action.accept(nextElement);
        }
        spliterator.forEachRemaining(action);
      }
    }
    return new Adapter();
  }

  public static PrimitiveIterator.OfInt iterator(Spliterator.OfInt spliterator) {
    Objects.requireNonNull(spliterator);
    class Adapter implements PrimitiveIterator.OfInt, IntConsumer {
      boolean valueReady = false;
      int nextElement;

      @Override
      public void accept(int t) {
        valueReady = true;
        nextElement = t;
      }

      @Override
      public boolean hasNext() {
        if (!valueReady) {
          spliterator.tryAdvance(this);
        }
        return valueReady;
      }

      @Override
      public int nextInt() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        valueReady = false;
        return nextElement;
      }

      @Override
      public void forEachRemaining(IntConsumer action) {
        Objects.requireNonNull(action);
        if (valueReady) {
          valueReady = false;
          action.accept(nextElement);
        }
        spliterator.forEachRemaining(action);
      }
    }
    return new Adapter();
  }

  public static PrimitiveIterator.OfLong iterator(Spliterator.OfLong spliterator) {
    Objects.requireNonNull(spliterator);
    class Adapter implements PrimitiveIterator.OfLong, LongConsumer {
      boolean valueReady = false;
      long nextElement;

      @Override
      public void accept(long t) {
        valueReady = true;
        nextElement = t;
      }

      @Override
      public boolean hasNext() {
        if (!valueReady) {
          spliterator.tryAdvance(this);
        }
        return valueReady;
      }

      @Override
      public long nextLong() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        valueReady = false;
        return nextElement;
      }

      @Override
      public void forEachRemaining(LongConsumer action) {
        Objects.requireNonNull(action);
        if (valueReady) {
          valueReady = false;
          action.accept(nextElement);
        }
        spliterator.forEachRemaining(action);
      }
    }
    return new Adapter();
  }

  public static PrimitiveIterator.OfDouble iterator(Spliterator.OfDouble spliterator) {
    Objects.requireNonNull(spliterator);
    class Adapter implements PrimitiveIterator.OfDouble, DoubleConsumer {
      boolean valueReady = false;
      double nextElement;

      @Override
      public void accept(double t) {
        valueReady = true;
        nextElement = t;
      }

      @Override
      public boolean hasNext() {
        if (!valueReady) {
          spliterator.tryAdvance(this);
        }
        return valueReady;
      }

      @Override
      public double nextDouble() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        valueReady = false;
        return nextElement;
      }

      @Override
      public void forEachRemaining(DoubleConsumer action) {
        Objects.requireNonNull(action);
        if (valueReady) {
          valueReady = false;
          action.accept(nextElement);
        }
        spliterator.forEachRemaining(action);
      }
    }
    return new Adapter();
  }

  private static void checkFromToBounds(int arrayLength, int origin, int fence) {
    if (origin < 0 || origin > fence || fence > arrayLength) {
      throw new ArrayIndexOutOfBoundsException("origin=" + origin + " fence=" + fence + " length=" + arrayLength);
    }
  }

  private abstract static class EmptySpliterator<T, S extends Spliterator<T>, C> {
    public S trySplit() {
      return null;
    }

    public boolean tryAdvance(C consumer) {
      Objects.requireNonNull(consumer);
      return false;
    }

    public void forEachRemaining(C consumer) {
      Objects.requireNonNull(consumer);
    }

    public long estimateSize() {
      return 0;
    }

    public int characteristics() {
      return Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    private static final class OfRef<T>
        extends EmptySpliterator<T, Spliterator<T>, Consumer<? super T>>
        implements Spliterator<T> {
      OfRef() {}
    }

    private static final class OfInt
        extends EmptySpliterator<Integer, Spliterator.OfInt, IntConsumer>
        implements Spliterator.OfInt {
      OfInt() {}
    }

    private static final class OfLong
        extends EmptySpliterator<Long, Spliterator.OfLong, LongConsumer>
        implements Spliterator.OfLong {
      OfLong() {}
    }

    private static final class OfDouble
        extends EmptySpliterator<Double, Spliterator.OfDouble, DoubleConsumer>
        implements Spliterator.OfDouble {
      OfDouble() {}
    }
  }

  static final class ArraySpliterator<T> implements Spliterator<T> {
    private final Object[] array;
    private int index;
    private final int fence;
    private final int characteristics;

    public ArraySpliterator(Object[] array, int additionalCharacteristics) {
      this(array, 0, array.length, additionalCharacteristics);
    }

    public ArraySpliterator(Object[] array, int origin, int fence, int additionalCharacteristics) {
      this.array = array;
      this.index = origin;
      this.fence = fence;
      this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    @Override
    public Spliterator<T> trySplit() {
      int lo = index, mid = (lo + fence) >>> 1;
      if (lo >= mid) {
        return null;
      }
      index = mid;
      return new ArraySpliterator<>(array, lo, mid, characteristics);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
      Objects.requireNonNull(action);
      Object[] a = array;
      int hi = fence;
      for (int i = index; i < hi; i++) {
        action.accept((T) a[i]);
      }
      index = hi;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      Objects.requireNonNull(action);
      if (index >= 0 && index < fence) {
        action.accept((T) array[index++]);
        return true;
      }
      return false;
    }

    @Override
    public long estimateSize() {
      return (long) (fence - index);
    }

    @Override
    public int characteristics() {
      return characteristics;
    }
  }

  static final class IntArraySpliterator implements Spliterator.OfInt {
    private final int[] array;
    private int index;
    private final int fence;
    private final int characteristics;

    public IntArraySpliterator(int[] array, int origin, int fence, int additionalCharacteristics) {
      this.array = array;
      this.index = origin;
      this.fence = fence;
      this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    @Override
    public OfInt trySplit() {
      int lo = index, mid = (lo + fence) >>> 1;
      if (lo >= mid) {
        return null;
      }
      index = mid;
      return new IntArraySpliterator(array, lo, mid, characteristics);
    }

    @Override
    public void forEachRemaining(IntConsumer action) {
      Objects.requireNonNull(action);
      int[] a = array;
      int hi = fence;
      for (int i = index; i < hi; i++) {
        action.accept(a[i]);
      }
      index = hi;
    }

    @Override
    public boolean tryAdvance(IntConsumer action) {
      Objects.requireNonNull(action);
      if (index >= 0 && index < fence) {
        action.accept(array[index++]);
        return true;
      }
      return false;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Integer> action) {
      if (action instanceof IntConsumer) {
        return tryAdvance((IntConsumer) action);
      }
      Objects.requireNonNull(action);
      return tryAdvance((IntConsumer) action::accept);
    }

    @Override
    public void forEachRemaining(Consumer<? super Integer> action) {
      if (action instanceof IntConsumer) {
        forEachRemaining((IntConsumer) action);
      } else {
        Objects.requireNonNull(action);
        forEachRemaining((IntConsumer) action::accept);
      }
    }

    @Override
    public long estimateSize() {
      return (long) (fence - index);
    }

    @Override
    public int characteristics() {
      return characteristics;
    }
  }

  static final class LongArraySpliterator implements Spliterator.OfLong {
    private final long[] array;
    private int index;
    private final int fence;
    private final int characteristics;

    public LongArraySpliterator(long[] array, int origin, int fence, int additionalCharacteristics) {
      this.array = array;
      this.index = origin;
      this.fence = fence;
      this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    @Override
    public OfLong trySplit() {
      int lo = index, mid = (lo + fence) >>> 1;
      if (lo >= mid) {
        return null;
      }
      index = mid;
      return new LongArraySpliterator(array, lo, mid, characteristics);
    }

    @Override
    public void forEachRemaining(LongConsumer action) {
      Objects.requireNonNull(action);
      long[] a = array;
      int hi = fence;
      for (int i = index; i < hi; i++) {
        action.accept(a[i]);
      }
      index = hi;
    }

    @Override
    public boolean tryAdvance(LongConsumer action) {
      Objects.requireNonNull(action);
      if (index >= 0 && index < fence) {
        action.accept(array[index++]);
        return true;
      }
      return false;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Long> action) {
      if (action instanceof LongConsumer) {
        return tryAdvance((LongConsumer) action);
      }
      Objects.requireNonNull(action);
      return tryAdvance((LongConsumer) action::accept);
    }

    @Override
    public void forEachRemaining(Consumer<? super Long> action) {
      if (action instanceof LongConsumer) {
        forEachRemaining((LongConsumer) action);
      } else {
        Objects.requireNonNull(action);
        forEachRemaining((LongConsumer) action::accept);
      }
    }

    @Override
    public long estimateSize() {
      return (long) (fence - index);
    }

    @Override
    public int characteristics() {
      return characteristics;
    }
  }

  static final class DoubleArraySpliterator implements Spliterator.OfDouble {
    private final double[] array;
    private int index;
    private final int fence;
    private final int characteristics;

    public DoubleArraySpliterator(double[] array, int origin, int fence, int additionalCharacteristics) {
      this.array = array;
      this.index = origin;
      this.fence = fence;
      this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    @Override
    public OfDouble trySplit() {
      int lo = index, mid = (lo + fence) >>> 1;
      if (lo >= mid) {
        return null;
      }
      index = mid;
      return new DoubleArraySpliterator(array, lo, mid, characteristics);
    }

    @Override
    public void forEachRemaining(DoubleConsumer action) {
      Objects.requireNonNull(action);
      double[] a = array;
      int hi = fence;
      for (int i = index; i < hi; i++) {
        action.accept(a[i]);
      }
      index = hi;
    }

    @Override
    public boolean tryAdvance(DoubleConsumer action) {
      Objects.requireNonNull(action);
      if (index >= 0 && index < fence) {
        action.accept(array[index++]);
        return true;
      }
      return false;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Double> action) {
      if (action instanceof DoubleConsumer) {
        return tryAdvance((DoubleConsumer) action);
      }
      Objects.requireNonNull(action);
      return tryAdvance((DoubleConsumer) action::accept);
    }

    @Override
    public void forEachRemaining(Consumer<? super Double> action) {
      if (action instanceof DoubleConsumer) {
        forEachRemaining((DoubleConsumer) action);
      } else {
        Objects.requireNonNull(action);
        forEachRemaining((DoubleConsumer) action::accept);
      }
    }

    @Override
    public long estimateSize() {
      return (long) (fence - index);
    }

    @Override
    public int characteristics() {
      return characteristics;
    }
  }

  public static class IteratorSpliterator<T> implements Spliterator<T> {
    private final Collection<? extends T> collection;
    private Iterator<? extends T> it;
    private final int characteristics;
    private long est;

    public IteratorSpliterator(Collection<? extends T> collection, int additionalCharacteristics) {
      this.collection = collection;
      this.it = null;
      this.characteristics = (additionalCharacteristics & Spliterator.CONCURRENT) == 0
          ? additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED
          : additionalCharacteristics;
    }

    public IteratorSpliterator(Iterator<? extends T> iterator, long size, int characteristics) {
      this.collection = null;
      this.it = iterator;
      this.est = size;
      this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
          ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
          : characteristics;
    }

    public IteratorSpliterator(Iterator<? extends T> iterator, int characteristics) {
      this.collection = null;
      this.it = iterator;
      this.est = Long.MAX_VALUE;
      this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
    }

    @Override
    public Spliterator<T> trySplit() {
      return null;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
      Objects.requireNonNull(action);
      if (it == null) {
        it = collection.iterator();
      }
      while (it.hasNext()) {
        action.accept(it.next());
      }
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      Objects.requireNonNull(action);
      if (it == null) {
        it = collection.iterator();
      }
      if (it.hasNext()) {
        action.accept(it.next());
        return true;
      }
      return false;
    }

    @Override
    public long estimateSize() {
      if (it == null) {
        return (long) collection.size();
      }
      return est;
    }

    @Override
    public int characteristics() {
      return characteristics;
    }
  }

  abstract static class PrimitiveIteratorSpliterator<T, T_CONS, S extends Spliterator.OfPrimitive<T, T_CONS, S>> {
    protected final int characteristics;
    protected long est;

    PrimitiveIteratorSpliterator(long size, int characteristics) {
      this.est = size;
      this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
          ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
          : characteristics;
    }

    PrimitiveIteratorSpliterator(int characteristics) {
      this.est = Long.MAX_VALUE;
      this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
    }

    public S trySplit() {
      return null;
    }

    public long estimateSize() {
      return est;
    }

    public int characteristics() {
      return characteristics;
    }

    static final class OfInt extends PrimitiveIteratorSpliterator<Integer, IntConsumer, Spliterator.OfInt>
        implements Spliterator.OfInt {
      private final PrimitiveIterator.OfInt it;

      OfInt(PrimitiveIterator.OfInt iterator, long size, int characteristics) {
        super(size, characteristics);
        this.it = iterator;
      }

      OfInt(PrimitiveIterator.OfInt iterator, int characteristics) {
        super(characteristics);
        this.it = iterator;
      }

      @Override
      public boolean tryAdvance(IntConsumer action) {
        Objects.requireNonNull(action);
        if (it.hasNext()) {
          action.accept(it.nextInt());
          return true;
        }
        return false;
      }

      @Override
      public void forEachRemaining(IntConsumer action) {
        Objects.requireNonNull(action);
        while (it.hasNext()) {
          action.accept(it.nextInt());
        }
      }

      @Override
      public boolean tryAdvance(Consumer<? super Integer> action) {
        if (action instanceof IntConsumer) {
          return tryAdvance((IntConsumer) action);
        }
        Objects.requireNonNull(action);
        return tryAdvance((IntConsumer) action::accept);
      }

      @Override
      public void forEachRemaining(Consumer<? super Integer> action) {
        if (action instanceof IntConsumer) {
          forEachRemaining((IntConsumer) action);
        } else {
          Objects.requireNonNull(action);
          forEachRemaining((IntConsumer) action::accept);
        }
      }
    }

    static final class OfLong extends PrimitiveIteratorSpliterator<Long, LongConsumer, Spliterator.OfLong>
        implements Spliterator.OfLong {
      private final PrimitiveIterator.OfLong it;

      OfLong(PrimitiveIterator.OfLong iterator, long size, int characteristics) {
        super(size, characteristics);
        this.it = iterator;
      }

      OfLong(PrimitiveIterator.OfLong iterator, int characteristics) {
        super(characteristics);
        this.it = iterator;
      }

      @Override
      public boolean tryAdvance(LongConsumer action) {
        Objects.requireNonNull(action);
        if (it.hasNext()) {
          action.accept(it.nextLong());
          return true;
        }
        return false;
      }

      @Override
      public void forEachRemaining(LongConsumer action) {
        Objects.requireNonNull(action);
        while (it.hasNext()) {
          action.accept(it.nextLong());
        }
      }

      @Override
      public boolean tryAdvance(Consumer<? super Long> action) {
        if (action instanceof LongConsumer) {
          return tryAdvance((LongConsumer) action);
        }
        Objects.requireNonNull(action);
        return tryAdvance((LongConsumer) action::accept);
      }

      @Override
      public void forEachRemaining(Consumer<? super Long> action) {
        if (action instanceof LongConsumer) {
          forEachRemaining((LongConsumer) action);
        } else {
          Objects.requireNonNull(action);
          forEachRemaining((LongConsumer) action::accept);
        }
      }
    }

    static final class OfDouble extends PrimitiveIteratorSpliterator<Double, DoubleConsumer, Spliterator.OfDouble>
        implements Spliterator.OfDouble {
      private final PrimitiveIterator.OfDouble it;

      OfDouble(PrimitiveIterator.OfDouble iterator, long size, int characteristics) {
        super(size, characteristics);
        this.it = iterator;
      }

      OfDouble(PrimitiveIterator.OfDouble iterator, int characteristics) {
        super(characteristics);
        this.it = iterator;
      }

      @Override
      public boolean tryAdvance(DoubleConsumer action) {
        Objects.requireNonNull(action);
        if (it.hasNext()) {
          action.accept(it.nextDouble());
          return true;
        }
        return false;
      }

      @Override
      public void forEachRemaining(DoubleConsumer action) {
        Objects.requireNonNull(action);
        while (it.hasNext()) {
          action.accept(it.nextDouble());
        }
      }

      @Override
      public boolean tryAdvance(Consumer<? super Double> action) {
        if (action instanceof DoubleConsumer) {
          return tryAdvance((DoubleConsumer) action);
        }
        Objects.requireNonNull(action);
        return tryAdvance((DoubleConsumer) action::accept);
      }

      @Override
      public void forEachRemaining(Consumer<? super Double> action) {
        if (action instanceof DoubleConsumer) {
          forEachRemaining((DoubleConsumer) action);
        } else {
          Objects.requireNonNull(action);
          forEachRemaining((DoubleConsumer) action::accept);
        }
      }
    }
  }
}
